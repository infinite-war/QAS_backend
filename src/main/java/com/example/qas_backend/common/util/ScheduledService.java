package com.example.qas_backend.common.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.StoredScriptId;

import co.elastic.clients.elasticsearch.core.UpdateResponse;
import co.elastic.clients.util.ObjectBuilder;
import com.example.qas_backend.post.entity.ESPost;
import com.example.qas_backend.post.entity.Post;
import com.example.qas_backend.post.mapper.PostMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Redis定时任务类
 */
@Slf4j  //日志插件
@Component
public class ScheduledService {

    private final RedisUtils redisUtils;
    private final PostMapper postMapper;
    private final ESopUtils eSopUtils;


    public ScheduledService(RedisUtils redisUtils, PostMapper postMapper, ESopUtils eSopUtils) {
        this.redisUtils = redisUtils;
        this.postMapper = postMapper;
        this.eSopUtils = eSopUtils;
    }


    //每过5秒就将缓存中的浏览量写入数据库
    @Scheduled(fixedRate = 5 * 1000)
    public void storeViewsInTheDatabase() {
        Map<String, Integer> map = redisUtils.getPostViewsEntry();
        if (!map.isEmpty()) {
            //日志中记录写入事件
            log.info("即将把Redis中的帖子访问量写入MySQL，其内容为" + map);
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                Long postId = Long.parseLong(entry.getKey());
                try {
                    //更新数据库
                    postMapper.increaseViewsBySpecifiedAmount(postId, Long.valueOf(entry.getValue()));

                    //更新es
                    eSopUtils.updateESPostViews(postId,Long.valueOf(entry.getValue()));

                } catch (Exception e) {
                    log.info("浏览量写入MySQL(或es)时出现异常，异常为" + e.getMessage() + "，帖子ID为" + postId);
                }
                redisUtils.deletePostViewsInCache(postId);
            }
        }
    }


}