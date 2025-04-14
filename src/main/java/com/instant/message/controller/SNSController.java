package com.instant.message.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instant.message.util.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.model.PostToConnectionRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SNSController implements RequestHandler<SNSEvent, Void> {
    private final  ApiGatewayManagementApiClient apiGatewayClient;

    @Override
    public Void handleRequest(SNSEvent snsEvent, Context context) {
        handleNotification(snsEvent, context);
        return null;
    }

    public void handleNotification(SNSEvent event, Context context) {
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            String message = record.getSNS().getMessage();
            handleNotification(message);
        }
    }

    public void handleNotification(String message) {
        try {
            JsonNode messageJson = new ObjectMapper().readTree(message);
            String recipientId = messageJson.path("recipientId").asText();
            for (String connectionId : WebSocketSessionManager.getConnectionsForUser(recipientId)) {
                try {
                    PostToConnectionRequest request = PostToConnectionRequest.builder()
                            .connectionId(connectionId)
                            .data(SdkBytes.fromByteArray(message.getBytes()))
                            .build();
                    apiGatewayClient.postToConnection(request);
                } catch (Exception e) {
                    WebSocketSessionManager.removeSession(connectionId);
                }
            }
        }catch (Exception ignored){
            log.error(ignored.getMessage());
        }
    }
}
