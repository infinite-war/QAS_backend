package com.example.qas_backend.post.dto;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * 修改用户信息参数
 */
@Data
public class UserModification {

    //手机号
    @NotBlank
    @Length(max = 20)
    private String phone;

    //邮箱
    @NotBlank
    @Length(max = 30)
    private String email;

    //简介
    @Length(max = 200)
    private String introduction;

}
