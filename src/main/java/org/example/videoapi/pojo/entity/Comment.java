package org.example.videoapi.pojo.entity;
import lombok.Data;
import java.util.Date;

@Data
public class Comment {
    private Long videoCommentId;
    private String content;
    private boolean isDelete;
    private Long userId;
    private Long videoId;
    private Long commentLikeCount;
    private Long rootCommentId;
    private Long toCommentId;
    private Date createTime;
    private Date updateTime;




}
