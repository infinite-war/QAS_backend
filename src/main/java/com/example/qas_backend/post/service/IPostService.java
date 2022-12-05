package com.example.qas_backend.post.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.post.dto.NewPost;
import com.example.qas_backend.post.dto.PagingParam;
import com.example.qas_backend.post.dto.SearchParam;
import com.example.qas_backend.post.entity.Post;

import java.io.IOException;

/**
 * 帖子服务接口
 */
public interface IPostService extends IService<Post> {

    //发表帖子
    Result publishPost(String token, NewPost newPost) throws IOException;

    //删除帖子
    Result deletePost(String token, Long postId) throws IOException;

    //是否赞过
    Result likedThePost(String token, Long postId);

    //给帖子点赞
    Result likeThePost(String token, Long postId) throws IOException;

    //给帖子取消赞
    Result dislikeThePost(String token, Long postId) throws IOException;

    //获取帖子详情页
    Result getPostDetail(String token, Long postId, PagingParam pagingParam);

    //获取帖子列表
    Result getPostList(SearchParam searchParam, PagingParam pagingParam) throws IOException;

    //获取用户自己发布的帖子
    Result getMyPosts(String token,SearchParam searchParam,PagingParam pagingParam) throws IOException;

    //随机获取10个帖子
    Result getRandomPosts() throws IOException;
}
