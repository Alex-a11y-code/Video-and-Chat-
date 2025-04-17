package org.example.videoapi.pojo.entity;
import lombok.Data;

import java.util.Date;

@Data
public class Follow {
    private Long id;
    private Long userId;
    private Long followUserId;
    private Date createTime;
}
