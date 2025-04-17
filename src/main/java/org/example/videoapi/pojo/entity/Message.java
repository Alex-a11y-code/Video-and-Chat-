package org.example.videoapi.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Message {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private String type; // "text" 或 "image"
    private LocalDateTime timestamp;
    private Boolean isBlocked;
    private String messageType; // "PRIVATE", "GROUP", 或 "BROADCAST"
    private Long groupId;
    private Boolean isRead;
}