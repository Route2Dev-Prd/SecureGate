package com.Securegate.Securegate.controller;

import com.Securegate.Securegate.model.User;
import com.Securegate.Securegate.service.BigQueryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private BigQueryService bigQueryService;

    @PostMapping("/register")
    public String register(@RequestBody User user) throws InterruptedException {
        return bigQueryService.registerUser(user);
    }
}