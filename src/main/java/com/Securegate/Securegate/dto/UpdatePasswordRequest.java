package com.Securegate.Securegate.dto;

import lombok.Data;

@Data
public class UpdatePasswordRequest {

    private String email;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}