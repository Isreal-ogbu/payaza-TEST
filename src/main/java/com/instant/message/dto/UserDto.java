package com.instant.message.dto;

public class UserDto {
    private String userId;
    private String username;
    private String email;

    // Constructors
    public UserDto() {}

    public UserDto(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public UserDto(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}