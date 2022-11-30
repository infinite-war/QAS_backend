package com.example.qas_backend.post.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息概要视图对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOutline {

    //用户ID
    private Long userId;

    //角色
    private String role;

    //生成的token
    private String token;

    public UserOutline(Long userId, String role) {
        this.userId = userId;
        this.role=role;
    }
}
