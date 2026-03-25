package com.Securegate.Securegate.service;

import com.Securegate.Securegate.dto.LoginRequest;
import com.Securegate.Securegate.dto.RegisterRequest;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.bigquery.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class BigQueryService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    private BigQuery getBigQuery() throws Exception {
        InputStream credentialsStream =
                getClass().getClassLoader().getResourceAsStream("securegateauth.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

        return BigQueryOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }

    // ========================= REGISTER =========================
    public String registerUser(RegisterRequest request) {

        if (request.getName() == null || request.getName().isEmpty()) {
            return "Name is required";
        }

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return "Email is required";
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return "Password is required";
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return "Password mismatch";
        }

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

        if (!request.getPassword().matches(regex)) {
            return "Weak password";
        }

        try {

            BigQuery bigquery = getBigQuery();

            String datasetName = "securegate_auth";
            String tableName = "Users";

            // 🔹 Check if email already exists
            String checkQuery = "SELECT Email FROM `securegate_auth.Users` WHERE Email='"
                    + request.getEmail() + "' LIMIT 1";

            TableResult checkResult = bigquery.query(
                    QueryJobConfiguration.newBuilder(checkQuery).build()
            );

            if (checkResult.getTotalRows() > 0) {
                return "User already exists";
            }

            // 🔹 Hash password
            String hashedPassword = passwordEncoder.encode(request.getPassword());

            Map<String, Object> row = new HashMap<>();
            row.put("Name", request.getName());
            row.put("Email", request.getEmail());
            row.put("Password", hashedPassword);
            row.put("Age", request.getAge());
            row.put("Height", request.getHeight());
            row.put("Weight", request.getWeight());

            InsertAllResponse response = bigquery.insertAll(
                    InsertAllRequest.newBuilder(datasetName, tableName)
                            .addRow(row)
                            .build()
            );

            if (response.hasErrors()) {
                return "Error inserting data";
            }

            return "User Registered Successfully";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during registration";
        }
    }

    // ========================= LOGIN =========================
    public String loginUser(LoginRequest request) {

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return "Email required";
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return "Password required";
        }

        try {

            BigQuery bigquery = getBigQuery();

            String query = "SELECT * FROM `securegate_auth.Users` WHERE Email='"
                    + request.getEmail() + "' LIMIT 1";

            TableResult result = bigquery.query(
                    QueryJobConfiguration.newBuilder(query).build()
            );

            for (FieldValueList row : result.iterateAll()) {

                String storedPassword = row.get("Password").getStringValue();

                if (passwordEncoder.matches(request.getPassword(), storedPassword)) {
                    return "Login Successful";
                } else {
                    return "Invalid Password";
                }
            }

            return "User Not Found";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during login";
        }
    }
}