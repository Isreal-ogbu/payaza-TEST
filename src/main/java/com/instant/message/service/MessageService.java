package com.instant.message.service;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.instant.message.dto.MessageDto;
import com.instant.message.exception.MessageProcessingException;
import com.instant.message.model.Message;
import com.instant.message.repository.MessageRepository;
import com.instant.message.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final AmazonSNS snsClient;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final MessageRepository messageRepository;

    @Value("${aws.sns.topicArn}")
    private String NOTIFICATION_TOPIC_ARN;

    public Message sendMessage(MessageDto messageDto, String authToken) {
        try {
            String senderId = jwtUtil.validateTokenAndGetUserId(authToken);

            Message message = new Message();
            message.setMessageId(UUID.randomUUID().toString());
            message.setSenderId(senderId);
            message.setRecipientId(messageDto.getRecipientId());
            message.setContent(messageDto.getContent());
            message.setTimestamp(new Date().getTime());
            message.setConversationId(generateConversationId(senderId, messageDto.getRecipientId()));

            messageRepository.saveMessage(message);
            String messageJson = objectMapper.writeValueAsString(message);
            snsClient.publish(NOTIFICATION_TOPIC_ARN, messageJson);

            return message;
        } catch (Exception e) {
            throw new MessageProcessingException("Failed to send message: " + e.getMessage(), e);
        }
    }

    private String generateConversationId(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    public String getMessages(String conversationId, String authToken,  int limit) {
        try {
            List<Message> messages = messageRepository.getMessagesByConversation(conversationId, limit);
            return objectMapper.writeValueAsString(messages);
        } catch (Exception e) {
            return "{}";
        }
    }
}