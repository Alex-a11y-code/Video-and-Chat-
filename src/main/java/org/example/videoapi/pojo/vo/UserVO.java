package org.example.videoapi.pojo.vo;

import lombok.Data;
import org.example.videoapi.pojo.entity.User;

@Data
public class UserVO  {
    private Long id;
    private String username;
    private String avatar;
}
