package com.instant.message.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instant.message.dto.AuthRequest;
import com.instant.message.dto.AuthResponse;
import com.instant.message.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import javax.naming.AuthenticationException;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            String path = request.getPath();
            String httpMethod = request.getHttpMethod();

            if ("/auth/register".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
                return handleRegister(request);
            } else if ("/auth/login".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
                return handleLogin(request);
            } else {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(404)
                        .withBody("{\"message\":\"Endpoint not found\"}");
            }
        } catch (AuthenticationException e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("{\"message\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"message\":\"Internal server error\"}");
        }
    }

    private APIGatewayProxyResponseEvent handleRegister(APIGatewayProxyRequestEvent request)
            throws Exception {
        AuthRequest authRequest = objectMapper.readValue(request.getBody(), AuthRequest.class);
        AuthResponse response = authService.registerUser(authRequest);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withBody(objectMapper.writeValueAsString(response));
    }

    private APIGatewayProxyResponseEvent handleLogin(APIGatewayProxyRequestEvent request)
            throws Exception {
        AuthRequest authRequest = objectMapper.readValue(request.getBody(), AuthRequest.class);
        AuthResponse response = authService.loginUser(authRequest);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(objectMapper.writeValueAsString(response));
    }
}