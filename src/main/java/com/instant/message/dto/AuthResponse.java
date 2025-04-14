package com.instant.message.dto;

public class AuthResponse {
    private String token;
    private String userId;
    private String username;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }

    public AuthResponse(String token, String userId, String username) {
        this.token = token;
        this.userId = userId;
        this.username = username;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}