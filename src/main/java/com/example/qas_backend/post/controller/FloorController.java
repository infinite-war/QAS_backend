package com.example.qas_backend.post.controller;


import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.post.dto.NewFloor;
import com.example.qas_backend.post.dto.PagingParam;
import com.example.qas_backend.post.dto.SearchParam;
import com.example.qas_backend.post.service.IFloorService;
import com.example.qas_backend.post.aspect.UserRequired;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 楼层控制器
 */
@RestController
@RequestMapping("/post/floor")
public class FloorController {

    private final IFloorService floorService;

    @Autowired
    public FloorController(IFloorService floorService) {
        this.floorService = floorService;
    }

    //发布楼层请求
    @UserRequired
    @PostMapping
    public Result publishFloor(@RequestHeader String token, @Valid @RequestBody NewFloor newFloor) {
        return floorService.publishFloor(token, newFloor);
    }

    //删除楼层请求
    @UserRequired
    @DeleteMapping("/{floorId}")
    public Result deleteFloor(@RequestHeader String token, @PathVariable Long floorId) {
        return floorService.deleteFloor(token, floorId);
    }

    //楼层点赞请求
    @UserRequired
    @PostMapping("/like/{floorId}")
    public Result likeTheFloor(@RequestHeader String token, @PathVariable Long floorId) {
        return floorService.likeTheFloor(token, floorId);
    }

    //楼层取消赞请求
    @UserRequired
    @DeleteMapping("/like/{floorId}")
    public Result dislikeTheFloor(@RequestHeader String token, @PathVariable Long floorId) {
        return floorService.dislikeTheFloor(token, floorId);
    }


    // 获取自己发布的楼层
    @UserRequired
    @GetMapping("/myfloors")
    public Result getMyFloors(@RequestHeader String token, SearchParam searchParam, @Valid PagingParam pagingParam){
        return floorService.getMyFloors(token,searchParam,pagingParam);
    }
}

