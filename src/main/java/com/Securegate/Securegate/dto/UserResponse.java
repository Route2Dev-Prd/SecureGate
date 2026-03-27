package com.Securegate.Securegate.dto;

import lombok.Data;

@Data
public class UserResponse {

    private String name;
    private String email;
    private int age;
    private double height;
    private double weight;
}