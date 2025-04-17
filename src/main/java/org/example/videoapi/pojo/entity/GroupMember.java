package org.example.videoapi.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupMember {
    private Long id;
    private Long groupId;
    private Long userId;
    private LocalDateTime joinTime;

}
