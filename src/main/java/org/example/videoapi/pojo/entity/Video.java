package org.example.videoapi.pojo.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Video {
    private Long videoId;
    private Long userId;
    private String title;
    private String introduction;
    private Long viewsCount;
    private Long likesCount;
    private String surfacePicture;
    private String videoAddress;
    private String status;// PENDING, APPROVED, REJECTED
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private String category;
}
