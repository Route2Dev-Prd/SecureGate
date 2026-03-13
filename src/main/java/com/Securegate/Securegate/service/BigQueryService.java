package com.Securegate.Securegate.service;

import com.Securegate.Securegate.model.User;
import com.google.cloud.bigquery.*;
import com.google.auth.oauth2.GoogleCredentials;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class BigQueryService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerUser(User user) {

        if (user.getName() == null || user.getName().isEmpty()) {
            return "Name is required";
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return "Email is required";
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Password is required";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            return "Password and Confirm Password must match";
        }

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

        if (!user.getPassword().matches(regex)) {
            return "Password must contain uppercase, lowercase, number and special character";
        }

        try {

            // 🔐 Load credentials JSON
            InputStream credentialsStream =
                    getClass().getClassLoader().getResourceAsStream("securegateauth.json");

            GoogleCredentials credentials =
                    GoogleCredentials.fromStream(credentialsStream);

            BigQuery bigquery = BigQueryOptions.newBuilder()
                    .setCredentials(credentials)
                    .build()
                    .getService();

            String datasetName = "securegate_auth";
            String tableName = "Users";

            String hashedPassword = passwordEncoder.encode(user.getPassword());

            Map<String, Object> row = new HashMap<>();
            row.put("Name", user.getName());
            row.put("Email", user.getEmail());
            row.put("Password", hashedPassword);
            row.put("Age", user.getAge());
            row.put("Height", user.getHeight());
            row.put("Weight", user.getWeight());

            InsertAllResponse response = bigquery.insertAll(
                    InsertAllRequest.newBuilder(datasetName, tableName)
                            .addRow(row)
                            .build()
            );

            if (response.hasErrors()) {
                return "Error inserting data into BigQuery";
            }

            return "User Registered Successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error connecting to BigQuery";
        }
    }
}