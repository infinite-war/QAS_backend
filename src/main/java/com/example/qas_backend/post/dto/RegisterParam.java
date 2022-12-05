package com.example.qas_backend.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterParam {
    //用户名
    @NotBlank(message = "用户名不能为空")
    @Length(max = 20)
    private String username;


    //密码
    @NotBlank(message = "密码不能为空")
    @Length(min = 6)
    private String password;
}
