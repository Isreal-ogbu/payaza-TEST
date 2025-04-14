package com.instant.message.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import lombok.Setter;

import java.util.Date;

@Setter
@DynamoDBTable(tableName = "Messages")
public class Message {
    private String messageId;
    private String senderId;
    private String recipientId;
    private String content;
    private long timestamp;
    private String conversationId;

    @DynamoDBHashKey
    public String getMessageId() { return messageId; }

    @DynamoDBAttribute
    public String getSenderId() { return senderId; }

    @DynamoDBAttribute
    public String getRecipientId() { return recipientId; }

    @DynamoDBAttribute
    public String getContent() { return content; }

    @DynamoDBAttribute
    public long getTimestamp() { return timestamp; }

    @DynamoDBHashKey
    public String getConversationId() {
        return conversationId;
    }


}