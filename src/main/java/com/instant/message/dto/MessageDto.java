package com.instant.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageDto {
    @NotBlank(message = "Recipient ID is required")
    private String recipientId;

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 1000, message = "Message cannot exceed 1000 characters")
    private String content;
}