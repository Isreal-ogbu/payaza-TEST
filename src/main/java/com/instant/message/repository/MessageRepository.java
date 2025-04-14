package com.instant.message.repository;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.instant.message.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MessageRepository {
    private final DynamoDB dynamoDB;
    @Value("${aws.dynamodb.messagesTable}")
    private String TABLE_NAME;

    public void saveMessage(Message message) {
        dynamoDB.getTable(TABLE_NAME)
                .putItem(new Item()
                        .withPrimaryKey("messageId", message.getMessageId())
                        .withString("senderId", message.getSenderId())
                        .withString("recipientId", message.getRecipientId())
                        .withString("content", message.getContent())
                        .withNumber("timestamp", message.getTimestamp())
                        .withString("conversationId", message.getConversationId()));
    }

    public List<Message> getMessagesByConversation(String conversationId, int limit) {
        List<Message> messages = new ArrayList<>();

        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("conversationId = :cid")
                .withValueMap(new ValueMap().withString(":cid", conversationId))
                .withScanIndexForward(false)
                .withMaxResultSize(limit);

        ItemCollection<QueryOutcome> items = dynamoDB.getTable(TABLE_NAME)
                .getIndex("ConversationIndex")
                .query(spec);

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            messages.add(mapItemToMessage(item));
        }

        return messages;
    }

    private Message mapItemToMessage(Item item) {
        Message message = new Message();
        message.setMessageId(item.getString("messageId"));
        message.setSenderId(item.getString("senderId"));
        message.setRecipientId(item.getString("recipientId"));
        message.setContent(item.getString("content"));
        message.setTimestamp(item.getLong("timestamp"));
        message.setConversationId(item.getString("conversationId"));
        return message;
    }
}