package com.example.qas_backend.post.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.common.entity.StatusCode;
import com.example.qas_backend.common.util.IdWorker;
import com.example.qas_backend.post.dto.NewComment;
import com.example.qas_backend.post.entity.Comment;
import com.example.qas_backend.post.entity.Floor;
import com.example.qas_backend.post.mapper.CommentMapper;
import com.example.qas_backend.post.mapper.FloorMapper;
import com.example.qas_backend.post.mapper.UserMapper;
import com.example.qas_backend.post.service.ICommentService;
import com.example.qas_backend.common.util.RedisUtils;
import com.example.qas_backend.post.dto.PublishComment;
import com.example.qas_backend.common.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评论服务实现类
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final FloorMapper floorMapper;

    private final CommentMapper commentMapper;

    private final RedisUtils redisUtils;

    private final IdWorker idWorker;

    private final TokenUtils tokenUtils;

    @Autowired
    public CommentServiceImpl(UserMapper userMapper, FloorMapper floorMapper, CommentMapper commentMapper, RedisUtils redisUtils, IdWorker idWorker, TokenUtils tokenUtils) {
        this.floorMapper = floorMapper;
        this.commentMapper = commentMapper;
        this.redisUtils = redisUtils;
        this.idWorker = idWorker;
        this.tokenUtils = tokenUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result publishComment(String token, NewComment newComment) {
        //要给哪个楼层发评论
        Long belongFloorId = newComment.getFloorId();
        Floor belongFloor = floorMapper.selectById(belongFloorId);
        if (belongFloor == null) {
            return new Result(true, StatusCode.OK, "发布失败，指定的楼层不存在");
        }
        //封装评论信息
        Comment comment = new Comment();
        Long commentId = idWorker.nextId();
        comment.setCommentId(commentId);
        comment.setBelongFloorId(belongFloorId);
        comment.setCommentNumber(belongFloor.getTotalComments() + 1);
        comment.setUserId(tokenUtils.getUserIdFromToken(token));
        comment.setLikes(0);
        comment.setContent(newComment.getContent());
        LocalDateTime now = LocalDateTime.now();
        comment.setCreateTime(now);
        //将评论加入数据库
        commentMapper.insert(comment);
        floorMapper.addCommentToFloor(belongFloorId);
        return new Result(true, StatusCode.OK, "发布楼层成功", new PublishComment(commentId, comment.getCommentNumber()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteComment(String token, Long commentId) {
        Long userId = tokenUtils.getUserIdFromToken(token);
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            return new Result(false, StatusCode.PARAM_ERROR, "删除失败，指定的评论不存在");
        }
        if (!userId.equals(comment.getUserId())) {
            return new Result(false, StatusCode.ACCESS_ERROR, "删除失败，无权操作");
        }
        commentMapper.deleteById(commentId);
        //楼层的评论数-1
        floorMapper.removeCommentFromFloor(comment.getBelongFloorId());
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @Override
    public Result likeTheComment(String token, Long commentId) {
        Long userId = tokenUtils.getUserIdFromToken(token);
        //有没有赞过
        boolean liked = redisUtils.queryUserIsLike(userId, commentId);
        //还没赞过
        if (!liked) {
            redisUtils.addUserLike(userId, commentId);
            commentMapper.increaseCommentLikes(commentId);
            return new Result(true, StatusCode.OK, "点赞成功");
        }
        return new Result(true, StatusCode.REP_ERROR, "已经给评论点赞，无法再次点赞");
    }

    @Override
    public Result dislikeTheComment(String token, Long commentId) {
        Long userId = tokenUtils.getUserIdFromToken(token);
        //有没有赞过
        boolean liked = redisUtils.queryUserIsLike(userId, commentId);
        //赞过
        if (liked) {
            redisUtils.removeUserLike(userId, commentId);
            commentMapper.decreaseCommentLikes(commentId);
            return new Result(true, StatusCode.OK, "取消点赞成功");
        }
        return new Result(true, StatusCode.REP_ERROR, "尚未给评论点赞，无法取消点赞");
    }
}