package com.example.qas_backend.post.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qas_backend.common.entity.Result;
import com.example.qas_backend.post.dto.NewFloor;
import com.example.qas_backend.post.entity.Floor;

/**
 * 楼层服务接口
 */
public interface IFloorService extends IService<Floor> {

    //发表楼层
    Result publishFloor(String token, NewFloor newFloor);

    //删除楼层
    Result deleteFloor(String token, Long floorId);

    //给楼层点赞
    Result likeTheFloor(String token, Long floorId);

    //给楼层取消赞
    Result dislikeTheFloor(String token, Long floorId);

}
