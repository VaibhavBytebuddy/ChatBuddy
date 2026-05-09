package com.om.ChatBuddy.chat;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

public class MessageDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MessageInDto {
        String content;
        String sender;
        MessageType messageType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MessageOutDto {
        String id;
        String content;
        String sender;
        MessageType messageType;
        LocalDateTime createdAt;
    }
}