package com.aiflow.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
}
