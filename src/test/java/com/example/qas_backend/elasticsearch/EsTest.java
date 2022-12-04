package com.example.qas_backend.elasticsearch;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.qas_backend.post.entity.ESPost;
import com.example.qas_backend.post.entity.Post;
import com.example.qas_backend.post.mapper.PostMapper;
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


import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;


@SpringBootTest
public class EsTest {

    @Autowired
    private ElasticsearchClient client;

    @Autowired
    private PostMapper postMapper;


    @Test
    // 将数据库中的post记录批量添加到es中
    public void insertToEs() throws IOException {
//        DeleteIndexResponse deleteIndexResponse = client.indices().delete(d -> d.index("post"));
//        System.out.println(deleteIndexResponse.acknowledged());
//
////        Map<String, Property> map=new HashMap<>();
////        map.put("post_id",{"type":"long"});
////        TypeMapping.Builder tb=new TypeMapping.Builder();
////        tb.properties();
////        CreateIndexResponse createIndexResponse=client.indices().create(c->c.index("post")
////                .mappings(m->m.properties(
////
////                )));
//
//        Reader input = new StringReader(
//                "PUT post\n" +
//                        "{\n" +
//                        "  \"mappings\": {\n" +
//                        "    \"properties\": {\n" +
//                        "      \"post_id\":{\n" +
//                        "        \"type\":\"long\"\n" +
//                        "      },\n" +
//                        "      \"title\":{\n" +
//                        "        \"type\": \"keyword\",\n" +
//                        "        \"fields\": {\n" +
//                        "            \"ik_max_analyzer\": {\n" +
//                        "              \"type\": \"text\",\n" +
//                        "              \"analyzer\": \"ik_max_word\",\n" +
//                        "              \"search_analyzer\": \"ik_max_word\"\n" +
//                        "            },\n" +
//                        "            \"ik_smart_analyzer\": {\n" +
//                        "              \"type\": \"text\",\n" +
//                        "              \"analyzer\": \"ik_smart\"\n" +
//                        "            }\n" +
//                        "          }\n" +
//                        "      },\n" +
//                        "      \"content\":{\n" +
//                        "        \"type\": \"text\"\n" +
//                        "      },\n" +
//                        "      \"user_id\":{\n" +
//                        "        \"type\": \"integer\"\n" +
//                        "      },\n" +
//                        "      \"views\":{\n" +
//                        "        \"type\": \"long\"\n" +
//                        "      },\n" +
//                        "      \"likes\":{\n" +
//                        "        \"type\": \"integer\"\n" +
//                        "      },\n" +
//                        "      \"create_time\":{\n" +
//                        "        \"type\": \"date\"\n" +
//                        "      },\n" +
//                        "      \"update_time\":{\n" +
//                        "        \"type\": \"date\"\n" +
//                        "      },\n" +
//                        "      \"floors\":{\n" +
//                        "        \"type\": \"integer\"\n" +
//                        "      },\n" +
//                        "      \"total_floors\":{\n" +
//                        "        \"type\": \"integer\"\n" +
//                        "      }\n" +
//                        "    }\n" +
//                        "  }\n" +
//                        "}"
//        );
////        CreateIndexRequest createIndexRequest= CreateIndexRequest.of(i->i.index("post").withJson(input));
//        IndexRequest<JsonData> request=IndexRequest.of(i->i
//                .index("post")
//                .withJson(input)
//        );
//        IndexResponse indexResponse = client.index(request);
//        System.out.println(indexResponse);

        // 批量添加数据
        //        List<BulkOperation> bulkOperationArrayList = new ArrayList<>();
//        for (Post post : posts) {
//            bulkOperationArrayList.add(BulkOperation.of(o->o.index(i->i.document(new ESPost(post)))));
//        }
//        BulkResponse bulkResponse = client.bulk(b -> b
//                .index("post")
//                .operations(bulkOperationArrayList));
//        System.out.println(bulkResponse);

        QueryWrapper<Post>queryWrapper=new QueryWrapper<>();
        List<Post> posts = postMapper.selectList(queryWrapper);

        BulkRequest.Builder br=new BulkRequest.Builder();
        int count=1;
        for (Post post : posts) {
            ESPost esPost = new ESPost(post);
            br.operations(o->o.index(i->i.index("post")
                    .id(esPost.getPostId().toString())
                    .document(esPost)));
            System.out.println(count++);
        }
        BulkResponse bulkResponse = client.bulk(br.build());
        System.out.println(bulkResponse);

    }


    // 对比数据库查询和elasticsearch查询
    @Test
    public void compareMysqlAndEs() throws IOException{
        // mysql查询 1669948809326-1669948808770
        int start= (int) System.currentTimeMillis();

        QueryWrapper<Post> queryWrapper=new QueryWrapper<>();
        queryWrapper.like("title","军人");
        List<Post> post = postMapper.selectList(queryWrapper);
        System.out.println((int)System.currentTimeMillis()-start + "ms");

//        // es查询
//        System.out.println(System.currentTimeMillis());
//        Map<String, HighlightField> map=new HashMap<>();
//        map.put("title.ik_max_analyzer",HighlightField.of(hf->hf.numberOfFragments(0)));
//        Highlight highlight=Highlight.of(
//                h->h.type(HighlighterType.Unified)
//                        .fields(map)
//                        .fragmentSize(50)
//                        .numberOfFragments(5)
//        );
//
//        SearchResponse<ESPost> search = client.search(s -> s
//                .index("post")
//                //查询name字段包含hello的document(不使用分词器精确查找)
//                .query(q -> q
//                        .match(m->m.field("title.ik_max_analyzer")
//                                .query("军人"))
//                )
//                .highlight(highlight)
//                //按age降序排序
//                ,ESPost.class
//        );
//        System.out.println(System.currentTimeMillis());


    }



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


    //通过script更新文档
    @Test
    public void updateDocumentByScript() throws IOException{
//        org.elasticsearch.action.update.UpdateRequest request= new org.elasticsearch.action.update.UpdateRequest("testdb","2");
//        String jsonString="{\n" + "\"script\":\"ctx._source.score+=125\"\n" + "}\n";
//        request.doc(jsonString, XContentType.JSON);
//        client.putScript(p->p.id("add")
//                .script(s->s.source("{\"script\":\"ctx._source.score+=125\"}\n")));
//        client.update
        Reader input = new StringReader("{\"script\":\"ctx._source.score+=125\"}");

//        CreateIndexRequest createIndexRequest= CreateIndexRequest.of(i->i.index("post").withJson(input));
//        IndexRequest<JsonData> request=IndexRequest.of(i->i
//                .index("testdb")
//                .withJson(input)
//        );
//
//        UpdateRequest updateRequest=UpdateRequest.of(o-> o.index("testdb")
//                .id("2")
//                .script());
//
//        IndexResponse indexResponse = client.index(request);
//        System.out.println(indexResponse);
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
