package com.instant.message.repository;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.instant.message.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DynamoDB dynamoDB;
    @Value("${aws.dynamodb.usersTable}")
    private String TABLE_NAME;

    public void saveUser(User user) {
        dynamoDB.getTable(TABLE_NAME)
                .putItem(new Item()
                        .withPrimaryKey("userId", user.getUserId())
                        .withString("passwordHash", user.getPasswordHash())
                        .withString("username", user.getUsername())
                        .withNumber("createdAt", user.getCreatedAt())
                        .withNumber("lastLogin", user.getLastLogin()));
    }

    public User getUserByUsername(String username) {
        Item item = dynamoDB.getTable(TABLE_NAME).getItem("username", username);
        if (item == null) return null;

        return User.builder()
                .userId(item.getString("userId"))
                .passwordHash(item.getString("passwordHash"))
                .username(item.getString("username"))
                .lastLogin(item.getLong("createdAt"))
                .createdAt(item.getLong("createdAt"))
                .build();
    }
}
