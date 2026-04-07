package com.Securegate.Securegate.dto;

import lombok.Data;

@Data
public class ForgetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}