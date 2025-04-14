package com.instant.message.service;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instant.message.exception.MessageProcessingException;
import com.instant.message.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import com.instant.message.util.WebSocketSessionManager;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.GoneException;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final  ApiGatewayManagementApiClient apiGatewayClient;
    private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;

    public void handleConnect(String connectionId, String userId) {
        try {
            dynamoDB.getTable("WebSocketConnections").putItem(new Item()
                    .withPrimaryKey("connectionId", connectionId)
                    .withString("userId", userId)
                    .withNumber("connectedAt", System.currentTimeMillis()));
            WebSocketSessionManager.addSession(connectionId, userId);
        } catch (Exception e) {
            throw new MessageProcessingException("Failed to handle connection", e);
        }
    }

    public void handleDisconnect(String connectionId) {
        try {
            dynamoDB.getTable("WebSocketConnections").deleteItem("connectionId", connectionId);
            WebSocketSessionManager.removeSession(connectionId);
        } catch (Exception e) {
            throw new MessageProcessingException("Failed to handle disconnection", e);
        }
    }

    @Async
    public void sendMessageToUser(String userId, Message message) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            Set<String> connections = WebSocketSessionManager.getConnectionsForUser(userId);

            for (String connectionId : connections) {
                try {

                    PostToConnectionRequest request = PostToConnectionRequest.builder()
                            .connectionId(connectionId)
                            .data(SdkBytes.fromByteArray(messageJson.getBytes()))
                            .build();

                    apiGatewayClient.postToConnection(request);
                } catch (GoneException e) {
                    WebSocketSessionManager.removeSession(connectionId);
                    dynamoDB.getTable("WebSocketConnections").deleteItem("connectionId", connectionId);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    @Async
    public void broadcastMessage(String user, Message message, Set<String> excludedUserIds) {
        try {
            String messageJson = objectMapper.writeValueAsString(message);
            Set<String> allConnections = WebSocketSessionManager.getConnectionsForUser(user);

            for (String connectionId : allConnections) {
                String userId = WebSocketSessionManager.getUserForConnection(connectionId);
                if (excludedUserIds != null && excludedUserIds.contains(userId)) {
                    continue;
                }

                try {
                    PostToConnectionRequest request = PostToConnectionRequest.builder()
                            .connectionId(connectionId)
                            .data(SdkBytes.fromByteArray(messageJson.getBytes()))
                            .build();

                    apiGatewayClient.postToConnection(request);
                } catch (GoneException e) {
                    WebSocketSessionManager.removeSession(connectionId);
                    dynamoDB.getTable("WebSocketConnections").deleteItem("connectionId", connectionId);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }
}