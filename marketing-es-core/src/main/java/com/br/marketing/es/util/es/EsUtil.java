package com.br.marketing.es.util.es;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.UpdateByQueryRequest;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ES工具类
 *
 * @Author linquan.guo
 * @CreateDate 2020/12/29 15:48
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/12/29 15:48
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class EsUtil {
    private final static RestHighLevelClient client = EsClientFactory.getClient();

    /**
     * 新增
     *
     * @param index
     * @param object
     * @return
     */
    public static boolean insert(String index, Map object) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index);
        String id = object.remove("_id").toString();
        indexRequest.id(id);
        indexRequest.source(object);
        client.index(indexRequest, RequestOptions.DEFAULT);
        return true;
    }

    /**
     * 删除
     *
     * @param indices
     * @param queryBuilder
     * @return
     */
    public static long delete(String[] indices, QueryBuilder queryBuilder) throws IOException {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indices);
        deleteByQueryRequest.setQuery(queryBuilder);
        BulkByScrollResponse response = client.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
        log.debug("fail:(count:{},message:{}),detele:{}", response.getBulkFailures().size(), JSON.toJSONString(response.getBulkFailures())
                , response.getDeleted());
        return response.getDeleted();
    }

    /**
     * 更新
     *
     * @param indices
     * @param queryBuilder
     * @param object
     * @return
     */
    public static long update(String[] indices, QueryBuilder queryBuilder, Map<String, Object> object) throws IOException {
        UpdateByQueryRequest updateByQueryRequest = new UpdateByQueryRequest();
        updateByQueryRequest.setQuery(queryBuilder);
        updateByQueryRequest.indices(indices);
        StringBuilder sb = new StringBuilder();
        for (String key : object.keySet()) {
            sb.append("ctx._source.").append(key).append("=").append("params.").append(key).append(";");
        }
        updateByQueryRequest.setScript(new Script(ScriptType.INLINE, "painless", sb.toString(), object));
        BulkByScrollResponse response = client.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT);
        log.debug("fail:(count:{},message:{}),update:{}", response.getBulkFailures().size(), JSON.toJSONString(response.getBulkFailures())
                , response.getUpdated());
        return response.getUpdated();
    }

    /**
     * 更新
     *
     * @param index
     * @param object
     * @return
     */
    public static boolean update(String index, Map object) throws IOException {
        String _id = object.remove("_id").toString();
        UpdateRequest updateRequest = new UpdateRequest(index, _id);
        updateRequest.doc(object);
        client.update(updateRequest, RequestOptions.DEFAULT);
        return true;
    }

    /**
     * 更新立即进行数据刷新
     *
     * @param index
     * @param object
     * @return
     */
    public static boolean updateRefreshPolicyImmediate(String index, Map object) throws IOException {
        String _id = object.remove("_id").toString();
        UpdateRequest updateRequest = new UpdateRequest(index, _id);
        //以WriteRequest.RefreshPolicy实例形式设置刷新策略,RefreshPolicy#IMMEDIATE-请求向ElasticSearch提交了数据，立即进行数据刷新，然后再结束请求。
        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        updateRequest.doc(object);
        client.update(updateRequest, RequestOptions.DEFAULT);
        return true;
    }

    /**
     * 查询
     *
     * @param indices
     * @param templateId
     * @param params
     * @return
     */
    public static SearchHits selectByTemplate(String[] indices, String templateId, Map params) throws IOException {
        try {
            SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest();
            searchTemplateRequest.setRequest(new SearchRequest(indices));
            searchTemplateRequest.setScript(templateId);
            searchTemplateRequest.setScriptType(ScriptType.STORED);
            searchTemplateRequest.setScriptParams(params);
            SearchTemplateResponse response = client.searchTemplate(searchTemplateRequest, RequestOptions.DEFAULT);
            return response.getResponse().getHits();
        } catch (ElasticsearchStatusException esException) {
            log.error("selectByTemplate ElasticsearchStatusException:{}", esException.getMessage());
        } catch (Exception e) {
            log.error("selectByTemplate error", e);
        }
        return null;
    }

    /**
     * 查询总数
     *
     * @param indices
     * @param queryBuilder
     * @return
     */
    public static long count(String[] indices, QueryBuilder queryBuilder) throws IOException {
        CountRequest countRequest = new CountRequest(indices);
        countRequest.query(queryBuilder);
        CountResponse countR = client.count(countRequest, RequestOptions.DEFAULT);
        return countR.getCount();
    }

    /**
     * 查询
     *
     * @param indices
     * @param templateId
     * @param params
     * @return
     */
    public static long selectByTemplateCount(String[] indices, String templateId, Map params) throws IOException {
        try {
            SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest();
            searchTemplateRequest.setRequest(new SearchRequest(indices));
            searchTemplateRequest.setScript(templateId);
            searchTemplateRequest.setScriptType(ScriptType.STORED);
            searchTemplateRequest.setScriptParams(params);
            // 给定参数值，模板可以在不执行搜索的情况下呈现:
            searchTemplateRequest.setSimulate(true);
            SearchTemplateResponse response = client.searchTemplate(searchTemplateRequest, RequestOptions.DEFAULT);
            String source = response.getSource().utf8ToString();
            JSONObject query = JSON.parseObject(source).getJSONObject("query");
            //获取query查询
            WrapperQueryBuilder builder = new WrapperQueryBuilder(query.toJSONString());
            CountRequest countRequest = new CountRequest(indices);
            countRequest.query(builder);
            CountResponse countR = client.count(countRequest, RequestOptions.DEFAULT);
            return countR.getCount();
        } catch (ElasticsearchStatusException esException) {
            log.error("selectByTemplateCount ElasticsearchStatusException:{}", esException.getMessage());
        } catch (Exception e) {
            log.error("selectByTemplateCount error", e);
        }
        return 0L;
    }

    /**
     * 查询
     *
     * @param indices
     * @param queryBuilder
     * @return
     */
    public static String select(String[] indices, QueryBuilder queryBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indices);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        return hitsToString(response.getHits());
    }

    /**
     * 转换数据类型
     *
     * @param hits
     * @return
     */
    private static String hitsToString(SearchHits hits) {
        List<String> result = new ArrayList<>(hits.getHits().length);
        for (SearchHit hit : hits) {
            result.add(hit.getSourceAsString());
        }
        return JSON.toJSONString(result);
    }
}
