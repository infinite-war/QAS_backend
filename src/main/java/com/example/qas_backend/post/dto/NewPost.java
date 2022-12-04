package com.example.qas_backend.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 发表问题
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewPost {


    //标题
    @NotBlank
    @Length(max = 30)
    private String title;

    //问题描述

    @Length() //default 2147483647
    private String content;

}
