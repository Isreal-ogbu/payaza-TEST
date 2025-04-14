package com.instant.message;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.instant.message.controller.AuthController;
import com.instant.message.controller.MessageController;
import com.instant.message.controller.WebSocketController;
import com.instant.message.service.AuthService;
import com.instant.message.service.MessageService;
import com.instant.message.service.WebSocketService;
import com.instant.message.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.google.gson.Gson;
import org.springframework.stereotype.Component;

@SpringBootApplication
@Component
@RequiredArgsConstructor
public class MessageApplication implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>{

	private final AuthController authController;
	private final MessageController messageController;
	private final Gson gson;

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		String path = input.getPath();
		try {
			if (path.startsWith("/auth")) {
				return authController.handleRequest(input, context);
			} else if (path.startsWith("/messages")) {
				return messageController.handleRequest(input, context);
			}
		} catch (Exception e) {
			return new APIGatewayProxyResponseEvent()
					.withStatusCode(500)
					.withBody("{\"message\":\"" + e.getMessage() + "\"}");
		}
        return new APIGatewayProxyResponseEvent()
				.withStatusCode(500)
				.withBody("{\"message\":\"" +"An error occurred"+ "\"}");
    }

	public static void main(String[] args) {
		SpringApplication.run(MessageApplication.class, args);
	}

}
