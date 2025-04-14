package com.instant.message.dto;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;

public class WebSocketResponse {

    public static APIGatewayV2WebSocketResponse response(int code, String message) {
        APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
        response.setStatusCode(code);
        response.setBody(message);
        return response;
    }
}
