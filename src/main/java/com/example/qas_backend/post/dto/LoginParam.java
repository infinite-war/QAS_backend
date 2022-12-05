package com.example.qas_backend.post.dto;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 登录参数
 */
@Data
public class LoginParam {

    //用户Id
    private String userId;

    //密码
    @NotBlank(message = "密码不能为空")
    @Length(min = 6)
    private String password;

}