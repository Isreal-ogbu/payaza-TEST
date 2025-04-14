package com.instant.message.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.auth0.jwt.algorithms.Algorithm;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;

import java.net.URI;

@Configuration
public class Config {

    @Value("${aws.region}")
    private String awsRegion;
    @Value("${jwt.secret}")
    private String SECRET;
    @Value("${aws.websocket.endpoint}")
    private String API_ENDPOINT;
    @Value("${aws.accessKey}")
    private String ACCESS_KEY;
    @Value("${aws.secretKey}")
    private String SECRET_KEY;

    @Bean
    public AWSCognitoIdentityProvider provider(BasicAWSCredentials creds) {
        return AWSCognitoIdentityProviderClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }

    @Bean
    public BasicAWSCredentials credentials(){
        return new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public AmazonSNS amazonSNS(BasicAWSCredentials creds) {
        return AmazonSNSClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }

    @Bean
    public DynamoDB dynamoDB(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDB(amazonDynamoDB);
    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(awsRegion)
                .withCredentials(new AWSStaticCredentialsProvider(credentials()))
                .build();
    }

    @Bean
    public Algorithm algorithm() {
        return Algorithm.HMAC256(SECRET);
    }

    @Bean
    public JWTVerifier JWTVerifier(Algorithm algorithm) {
        return JWT.require(algorithm).build();
    }

    @Bean
    public ApiGatewayManagementApiClient apiClient(BasicAWSCredentials creds){
        return ApiGatewayManagementApiClient.builder()
                .endpointOverride(URI.create(API_ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();
    }
}
