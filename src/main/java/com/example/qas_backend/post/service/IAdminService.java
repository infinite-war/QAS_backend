package com.example.qas_backend.post.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.post.entity.User;

/**
 * 管理员服务接口
 */
public interface IAdminService extends IService<User> {

    //获取用户记录,并分页
    Result getAllUsers();

    //获取文章信息，并分页
    Result getAllPosts();

}
