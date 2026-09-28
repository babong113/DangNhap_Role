package com.bteam.badmintonmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String login;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
