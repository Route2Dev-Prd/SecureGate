package com.Securegate.Securegate.controller;

import com.Securegate.Securegate.dto.LoginRequest;
import com.Securegate.Securegate.dto.RegisterRequest;
import com.Securegate.Securegate.model.User;
import com.Securegate.Securegate.service.BigQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private BigQueryService service;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return service.registerUser(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return service.loginUser(request);
    }
}