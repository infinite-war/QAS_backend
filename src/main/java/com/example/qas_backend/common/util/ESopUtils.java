package com.example.qas_backend.common.util;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ScriptSort;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.HighlighterType;
import com.example.qas_backend.post.dto.PagingParam;
import com.example.qas_backend.post.dto.SearchParam;
import com.example.qas_backend.post.entity.ESPost;
import com.example.qas_backend.post.entity.Post;
import com.example.qas_backend.post.entity.User;
import com.example.qas_backend.post.mapper.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


// 封装一些常用的es操作
@Component
public class ESopUtils {
    private final PostMapper postMapper;
    private final ElasticsearchClient esClient;

    @Autowired
    public ESopUtils(PostMapper postMapper,ElasticsearchClient esClient) {
        this.postMapper = postMapper;
        this.esClient=esClient;
    }


    // 关键字高亮查询   ,设置参数：要按什么方式查询
    public SearchResponse<ESPost> searchPosts(SearchParam searchParam, PagingParam pagingParam) throws IOException {

        Map<String, HighlightField> map=new HashMap<>();
        // 设置分词器
        map.put("title.ik_max_analyzer",HighlightField.of(hf->hf.numberOfFragments(0)));
        // 设置高亮
        Highlight highlight=Highlight.of(
                h->h.type(HighlighterType.Unified)
                        .fields(map)
                        .fragmentSize(50)
                        .numberOfFragments(5)
                        .preTags("<span style=\"color: red\">")
                        .postTags("</span>")
        );

        // 查询
        SearchResponse<ESPost> search = esClient.search(s -> s
                        .index("post")
                        //查询name字段包含hello的document(不使用分词器精确查找)
                        .query(q -> q
                                .match(m -> m.field("title.ik_max_analyzer")
                                .query(searchParam.getKeyword().toString()))
                        )
                        .highlight(highlight)
                        //分页查询，从第0页开始查询3个document
                        .from(pagingParam.getPage())
                        .size(pagingParam.getSize()
                                //es内部会根据词频给匹配到的结果进行打分(score)，返回的结果基于分值降序排序
                        ), ESPost.class
        );
        return search;
    }

    // 查询用户发布的post
    public SearchResponse<ESPost> getMyPosts(Long userId,SearchParam searchParam,PagingParam pagingParam) throws IOException {
        SearchResponse<ESPost> search;//分页查询
        if(searchParam.getKeyword()!=null){
            search = esClient.search(s -> s
                            .index("post")
                            .query(q -> q
                                    .bool(b -> b.must(m1 -> m1.term(t1 -> t1.field("userId").value(userId)))
                                                .must(m2 -> m2.match(t2 -> t2.field("title").query(searchParam.getKeyword()))))
                            )
                            //分页查询
                            .from(pagingParam.getPage())
                            .size(pagingParam.getSize())
                            .sort(f -> f.field(o -> o.field("updateTime").order(SortOrder.Desc)))
                    , ESPost.class
            );
        }
        else{
            search = esClient.search(s -> s
                            .index("post")
                            .query(q -> q
                                    .term(t -> t.field("userId").value(userId))
                            )
                            //分页查询
                            .from(pagingParam.getPage())
                            .size(pagingParam.getSize())
                            .sort(f -> f.field(o -> o.field("updateTime").order(SortOrder.Desc)))
                    , ESPost.class
            );
        }
        return search;
    }


    // 在es中添加记录
    public Boolean insertESPost(Post post) throws IOException {
        try {
            IndexResponse indexResponse = esClient.index(i -> i
                    .index("post")
                    .id(post.getPostId().toString())
                    .document(new ESPost(post)));
        }catch (Exception e){
            return false;
        }
        return true;
    }


    // 更新es中的post记录
    public Boolean updateESPost(Long postId) throws IOException {
        Post post=postMapper.selectById(postId);
        try {
            esClient.update(u -> u
                            .index("post")
                            .id(postId.toString())
                            .doc(new ESPost(post))
                    , User.class);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    // 更新post的view
    public Boolean updateESPostViews(Long postId,Long addViews) throws IOException{
        try {
            Reader input = new StringReader("{\"script\":\"ctx._source.views+=" + addViews.toString() + "\"}");
            esClient.update(u -> u
                            .index("post")
                            .id(postId.toString())
                            .withJson(input)
                    , ESPost.class);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    // es中的post点赞数据+1
    public Boolean increaseESPostLikes(Long postId) throws IOException{
        try {
            Reader input = new StringReader("{\"script\":\"ctx._source.likes+=1\"}");
            esClient.update(u -> u
                            .index("post")
                            .id(postId.toString())
                            .withJson(input)
                    , ESPost.class);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    // es中的post点赞数据-1
    public Boolean decreaseESPostLikes(Long postId) throws IOException{
        try {
            Reader input = new StringReader("{\"script\":\"ctx._source.likes-=1\"}");
            esClient.update(u -> u
                            .index("post")
                            .id(postId.toString())
                            .withJson(input)
                    , ESPost.class);
        }catch (Exception e){
            return false;
        }
        return true;
    }

    // 删除es中的post记录
    public Boolean deleteESPost(Long postId) throws IOException{
        try {
            esClient.delete(d -> d.index("post").id(postId.toString()));
        }catch (Exception e){
            return false;
        }
        return true;
    }


    // 随机获取10个帖子
    public SearchResponse<ESPost> getRandomPosts() throws IOException{
                Reader input = new StringReader(
                "{\n" +
                        "  \"from\": 1,\n" +
                        "  \"size\": 10,\n" +
                        "  \"sort\": [\n" +
                        "    {\n" +
                        "        \"_script\": {\n" +
                        "        \"script\": \"Math.random()\",\n" +
                        "        \"type\": \"number\",\n" +
                        "        \"order\": \"asc\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}"
        );

        SearchResponse<ESPost> search = esClient.search(s -> s.index("post")
                        .withJson(input)
                    , ESPost.class
            );

        return search;
    }

}
