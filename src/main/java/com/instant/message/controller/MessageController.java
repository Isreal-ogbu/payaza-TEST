package com.instant.message.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instant.message.dto.MessageDto;
import com.instant.message.exception.MessageProcessingException;
import com.instant.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String path = request.getPath();
            String httpMethod = request.getHttpMethod();

            if ("/messages".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
                return handleSendMessage(request);
            } else if ("/messages".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
                return handleGetMessages(request);
            } else {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404)
                        .withBody("{\"message\":\"Endpoint not found\"}");
            }
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"message\":\"Internal server error\"}");
        }
    }
    @PostMapping(value = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE , produces = MediaType.APPLICATION_JSON_VALUE)
    private APIGatewayProxyResponseEvent handleSendMessage(APIGatewayProxyRequestEvent request)
            throws Exception {
        try {
            String authToken = extractAuthToken(request.getHeaders());
            MessageDto messageDto = objectMapper.readValue(request.getBody(), MessageDto.class);
            var message = messageService.sendMessage(messageDto, authToken);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withBody(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(objectMapper.writeValueAsString("Message failed: " + e.getMessage()));
        }
    }
    @GetMapping(value = "/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    private APIGatewayProxyResponseEvent handleGetMessages(APIGatewayProxyRequestEvent request)
            throws Exception {
        try {
            String authToken = extractAuthToken(request.getHeaders());
            Map<String, String> queryParams = request.getQueryStringParameters() != null ?
                    request.getQueryStringParameters() : Collections.emptyMap();

            String conversationId = queryParams.get("conversationId");
            String limitParam = queryParams.getOrDefault("limit", "50");
            int limit = Integer.parseInt(limitParam);

            var messages = messageService.getMessages(conversationId, authToken, limit);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(messages));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(objectMapper.writeValueAsString("Message failed: " + e.getMessage()));
        }
    }

    private String extractAuthToken(Map<String, String> headers) {
        if (headers == null || !headers.containsKey("Authorization")) {
            throw new MessageProcessingException("Missing authorization header");
        }

        String authHeader = headers.get("Authorization");
        if (!authHeader.startsWith("Bearer ")) {
            throw new MessageProcessingException("Invalid authorization format");
        }

        return authHeader.substring(7);
    }
}