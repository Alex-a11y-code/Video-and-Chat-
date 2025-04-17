package org.example.videoapi.pojo.entity;

import lombok.Data;

@Data
public class CommentLikes {
    private Long id;
    private Long userId;
    private Long videoCommentId;
}
