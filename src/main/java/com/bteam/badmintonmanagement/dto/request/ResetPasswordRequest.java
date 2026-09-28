package com.bteam.badmintonmanagement.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    @NotBlank(message = "email không được để trống")
    @Email(message = "email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mã xác thực không được để trống")
    @Pattern(
            regexp = "^[0-9]{6}$",
            message = "Mã OTP phải gồm đúng 6 chữ số"
    )
    private String otp;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(
            min = 6,
            message = "Mật khẩu phải có ít nhất 6 ký tự"
    )
    private String newPassword;

    @NotBlank(message = "Mật khẩu xác nhận không được để trống")
    private String confirmPassword;
}
