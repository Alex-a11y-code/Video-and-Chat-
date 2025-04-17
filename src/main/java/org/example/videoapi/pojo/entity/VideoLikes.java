package org.example.videoapi.pojo.entity;

import lombok.Data;

@Data
public class VideoLikes {
    private Long id;
    private Long userId;
    private Long videoId;
}
