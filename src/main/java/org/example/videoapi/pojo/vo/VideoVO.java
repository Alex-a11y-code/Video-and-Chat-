package org.example.videoapi.pojo.vo;

import lombok.Data;
import org.example.videoapi.pojo.entity.Video;

import java.time.LocalDateTime;

@Data
public class VideoVO  {
    private Long videoId;
    private Long userId;
    private String title;
    private String username;
    private String introduction;
    private Long viewsCount;
    private Long likesCount;
    private String surfacePicture;
    private String videoAddress;
    private String status;
    private Long reviewerId;
    private LocalDateTime reviewTime;
    private LocalDateTime createTime;
    private String category;


}
