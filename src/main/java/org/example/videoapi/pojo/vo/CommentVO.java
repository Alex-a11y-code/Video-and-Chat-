package org.example.videoapi.pojo.vo;

import lombok.Data;
import org.example.videoapi.pojo.entity.Comment;
import java.util.List;
@Data
public class CommentVO {
    private Long videoCommentId;
    private Long childCount;
    private List<Comment> childComments;
}
