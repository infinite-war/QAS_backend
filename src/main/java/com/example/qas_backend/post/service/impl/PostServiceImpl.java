package com.example.qas_backend.post.service.impl;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qas_backend.common.entity.PageResult;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.common.entity.StatusCode;
import com.example.qas_backend.common.util.*;
import com.example.qas_backend.post.dto.NewPost;
import com.example.qas_backend.post.dto.PagingParam;
import com.example.qas_backend.post.dto.SearchParam;
import com.example.qas_backend.post.entity.*;
import com.example.qas_backend.post.mapper.CommentMapper;
import com.example.qas_backend.post.mapper.FloorMapper;
import com.example.qas_backend.post.mapper.PostMapper;
import com.example.qas_backend.post.mapper.UserMapper;
import com.example.qas_backend.post.service.IPostService;
import com.example.qas_backend.post.dto.PublishPost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子服务实现类
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final FloorMapper floorMapper;
    private final CommentMapper commentMapper;
    private final RedisUtils redisUtils;
    private final IdWorker idWorker;
    private final TokenUtils tokenUtils;
    private final ESopUtils eSopUtils;

    public PostServiceImpl(UserMapper userMapper, PostMapper postMapper, FloorMapper floorMapper, CommentMapper commentMapper, RedisUtils redisUtils, IdWorker idWorker, TokenUtils tokenUtils, ESopUtils eSopUtils) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.floorMapper = floorMapper;
        this.commentMapper = commentMapper;
        this.redisUtils = redisUtils;
        this.idWorker = idWorker;
        this.tokenUtils = tokenUtils;
        this.eSopUtils = eSopUtils;
    }


    @Override
    public Result publishPost(String token, NewPost newPost) throws IOException {
        //将前端传来的贴子信息封装成类
        Post post = new Post();
        Long postId = idWorker.nextId();
        Long userId = tokenUtils.getUserIdFromToken(token);
        post.setPostId(postId);
        post.setTitle(newPost.getTitle());
        post.setContent(newPost.getContent());
        post.setUserId(userId);
        post.setViews(0L);
        post.setLikes(0);
        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        post.setFloors(0);
        post.setTotalFloors(0);
        //在数据库中添加帖子记录
        postMapper.insert(post);
        userMapper.increasePublishedNums(userId);

        //在elasticsearch中添加记录
        if(!eSopUtils.insertESPost(post)){
            return new Result(false, StatusCode.ERROR,"elasticsearch中帖子添加失败");
        }

        return new Result(true, StatusCode.OK, "帖子发布成功", new PublishPost(postId));
    }

    @Override
    @Transactional
    public Result deletePost(String token, Long postId) throws IOException {
        //获取发出删除请求的用户id
        //只有当前id是这个帖子的发布者，才能执行删除操作
        Long userId = tokenUtils.getUserIdFromToken(token);
        User user = userMapper.selectById(userId);
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return new Result(false, StatusCode.PARAM_ERROR, "删除失败，指定的帖子不存在");
        }
        if (!(userId.equals(post.getUserId()) || user.getRole()==1)) {
            return new Result(false, StatusCode.ACCESS_ERROR, "删除失败，无权操作");
        }
        //先清空这个帖子下的所有楼层(删除楼层的过程中还要删除底下的评论)
        List<Floor> floorList = floorMapper.selectList(new QueryWrapper<Floor>().eq("belong_post_id", postId));
        for (Floor floor : floorList) {
            commentMapper.delete(new QueryWrapper<Comment>().eq("belong_floor_id", floor.getFloorId()));
            floorMapper.deleteById(floor);
        }
        //楼层清空后再删除帖子
        postMapper.deleteById(postId);
        userMapper.decreasePublishedNums(userId);
        // es中删除对应的帖子
        if(!eSopUtils.deleteESPost(postId)){
            return new Result(false,StatusCode.ERROR,"elasticsearch中的帖子记录删除失败");
        }

        return new Result(true, StatusCode.OK, "删除成功");
    }

    @Override
    public Result getPostDetail(String token, Long postId, PagingParam pagingParam) {
        //判断用户是否处于属于登录状态
        boolean loginStatus = false;
        Long userId = null;
        //收到的token不为空，说明处于登录状态
        if (token != null) {
            loginStatus = true;
            userId = tokenUtils.getUserIdFromToken(token);
        }
        //获取指定的帖子记录
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return new Result(false, StatusCode.PARAM_ERROR, "获取失败，指定的帖子不存在");
        }
        //每次请求帖子详情页的第一页时，访问量便加一
        if (pagingParam.getPage() == 1) {
            redisUtils.increasePostViews(postId);
        }
        //如果当前处于登录状态，则需要查看一下发出请求的用户有没有给这个帖子点过赞
        if (loginStatus) {
            post.setLiked(redisUtils.queryUserIsLike(userId, postId));
        }
        //加载这个帖子下的楼层
        QueryWrapper<Floor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("belong_post_id", postId);
        WrapperOrderPlugin.addOrderToFloorWrapper(queryWrapper, pagingParam.getOrder());
        //对楼层进行分页处理
        IPage<Floor> page = new Page<>(pagingParam.getPage(), pagingParam.getSize());
        IPage<Floor> result = floorMapper.selectPage(page, queryWrapper);
        List<Floor> floorList = result.getRecords();
        //加载这个帖子下的楼层
        post.setFloorList(floorList);
        for (Floor floor : floorList) {
            floor.setUsername(userMapper.getUserNameById(floor.getUserId()));
            if (loginStatus) {
                floor.setLiked(redisUtils.queryUserIsLike(userId, floor.getFloorId()));
            }
            //每个楼层下面可能还有评论
            //没有出评论分页功能，所以这里展示所有的评论，有多少评论就展示多少
            List<Comment> commentList = commentMapper.selectList(new QueryWrapper<Comment>()
                    .eq("belong_floor_id", floor.getFloorId()).orderByAsc("comment_number"));
            floor.setCommentList(commentList);
            for (Comment comment : commentList) {
                comment.setNickname(userMapper.getUserNameById(comment.getUserId()));
                if (loginStatus) {
                    comment.setLiked(redisUtils.queryUserIsLike(userId, comment.getCommentId()));
                }
            }
        }
        return new Result(true, StatusCode.OK, "获取成功", post);
    }

    //使用es查询
    @Override
    public Result getPostList(SearchParam searchParam, PagingParam pagingParam) throws IOException {

        SearchResponse<ESPost> search = eSopUtils.searchPosts(searchParam, pagingParam);

        PageResult<ESPost> pageResult = new PageResult<>();
        pageResult.createRecords();
        for (Hit<ESPost> hit : search.hits().hits()) {
            System.out.println(hit.source());
            assert hit.source() != null; // 非空断言
            ESPost esPost=new ESPost(hit.source());
            esPost.setTitle(hit.highlight().get("title.ik_max_analyzer").toString().replace("[","").replace("]",""));// 替换为高亮的标题
            pageResult.add(esPost);
        }
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }


