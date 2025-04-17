package org.example.videoapi.pojo.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class RegisterRequest {
    private String username;
    private String password;
    private String role;
}