package com.example.qas_backend.post.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.post.dto.NewComment;
import com.example.qas_backend.post.dto.PagingParam;
import com.example.qas_backend.post.dto.SearchParam;
import com.example.qas_backend.post.entity.Comment;
import org.elasticsearch.client.core.PageParams;

/**
 * 评论服务接口
 */
public interface ICommentService extends IService<Comment> {

    //发表评论
    Result publishComment(String token, NewComment newComment);

    //删除评论
    Result deleteComment(String token, Long commentId);

    //给评论点赞
    Result likeTheComment(String token, Long commentId);

    //给评论取消赞
    Result dislikeTheComment(String token, Long commentId);

    //查询用户自己发布的子评论
    Result getMyComments(String token, SearchParam searchParam, PagingParam pagingParam);

}