// mysql查询
//   @Override
//    public Result getPostList(SearchParam searchParam, PagingParam pagingParam) {
//        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
//        //如果SearchParam存在参数，则加入QueryWrapper中作为查询条件
//        if ((searchParam.getKeyword() != null) && (!searchParam.getKeyword().isBlank())) {
//            queryWrapper.like("title", searchParam.getKeyword());
//        }
//        if (searchParam.getUserId() != null) {
//            queryWrapper.eq("user_id", searchParam.getUserId());
//        }
//        if (searchParam.getCategory() != null) {
//            queryWrapper.eq("category", searchParam.getCategory());
//        }
//        WrapperOrderPlugin.addOrderToPostWrapper(queryWrapper, pagingParam.getOrder());
//        //对帖子进行分页处理
//        IPage<Post> page = new Page<>(pagingParam.getPage(), pagingParam.getSize());
//        IPage<Post> result = postMapper.selectPage(page, queryWrapper);
//        List<Post> postList = result.getRecords();
//        for (Post post : postList) {
//            if (loginStatus) {
//                post.setLiked(redisUtils.queryUserIsLike(userId, post.getPostId()));
//            }
//        }
//        System.out.println(postList);
//        //填充PageResult
//        PageResult<Post> pageResult = new PageResult<>();
//        pageResult.setRecords(postList);
//        pageResult.setTotal(postList.size());
//        return new Result(true, StatusCode.OK, "查询成功", pageResult);
//    }

    @Override
    public Result likedThePost(String token, Long postId){
        Long userId = tokenUtils.getUserIdFromToken(token);
        //查看发请求的人之前是不是赞过这个帖子
        boolean liked = redisUtils.queryUserIsLike(userId, postId);
        //之前没有赞过
        if (!liked) {
            return new Result(false, StatusCode.OK, "未点赞");
        }
        return new Result(true, StatusCode.REP_ERROR, "已经给帖子点赞");
    }

    @Override
    public Result likeThePost(String token, Long postId) throws IOException {
        Long userId = tokenUtils.getUserIdFromToken(token);
        //查看发请求的人之前是不是赞过这个帖子
        boolean liked = redisUtils.queryUserIsLike(userId, postId);
        Long publisherId=postMapper.selectById(postId).getUserId();
        //之前没有赞过
        if (!liked) {
            //向redis中写入点赞的记录
            redisUtils.addUserLike(userId, postId);
            //增加帖子的点赞数
            postMapper.increasePostLikes(postId);
            //贴主获赞数+1
            userMapper.increaseLikesNums(publisherId);

            //更新es中的post记录
            eSopUtils.increaseESPostLikes(postId);

            if(!eSopUtils.updateESPost(postId)){
                return new Result(false,StatusCode.ERROR,"elasticsearch中的帖子记录更新失败");
            }

            return new Result(true, StatusCode.OK, "点赞成功");
        }
        return new Result(false, StatusCode.REP_ERROR, "已经给帖子点赞，无法再次点赞");
    }

    @Override
    public Result dislikeThePost(String token, Long postId) throws IOException {
        Long userId = tokenUtils.getUserIdFromToken(token);
        //查看是不是点过赞
        boolean liked = redisUtils.queryUserIsLike(userId, postId);
        Long publisherId=postMapper.selectById(postId).getUserId();
        //如果赞过
        if (liked) {
            //在redis中删除点赞记录
            redisUtils.removeUserLike(userId, postId);
            postMapper.decreasePostLikes(postId);
            userMapper.decreaseLikesNums(publisherId);
            eSopUtils.decreaseESPostLikes(postId);
            if(!eSopUtils.updateESPost(postId)){
                return new Result(false,StatusCode.ERROR,"elasticsearch中的帖子记录更新失败");
            }
            return new Result(true, StatusCode.OK, "取消点赞成功");
        }
        return new Result(false, StatusCode.REP_ERROR, "尚未给帖子点赞，无法取消点赞");
    }


    // 获取用户自己发布的帖子
    @Override
    public Result getMyPosts(String token,SearchParam searchParam,PagingParam pagingParam){
        Long userId=tokenUtils.getUserIdFromToken(token);
        PageResult<ESPost> pageResult = new PageResult<>();
        try {
            SearchResponse<ESPost> myPosts = eSopUtils.getMyPosts(userId,searchParam,pagingParam);

            pageResult.createRecords();
            for (Hit<ESPost> hit : myPosts.hits().hits()) {
                System.out.println(hit.source());
                assert hit.source() != null; // 非空断言
                ESPost esPost=new ESPost(hit.source());
                pageResult.add(esPost);
            }
        }catch (Exception e){
            return new Result(false,StatusCode.ERROR,"查询失败");
        }
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }

    @Override
    public Result getRandomPosts() throws IOException {

        PageResult<ESPost> pageResult = new PageResult<>();
        try {
            SearchResponse<ESPost> myPosts = eSopUtils.getRandomPosts();

            pageResult.createRecords();
            for (Hit<ESPost> hit : myPosts.hits().hits()) {
                System.out.println(hit.source());
                assert hit.source() != null; // 非空断言
                ESPost esPost=new ESPost(hit.source());
                pageResult.add(esPost);
            }
        }catch (Exception e){
            return new Result(false,StatusCode.ERROR,"查询失败");
        }
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }
}
