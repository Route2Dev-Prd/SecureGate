package com.Securegate.Securegate.service;

import com.Securegate.Securegate.dto.LoginRequest;
import com.Securegate.Securegate.dto.RegisterRequest;
import com.Securegate.Securegate.dto.UpdatePasswordRequest;
import com.Securegate.Securegate.dto.UserResponse;
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

    // ========================= COMMON BIGQUERY CONNECTION =========================
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

            // 🔹 Check if user already exists
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

    // ========================= GET USER DETAILS =========================
    public UserResponse getUserDetails(String email) {

        if (email == null || email.isEmpty()) {
            return null;
        }

        try {

            BigQuery bigquery = getBigQuery();

            String query = "SELECT Name, Email, Age, Height, Weight FROM `securegate_auth.Users` WHERE Email='"
                    + email + "' LIMIT 1";

            TableResult result = bigquery.query(
                    QueryJobConfiguration.newBuilder(query).build()
            );

            for (FieldValueList row : result.iterateAll()) {

                UserResponse user = new UserResponse();

                user.setName(row.get("Name").getStringValue());
                user.setEmail(row.get("Email").getStringValue());
                user.setAge((int) row.get("Age").getLongValue());
                user.setHeight(row.get("Height").getDoubleValue());
                user.setWeight(row.get("Weight").getDoubleValue());

                return user;
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // ================= UPDATE PASSWORD =================
    public String updatePassword(UpdatePasswordRequest request) {

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            return "Email required";
        }

        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            return "Old password required";
        }

        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return "New password required";
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return "Password mismatch";
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

                // 🔐 old password verify
                if (!passwordEncoder.matches(request.getOldPassword(), storedPassword)) {
                    return "Old password is incorrect";
                }

                // 🔐 new password hash
                String newHashedPassword = passwordEncoder.encode(request.getNewPassword());

                // 🔹 UPDATE QUERY
                String updateQuery = "UPDATE `securegate_auth.Users` SET Password='"
                        + newHashedPassword + "' WHERE Email='" + request.getEmail() + "'";

                bigquery.query(QueryJobConfiguration.newBuilder(updateQuery).build());

                return "Password updated successfully";
            }

            return "User not found";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error updating password";
        }
    }
}