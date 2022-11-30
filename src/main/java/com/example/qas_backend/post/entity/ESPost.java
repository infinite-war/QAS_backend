package com.example.qas_backend.post.entity;

import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @author :infinite-war
 * @date : 2022/11/30 15:45
 * @desc :
 */

// elasticsearchs中的Post实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESPost {

    public ESPost(Post post){
        this.postId=post.getPostId();
        this.title=post.getTitle();
        this.content=post.getContent();
        this.userId=post.getUserId();
        this.views=post.getViews();
        this.likes=post.getLikes();
        this.createTime= Date.from(post.getCreateTime().atZone(ZoneId.systemDefault()).toInstant());
        this.updateTime=Date.from(post.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant());
        this.floors=post.getFloors();
        this.totalFloors=post.getTotalFloors();
    }


    public ESPost(ESPost post){
        this.postId=post.getPostId();
        this.title=post.getTitle();
        this.content=post.getContent();
        this.userId=post.getUserId();
        this.views=post.getViews();
        this.likes=post.getLikes();
        this.createTime= post.createTime;
        this.updateTime=post.updateTime;
        this.floors=post.getFloors();
        this.totalFloors=post.getTotalFloors();
    }


    //帖子ID
    @TableId(value = "post_id", type = IdType.INPUT)
    private Long postId;

    //标题
    private String title;

    //内容
    private String content;

    //用户ID
    private Long userId;

    //浏览量
    private Long views;

    //点赞数
    private Integer likes;

    //发布时间
    private Date createTime;

    //最后回复时间
    private Date updateTime;

    //当前楼层数
    private Integer floors;

    //历史楼层数
    private Integer totalFloors;


}
