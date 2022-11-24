package com.example.qas_backend.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.qas_backend.post.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * 评论Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    //点赞操作
    @Update({"update `comment` set `likes`=`likes`+1 where comment_id = #{commentId}"})
    int increaseCommentLikes(Long commentId);

    //取消点赞操作
    @Update({"update `comment` set `likes`=`likes`-1 where comment_id = #{commentId}"})
    int decreaseCommentLikes(Long commentId);

}
