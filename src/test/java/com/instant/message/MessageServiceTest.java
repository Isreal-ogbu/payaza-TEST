package com.instant.message;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.sns.AmazonSNS;
import com.instant.message.dto.MessageDto;
import com.instant.message.exception.MessageProcessingException;
import com.instant.message.service.MessageService;
import com.instant.message.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MessageServiceTest {
    @Mock
    private Table messagesTable;
    @Mock
    private AmazonSNS snsClient;
    @Mock
    private JwtUtil jwtUtil;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtUtil.validateTokenAndGetUserId(any())).thenReturn("user1");
    }

    @Test
    public void sendMessage_ValidInput_ReturnsMessage() throws Exception {
        MessageDto messageDto = new MessageDto();
        messageDto.setRecipientId("user2");
        messageDto.setContent("Hello");

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        doNothing().when(messagesTable).putItem(itemCaptor.capture());

        var result = messageService.sendMessage(messageDto, "validToken");

        assertNotNull(result);
        assertEquals("user1", result.getSenderId());
        assertEquals("user2", result.getRecipientId());
        assertEquals("Hello", result.getContent());

        Item savedItem = itemCaptor.getValue();
        assertEquals("user1", savedItem.getString("senderId"));
        assertEquals("user2", savedItem.getString("recipientId"));

        verify(snsClient).publish(any(), any());
    }

    @Test
    public void sendMessage_InvalidToken_ThrowsException() {
        when(jwtUtil.validateTokenAndGetUserId(any())).thenThrow(new RuntimeException("Invalid token"));

        MessageDto messageDto = new MessageDto();
        messageDto.setRecipientId("user2");
        messageDto.setContent("Hello");

        assertThrows(MessageProcessingException.class, () -> {
            messageService.sendMessage(messageDto, "invalidToken");
        });
    }
}
