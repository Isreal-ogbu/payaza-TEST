package com.instant.message.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import com.instant.message.dto.AuthRequest;
import com.instant.message.dto.AuthResponse;
import com.instant.message.model.User;
import com.instant.message.repository.UserRepository;
import com.instant.message.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AWSCognitoIdentityProvider cognitoClient;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    @Value("${aws.cognito.userPoolId}")
    private String USER_POOL_ID;
    @Value("${aws.cognito.clientId}")
    private String CLIENT_ID;


    public AuthResponse registerUser(AuthRequest request) throws AuthenticationException {
        try {
            SignUpRequest signUpRequest = new SignUpRequest()
                    .withClientId(CLIENT_ID)
                    .withUsername(request.getUsername())
                    .withPassword(request.getPassword())
                    .withUserAttributes(
                            new AttributeType().withName("email").withValue(request.getUsername()));

            User user = User.builder()
                    .username(request.getUsername())
                    .passwordHash(request.getPassword().getBytes().toString())
                    .lastLogin(new Date().getTime())
                    .lastLogin(new Date().getTime())
                    .userId(UUID.randomUUID().toString())
                    .build();

            userRepository.saveUser(user); // persist user here
            cognitoClient.signUp(signUpRequest);

            AdminConfirmSignUpRequest confirmRequest = new AdminConfirmSignUpRequest()
                    .withUserPoolId(USER_POOL_ID)
                    .withUsername(request.getUsername());

            cognitoClient.adminConfirmSignUp(confirmRequest);

            return loginUser(request);
        } catch (Exception e) {
            throw new AuthenticationException("Registration failed: " + e.getMessage());
        }
    }

    public AuthResponse loginUser(AuthRequest request) throws AuthenticationException {
        try {
            InitiateAuthRequest authRequest = new InitiateAuthRequest()
                    .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .withClientId(CLIENT_ID)
                    .addAuthParametersEntry("USERNAME", request.getUsername())
                    .addAuthParametersEntry("PASSWORD", request.getPassword());

            cognitoClient.initiateAuth(authRequest);
            String userId = getUserId(request.getUsername());
            String token = jwtUtil.generateToken(userId);
            return new AuthResponse(token, userId);
        } catch (Exception e) {
            throw new AuthenticationException("Login failed: " + e.getMessage());
        }
    }

    private String getUserId(String username) {
        return  userRepository.getUserByUsername(username).getUserId();
    }
}