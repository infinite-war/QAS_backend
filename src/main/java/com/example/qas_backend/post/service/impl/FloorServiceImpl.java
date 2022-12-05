package com.example.qas_backend.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.qas_backend.common.entity.PageResult;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.common.entity.StatusCode;
import com.example.qas_backend.common.util.IdWorker;
import com.example.qas_backend.common.util.WrapperOrderPlugin;
import com.example.qas_backend.post.dto.NewFloor;
import com.example.qas_backend.post.dto.PagingParam;
import com.example.qas_backend.post.dto.SearchParam;
import com.example.qas_backend.post.entity.Comment;
import com.example.qas_backend.post.entity.Floor;
import com.example.qas_backend.post.entity.Post;
import com.example.qas_backend.post.mapper.CommentMapper;
import com.example.qas_backend.post.mapper.FloorMapper;
import com.example.qas_backend.post.mapper.PostMapper;
import com.example.qas_backend.post.mapper.UserMapper;
import com.example.qas_backend.post.service.IFloorService;
import com.example.qas_backend.common.util.RedisUtils;
import com.example.qas_backend.post.dto.PublishFloor;
import com.example.qas_backend.common.util.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 楼层服务实现类
 */
@Service
public class FloorServiceImpl extends ServiceImpl<FloorMapper, Floor> implements IFloorService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;

    private final FloorMapper floorMapper;

    private final CommentMapper commentMapper;

    private final RedisUtils redisUtils;

    private final IdWorker idWorker;

    private final TokenUtils tokenUtils;

    @Autowired
    public FloorServiceImpl(UserMapper userMapper, PostMapper postMapper, FloorMapper floorMapper, CommentMapper commentMapper, RedisUtils redisUtils, IdWorker idWorker, TokenUtils tokenUtils) {
        this.userMapper = userMapper;
        this.postMapper = postMapper;
        this.floorMapper = floorMapper;
        this.commentMapper = commentMapper;
        this.redisUtils = redisUtils;
        this.idWorker = idWorker;
        this.tokenUtils = tokenUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result publishFloor(String token, NewFloor newFloor) {
        //要给哪个帖子盖楼
        Long belongPostId = newFloor.getPostId();
        Post belongPost = postMapper.selectById(belongPostId);
        if (belongPost == null) {
            return new Result(true, StatusCode.OK, "发布失败，指定的帖子不存在");
        }
        //封装楼层信息
        Floor floor = new Floor();
        Long floorId = idWorker.nextId();
        floor.setFloorId(floorId);
        floor.setBelongPostId(belongPostId);
        floor.setFloorNumber(belongPost.getTotalFloors() + 1);
        floor.setUserId(tokenUtils.getUserIdFromToken(token));
        floor.setLikes(0);
        floor.setContent(newFloor.getContent());
        LocalDateTime now = LocalDateTime.now();
        floor.setCreateTime(now);
        floor.setComments(0);
        floor.setTotalComments(0);
        //将新楼层加入数据库
        floorMapper.insert(floor);
        //该帖子的楼层数+1
        postMapper.addFloorToPost(belongPostId);
        return new Result(true, StatusCode.OK, "发布楼层成功", new PublishFloor(floorId, floor.getFloorNumber()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteFloor(String token, Long floorId) {
        Long userId = tokenUtils.getUserIdFromToken(token);
        Floor floor = floorMapper.selectById(floorId);
//        if(userMapper.getUserNameById(userId)==null){
//            return new Result(false,StatusCode.ACCESS_ERROR,"用户不存在");
//        }
        if (floor == null) {
            return new Result(false, StatusCode.PARAM_ERROR, "删除失败，指定的楼层不存在");
        }
        if (!userId.equals(floor.getUserId())) {
            return new Result(false, StatusCode.ACCESS_ERROR, "删除失败，无权操作");
        }
        //在评论表中删除该楼层下的评论记录
        commentMapper.delete(new QueryWrapper<Comment>().eq("belong_floor_id", floorId));
        //在楼层表中删除该楼层记录
        floorMapper.deleteById(floorId);
        //该帖子的楼层数-1
        postMapper.removeFloorFromPost(floor.getBelongPostId());
        return new Result(true, StatusCode.OK, "删除成功");
    }

    @Override
    public Result likeTheFloor(String token, Long floorId) {
        Long userId = tokenUtils.getUserIdFromToken(token);
        //在redis中查看发送请求的用户是否赞过这个楼层
        boolean liked = redisUtils.queryUserIsLike(userId, floorId);
        //若没赞过
        if (!liked) {
            redisUtils.addUserLike(userId, floorId);
            floorMapper.increaseFloorLikes(floorId);
            return new Result(true, StatusCode.OK, "点赞成功");
        }
        return new Result(true, StatusCode.REP_ERROR, "已经给楼层点赞，无法再次点赞");
    }

    @Override
    public Result dislikeTheFloor(String token, Long floorId) {
        Long userId = tokenUtils.getUserIdFromToken(token);
        //之前有没有赞过
        boolean liked = redisUtils.queryUserIsLike(userId, floorId);
        //赞过
        if (liked) {
            redisUtils.removeUserLike(userId, floorId);
            floorMapper.decreaseFloorLikes(floorId);
            return new Result(true, StatusCode.OK, "取消点赞成功");
        }
        return new Result(true, StatusCode.REP_ERROR, "尚未给楼层点赞，无法取消点赞");
    }

    @Override
    public Result getMyFloors(String token, SearchParam searchParam, PagingParam pagingParam){
        if(pagingParam.getPage()==null){
            pagingParam.setPage(0);
        }
        if(pagingParam.getSize()==null){
            pagingParam.setSize(10);
        }

        QueryWrapper<Floor> queryWrapper = new QueryWrapper<>();
        //如果SearchParam存在参数，则加入QueryWrapper中作为查询条件
        if ((searchParam.getKeyword() != null) && (!searchParam.getKeyword().isBlank())) {
            queryWrapper.like("content", searchParam.getKeyword());
        }
        if (searchParam.getUserId() != null) {
            queryWrapper.eq("user_id", searchParam.getUserId());
        }
        WrapperOrderPlugin.addTimeOrderToFloorWrapper(queryWrapper,pagingParam.getOrder());
        //对帖子进行分页处理
        IPage<Floor> page = new Page<>(pagingParam.getPage(), pagingParam.getSize());
        IPage<Floor> result = floorMapper.selectPage(page, queryWrapper);
        List<Floor> floorList = result.getRecords();

//        System.out.println(floorList);
        //填充PageResult
        PageResult<Floor> pageResult = new PageResult<>();
        pageResult.setRecords(floorList);
        pageResult.setTotal(floorList.size());
        return new Result(true, StatusCode.OK, "查询成功", pageResult);

    }
}
