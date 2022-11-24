package com.example.qas_backend.post.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.post.entity.Comment;
import com.example.qas_backend.post.entity.User;
import com.example.qas_backend.post.mapper.CommentMapper;
import com.example.qas_backend.post.mapper.PostMapper;
import com.example.qas_backend.post.mapper.UserMapper;
import com.example.qas_backend.post.service.IAdminService;

public class AdminServiceImpl extends ServiceImpl<UserMapper, User>  implements IAdminService {

    private UserMapper userMapper;
    private PostMapper postMapper;

    @Override
    public Result getAllUsers() {
        return null;
    }

    @Override
    public Result getAllPosts() {
        return null;
    }
}
