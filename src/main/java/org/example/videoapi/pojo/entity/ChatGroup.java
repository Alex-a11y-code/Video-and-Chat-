package org.example.videoapi.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatGroup {
    private Long id;
    private String name;
    private Long creatorId;
    private LocalDateTime createTime;
    private String description;
}
