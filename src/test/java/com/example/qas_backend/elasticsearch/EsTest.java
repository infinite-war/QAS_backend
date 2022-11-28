package com.example.qas_backend.elasticsearch;


import org.springframework.boot.test.context.SpringBootTest;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;

import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.GetIndexResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@SpringBootTest
public class EsTest {

    @Autowired
    private ElasticsearchClient client;


    //====================================================================================

    // =========================JAVA API============================

    //====================================================================================


    // 增加index
    @Test
    void createTest() throws IOException{
        //写法比RestHighLevelClient更加简洁
        CreateIndexResponse indexResponse=client.indices().create(c->c.index("m_index"));
        System.out.println(indexResponse.index());
        System.out.println(indexResponse);
        System.out.println(indexResponse.acknowledged());
    }

    // 查询index
    @Test
    public void queryTest() throws IOException {
        GetIndexResponse getIndexResponse = client.indices().get(i -> i.index("user"));
        System.out.println(getIndexResponse);
    }


    // 判断index是否存在
    @Test
    public void existsTest() throws IOException {
        BooleanResponse booleanResponse = client.indices().exists(e -> e.index("user"));
        System.out.println(booleanResponse.value());
    }

    // 删除index
    @Test
    public void deleteTest() throws IOException {
        DeleteIndexResponse deleteIndexResponse = client.indices().delete(d -> d.index("user"));
        System.out.println(deleteIndexResponse.acknowledged());
    }

    //====================文档操作====================

    // 插入文档
    @Test
    public void addDocumentTest() throws IOException{
        User user = new User("user1", 10);
        IndexResponse indexResponse = client.index(i -> i
                .index("user")
                //设置id
                .id("1")
                //传入user对象
                .document(user));
        System.out.println(indexResponse.index());
        System.out.println(indexResponse);
    }

    //更新文档
    @Test
    public void updateDocumentTest() throws IOException {
        UpdateResponse<User> updateResponse = client.update(u -> u
                        .index("user")
                        .id("1")
                        .doc(new User("user2", 13))
                , User.class);
        System.out.println(updateResponse);
    }

    //判断document是否存在
    @Test
    public void existDocumentTest() throws IOException {
        BooleanResponse indexResponse = client.exists(e -> e.index("user").id("1"));
        System.out.println(indexResponse.value());
    }

    //查询document
    @Test
    public void getDocumentTest() throws IOException {
        GetResponse<User> getResponse = client.get(g -> g
                        .index("user")
                        .id("1")
                , User.class
        );
        System.out.println(getResponse.source());
    }

    //删除document
    @Test
    public void deleteDocumentTest() throws IOException {
        DeleteResponse deleteResponse = client.delete(d -> d
                .index("user")
                .id("1")
        );
        System.out.println(deleteResponse.id());
    }

    //批量插入document
    @Test
    public void bulkTest() throws IOException {
        List<User> userList = new ArrayList<>();
        userList.add(new User("user1", 11));
        userList.add(new User("user2", 12));
        userList.add(new User("user3", 13));
        userList.add(new User("user4", 14));
        userList.add(new User("user5", 15));
        List<BulkOperation> bulkOperationArrayList = new ArrayList<>();
        //遍历添加到bulk中
        for(User user : userList){
            bulkOperationArrayList.add(BulkOperation.of(o->o.index(i->i.document(user))));
        }

        BulkResponse bulkResponse = client.bulk(b -> b
                .index("user")
                .operations(bulkOperationArrayList));
        System.out.println(bulkResponse);
    }


    // 查询
    @Test
    public void searchTest() throws IOException {
        SearchResponse<User> search = client.search(s -> s
                        .index("user")
                        //查询name字段包含hello的document(不使用分词器精确查找)
                        .query(q -> q
                                .bool(b->b
                                        .should(sh->sh
                                                .match(m1->m1
                                                        .field("name")
                                                        .query("user1")
                                                ))
                                        .should(sh1->sh1
                                                .match(m2->m2
                                                        .field("age")
                                                        .query(12)))
                                ))
//                        .term(t -> t
//                                .field("name")
//                                .value(v -> v.stringValue("user1"))
//                        ))
                        //分页查询，从第0页开始查询3个document
                        .from(0)
                        .size(10)
                        //按age降序排序
                        .sort(f->f.field(o->o.field("age").order(SortOrder.Desc))),User.class
        );
        for (Hit<User> hit : search.hits().hits()) {
            System.out.println(hit.source());
        }
    }

}
