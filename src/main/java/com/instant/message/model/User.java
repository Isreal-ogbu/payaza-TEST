package com.instant.message.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Builder;
import lombok.Setter;

import java.util.Date;

@Setter
@DynamoDBTable(tableName = "Users")
@Builder
public class User {
    private String userId;
    private String username;
    private String passwordHash;
    private long createdAt;
    private long lastLogin;

    @DynamoDBAttribute
    public String getUserId() {
        return userId;
    }
    @DynamoDBAttribute
    public String getUsername() {
        return username;
    }
    @DynamoDBAttribute
    public String getPasswordHash() {
        return passwordHash;
    }
    @DynamoDBAttribute
    public long getCreatedAt() {
        return createdAt;
    }
    @DynamoDBAttribute
    public long getLastLogin() {
        return lastLogin;
    }
}