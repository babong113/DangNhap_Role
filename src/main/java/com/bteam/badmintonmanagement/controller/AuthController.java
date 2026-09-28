package com.bteam.badmintonmanagement.controller;

import com.bteam.badmintonmanagement.dto.request.ForgotPasswordRequest;
import com.bteam.badmintonmanagement.dto.request.LoginRequest;
import com.bteam.badmintonmanagement.dto.request.RegisterRequest;
import com.bteam.badmintonmanagement.dto.request.ResetPasswordRequest;
import com.bteam.badmintonmanagement.dto.response.ApiResponse;
import com.bteam.badmintonmanagement.dto.response.LoginResponse;
import com.bteam.badmintonmanagement.dto.response.RegisterResponse;
import com.bteam.badmintonmanagement.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(
            @Valid
            @RequestBody
            RegisterRequest request)
    {
        RegisterResponse responseRegister =
                authService.register(request);

        ApiResponse<RegisterResponse> response =
                ApiResponse.<RegisterResponse>builder()
                        .success(true)
                        .message("Đăng ký thành công")
                        .data(responseRegister)
                        .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login (
            @Valid
            @RequestBody
            LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse

    )
    {
        LoginResponse respone=authService.login(request,httpRequest,httpResponse);
        ApiResponse<LoginResponse>
                apiResponse=ApiResponse.<LoginResponse>builder()
                .success(true)
                .message("Đăng nhập thành công ")
                .data(respone)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    )
    {
        authService.forgotPassword(request);
        ApiResponse<Void> apiResponse=ApiResponse.<Void>builder()
                .success(true)
                .message("Gửi yêu cầu đổi mật khẩu thành công")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    )
    {
        authService.resetPassword(request);
        ApiResponse<Void> apiResponse=ApiResponse.<Void>builder()
                .success(true)
                .message("Đổi mật khẩu thành công")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
