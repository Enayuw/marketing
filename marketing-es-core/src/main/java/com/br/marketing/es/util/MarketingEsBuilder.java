package com.br.marketing.es.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.util.es.EsHandleUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ES条件组装
 *
 * @Author linquan.guo
 * @CreateDate 2021/1/9 17:29
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/1/9 17:29
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class MarketingEsBuilder {
    private QueryBaseBean queryBaseBean;

    public MarketingEsBuilder(QueryBaseBean queryBaseBean) {
        this.queryBaseBean = queryBaseBean;
    }

    /**
     * 选取索引
     *
     * @param
     * @return
     */
    public Set<String> builderHistoryWithIndexSet() {
        Set<String> indexSet = new HashSet<>();
        //多个流水号
        String batchNumbers = queryBaseBean.getBatchNumbers();
        String[] split = batchNumbers.split(",");
        if (split != null && split.length > 0) {
            for (String batchNumber : split) {
                String date = EsHandleUtil.getDateFromBatchNumber(batchNumber);
                String index = String.format(EsConstants.HISTORY_KEY, date);
                indexSet.add(index);
            }
        } else {
            throw new RuntimeException("Index is exception");
        }
        return indexSet;
    }

    /**
     * 查询营销数量构建
     *
     * @param paramsCount
     * @return
     */
    public void marketingWhereCount(Map<String, Object> paramsCount) {
        //返回结果，排序会根据产品值进行倒序排序，二次排序采用流水号
        paramsCount.put("source", JSON.toJSONString(Arrays.asList("_id".split(","))));
        //查询营销条件构建
        marketingWhere(paramsCount);
    }

    /**
     * 查询营销条件构建
     *
     * @param params
     * @return
     */
    public void marketingWhere(Map<String, Object> params) {
        //api_code必传
        String apiCode = queryBaseBean.getApiCode();
        if (StringUtils.isNotBlank(apiCode)) {
            params.put("api_code", apiCode);
        }
        //查询批次
        String batchNumber = queryBaseBean.getBatchNumbers();
        if (StringUtils.isNotBlank(batchNumber)) {
            List<String> batchNumberList = Arrays.asList(batchNumber.split(","));
            params.put("batch_number", JSON.toJSONString(batchNumberList));
        }
        //分值区间
        String modelCode = queryBaseBean.getModelCode();
        String modelVersion = queryBaseBean.getModelVersion();
        if (StringUtils.isBlank(modelVersion)) {
            modelVersion = "";
        }
        String scoreRange = queryBaseBean.getScoreRange();
        if (StringUtils.isNotBlank(modelCode) && StringUtils.isNotBlank(scoreRange)) {
            String cv = String.format(EsConstants.CODEVERSION_KEY, modelCode, modelVersion);
            params.put("code_version", cv);
            String[] split = scoreRange.split(",");
            if (split != null && split.length > 0) {
                Double begin = 0D;
                Double end = 0D;
                for (int i = 0; i < split.length; i++) {
                    if (i == 0) {
                        begin = Double.valueOf(split[i]);
                    } else {
                        end = Double.valueOf(split[i]);
                    }
                }
                params.put("begin_score", begin);
                params.put("end_score", end);
            }
        }
    }

    /**
     * 根据条件获取滚动搜索值
     *
     * @param
     * @return
     */
    public Map<String, Object> builderMarketingWithSearchAfter() {
        Map<String, Object> params = new HashMap<>();
        //返回结果，排序会根据产品值进行倒序排序，二次排序采用流水号
        List<String> fieldList = Arrays.asList("_id".split(","));
        params.put("source", JSON.toJSONString(fieldList));
        //分页大小
        Integer pageSize = queryBaseBean.getPageSize();
        if (pageSize > 10000) {
            throw new RuntimeException("ES size Exception" + pageSize);
        }
        params.put("size", pageSize);
        //滚动搜索值
        String searchAfter = queryBaseBean.getSearchAfter();
        if(StringUtils.isNotBlank(searchAfter)){
            params.put("searchAfter", searchAfter);
        }
        //条件
        marketingWhere(params);
        return params;
    }

    /**
     * 选取数据
     *
     * @param columns
     * @return
     */
    public Map<String, Object> builderMarketingWithList(String columns) {
        Map<String, Object> params = new HashMap<>();
        //返回结果，排序会根据产品值进行倒序排序，二次排序采用流水号
        String sources = EsConstants.ALL_MARKETING_KEY;
        if (StringUtils.isNotBlank(columns)) {
            sources = columns;
        }
        List<String> fieldList = Arrays.asList(sources.split(","));
        params.put("source", JSON.toJSONString(fieldList));
        //返回条数
        Integer pageSize = queryBaseBean.getPageSize();
        if (pageSize > 10000) {
            throw new RuntimeException("ES size Exception" + pageSize);
        }
        params.put("size", pageSize);
        //滚动搜索值
        String searchAfter = queryBaseBean.getSearchAfter();
        if(StringUtils.isNotBlank(searchAfter)){
            params.put("searchAfter", searchAfter);
        }
        //条件
        marketingWhere(params);
        return params;
    }
}
