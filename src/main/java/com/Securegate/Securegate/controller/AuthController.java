package com.Securegate.Securegate.controller;

import com.Securegate.Securegate.dto.LoginRequest;
import com.Securegate.Securegate.dto.RegisterRequest;
import com.Securegate.Securegate.dto.UpdatePasswordRequest;
import com.Securegate.Securegate.dto.UserResponse;
import com.Securegate.Securegate.service.BigQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private BigQueryService service;

    // ================= REGISTER =================
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return service.registerUser(request);
    }

    // ================= LOGIN =================
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return service.loginUser(request);
    }

    // ================= GET USER =================
    @GetMapping("/user")
    public UserResponse getUser(@RequestParam String email) {
        return service.getUserDetails(email);
    }
    // ================= UPDATE PASSWORD =================
    @PostMapping("/updatePassword")
    public String updatePassword(@RequestBody UpdatePasswordRequest request) {
        return service.updatePassword(request);
    }
}