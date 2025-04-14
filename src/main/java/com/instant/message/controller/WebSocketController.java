package com.instant.message.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.instant.message.dto.WebSocketResponse;
import com.instant.message.model.Message;
import com.instant.message.service.WebSocketService;
import com.instant.message.util.JwtUtil;
import com.instant.message.util.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.util.Set;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class WebSocketController implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {
    private final WebSocketService webSocketService;
    private final JwtUtil jwtUtil;
    private final Gson gson;


    public APIGatewayV2WebSocketResponse handleConnect(APIGatewayV2WebSocketEvent request, Context context) {
        try {
            String connectionId = request.getRequestContext().getRequestId();
            String authToken = request.getQueryStringParameters().get("token");
            String userId = jwtUtil.validateTokenAndGetUserId(authToken);
            WebSocketSessionManager.addSession(connectionId, userId);

            webSocketService.handleConnect(connectionId, userId);
            return WebSocketResponse.response(200, "{\"message\":\"Success\"}");
        } catch (Exception e) {
            return WebSocketResponse.response(500, "{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    public APIGatewayV2WebSocketResponse handleDisconnect(APIGatewayV2WebSocketEvent request, Context context) {
        String connectionId = request.getRequestContext().getRequestId();
        String authToken = request.getQueryStringParameters().get("token");
        jwtUtil.validateTokenAndGetUserId(authToken);
        WebSocketSessionManager.removeSession(connectionId);
        webSocketService.handleDisconnect(connectionId);
        return WebSocketResponse.response(200, "{\"message\":\"Success\"}");
    }

    public APIGatewayV2WebSocketResponse handleDefault(APIGatewayV2WebSocketEvent request, Context context) {
        try {
            String connectionId = request.getRequestContext().getRequestId();
            String userId = WebSocketSessionManager.getUserForConnection(connectionId);
            String messageBody = request.getBody();
            JsonNode messageJson = new ObjectMapper().readTree(messageBody);
            String messageType = messageJson.path("type").asText();
            switch (messageType) {
                case "CHAT_MESSAGE":
                    handleChatMessage(userId, messageJson);
                    break;
                default:
                    return WebSocketResponse.response(200, "{\"message\":\"Success\"}");
            }

            return WebSocketResponse.response(200, "{\"message\":\"Success\"}");

        } catch (Exception e) {
            context.getLogger().log("Error processing message: " + e.getMessage());
            return WebSocketResponse.response(500, "{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleChatMessage(String senderId, JsonNode messageJson) {
        String recipientId = messageJson.path("recipientId").asText();
        String content = messageJson.path("content").asText();
        String conversationId = messageJson.path("conversationId").asText();

        Message message = new Message();
        message.setMessageId(UUID.randomUUID().toString());
        message.setSenderId(senderId);
        message.setRecipientId(recipientId);
        message.setContent(content);
        message.setTimestamp(System.currentTimeMillis());
        message.setConversationId(conversationId);

        webSocketService.sendMessageToUser(recipientId, message);
        webSocketService.broadcastMessage(senderId, message, Set.of());
    }


    @Override
    public APIGatewayV2WebSocketResponse handleRequest(APIGatewayV2WebSocketEvent input, Context context) {
        String routeKey = input.getRequestContext().getRouteKey();
        try {
            switch (routeKey) {
                case "$connect":
                    return handleConnect(input, context);
                case "$disconnect":
                    return handleDisconnect(input, context);
                case "$default":
                    return handleDefault(input, context);
                default:
                    return WebSocketResponse.response(200, "{\"message\":\"Success\"}");

            }
        } catch (Exception e) {
            return WebSocketResponse.response(500, "{\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}