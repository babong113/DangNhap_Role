package com.bteam.badmintonmanagement.service;

import com.bteam.badmintonmanagement.dto.request.ForgotPasswordRequest;
import com.bteam.badmintonmanagement.dto.request.LoginRequest;
import com.bteam.badmintonmanagement.dto.request.RegisterRequest;
import com.bteam.badmintonmanagement.dto.request.ResetPasswordRequest;
import com.bteam.badmintonmanagement.dto.response.LoginResponse;
import com.bteam.badmintonmanagement.dto.response.RegisterResponse;
import com.bteam.badmintonmanagement.entity.user.User;
import com.bteam.badmintonmanagement.entity.user.UserRole;
import com.bteam.badmintonmanagement.entity.user.UserStatus;
import com.bteam.badmintonmanagement.exception.InvalidDataException;
import com.bteam.badmintonmanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final SecureRandom secureRandom=new SecureRandom();
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public RegisterResponse register(RegisterRequest request)
    {
        String email=request.getEmail().trim().toLowerCase();
        String phoneNumber=request.getPhoneNumber().trim();

        if (userRepository.existsByEmail(email)) {

            throw new InvalidDataException("Email đã tồn tại");
        }

        if (userRepository.existsByPhoneNumber(phoneNumber))
        {

            throw new InvalidDataException("Số điện thoại đã tồn tại");
        }

        User user=User.builder()
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(String.valueOf(UserRole.CUSTOMER))
                .status(String.valueOf(UserStatus.ACTIVE))
                .build();

        User savedUser =
                userRepository.save(user);

        return RegisterResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .phoneNumber(savedUser.getPhoneNumber())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .status(savedUser.getStatus())
                .build();
    }

    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse)
    {
        String login=request.getLogin().trim().toLowerCase();
        Authentication authentication;
        try {
             authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    login,
                                    request.getPassword()
                            )
                    );
        } catch (BadCredentialsException e) {

            throw new InvalidDataException("Tài khoản hoặc mật khẩu không đúng");

        } catch (DisabledException e){

            throw new InvalidDataException("Tài khoản đã bị khóa");
        }

        User user;

        if (login.contains("@")) {
            user = userRepository
                    .findByEmail(login.toLowerCase())
                    .orElseThrow(
                            () -> new InvalidDataException(
                                    "Không tìm thấy tài khoản"
                            )
                    );
        } else {
            user = userRepository
                    .findByPhoneNumber(login)
                    .orElseThrow(
                            () -> new InvalidDataException(
                                    "Không tìm thấy tài khoản"
                            )
                    );
        }
        SecurityContext context =
                SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(
                context,
                httpRequest,
                httpResponse
        );
        return LoginResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .build();
    }

    public void forgotPassword(ForgotPasswordRequest request)
    {
        String email=request.getEmail().trim().toLowerCase();
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new InvalidDataException("Email không tồn tại"));

        String otp=String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        user.setResetOtp(otp);
        userRepository.save(user);

        emailService.sendMailResetPasswordOtp(email,otp);
    }

    public void resetPassword(ResetPasswordRequest request)
    {
        String email=request.getEmail().trim().toLowerCase();
        User user=userRepository.findByEmail(email)
                .orElseThrow(()->new InvalidDataException("Email không tồn tại"));

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new InvalidDataException(
                    "Mật khẩu xác nhận không khớp"
            );
        }

        String savedOtp=user.getResetOtp();
        String requestOtp=request.getOtp().trim();

        if(savedOtp==null || !savedOtp.equals(requestOtp))
        {
            throw new InvalidDataException("Mã otp không hợp lệ");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        user.setResetOtp(null);

        userRepository.save(user);
    }

}
