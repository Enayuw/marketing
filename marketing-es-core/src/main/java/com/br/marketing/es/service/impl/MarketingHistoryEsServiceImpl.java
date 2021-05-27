package com.br.marketing.es.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.es.util.EsConstants;
import com.br.marketing.es.util.MarketingEsBuilder;
import com.br.marketing.es.util.SwiftNumberManager;
import com.br.marketing.es.util.es.EsHandleUtil;
import com.br.marketing.es.util.es.EsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 营销ES
 *
 * @Author linquan.guo
 * @CreateDate 2021/5/24 10:07
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/5/24 10:07
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Service
@Slf4j
public class MarketingHistoryEsServiceImpl implements MarketingHistoryEsService {

    /**
     * 插入
     *
     * @param marketing
     * @param uuid
     * @return
     */
    @Override
    public void insert(MarketingHistory marketing, String uuid) {
        String batchNumber = marketing.getBatchNumber();
        //流水号处理
        String swiftNumber = marketing.getSwiftNumber();
        String apiCode = marketing.getApiCode();
        if (StringUtils.isBlank(swiftNumber)) {
            swiftNumber = apiCode + "_" + SwiftNumberManager.getSwiftNumberManager().getSwiftNumberPre();
            marketing.setSwiftNumber(swiftNumber);
        }
        long startTime = System.currentTimeMillis();
        boolean insert = false;
        for (int i = 0; i < 3; i++) {
            JSONObject params = (JSONObject) JSONObject.toJSON(marketing);
            params.put("_id", uuid);
            try {
                String date = EsHandleUtil.getDateFromBatchNumber(batchNumber);
                String index = String.format(EsConstants.HISTORY_KEY, date);
                insert = EsUtil.insert(index, params);
                log.info("ES insert batch_number:{},uuid:{},insert:{}", batchNumber, uuid, insert);
            } catch (Exception e) {
                log.error("ES insert error,uuid:{},重试", uuid, e);
            }
            if (!insert) {
                log.warn("第{}次请求insert params:{} costTime:{}", i, params, System.currentTimeMillis() - startTime);
                threadSleep();
            } else {
                break;
            }
        }
    }

    /**
     * 沉睡0.5s
     *
     * @param
     * @return
     */
    private void threadSleep() {
        try {
            Thread.sleep(500);
        } catch (Exception e) {
            log.error("Thread.sleep error", e);
        }
    }

    /**
     * 总记录数查询
     *
     * @param queryBaseBean
     * @return
     */
    @Override
    public int builderMarketingWithTotal(QueryBaseBean queryBaseBean) {
        try {
            if (StringUtils.isNotBlank(queryBaseBean.getApiCode())) {
                MarketingEsBuilder esBuilder = new MarketingEsBuilder(queryBaseBean);
                //根据批次号判断查询ES索引
                Set<String> indexSet = esBuilder.builderHistoryWithIndexSet();
                Map<String, Object> paramsCount = new HashMap<>();
                esBuilder.MarketingWhereCount(paramsCount);
                String[] indexArr = indexSet.toArray(new String[indexSet.size()]);
                //查询数量
                int count = (int) EsUtil.selectByTemplateCount(indexArr, EsConstants.PAGE_TEMPLATE, paramsCount);
                log.info("ES builderMarketingCount indexSet:{},paramsCount:{},count:{}",
                        JSON.toJSONString(indexSet), JSON.toJSONString(paramsCount), count);
                return amountTopHandle(queryBaseBean.getAmountTop(), count);
            }
        } catch (Exception e) {
            log.error("ES builderMarketingCount error,params:{}", JSON.toJSONString(queryBaseBean), e);
        }
        return 0;
    }

    /**
     * 数量处理
     *
     * @param
     * @return
     */
    private int amountTopHandle(String amountTop, int esTotal) {
        int topBegin = 0;
        int topEnd = 0;
        try {
            String[] split = amountTop.split(",");
            if (split != null && split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    if (i == 0) {
                        topBegin = Integer.parseInt(split[i]);
                    } else {
                        topEnd = Integer.parseInt(split[i]);
                    }
                }
                if (topBegin > esTotal) {
                    return 0;
                } else if (topBegin < esTotal && topEnd > esTotal) {
                    return esTotal - topBegin;
                } else if (topBegin < esTotal && topEnd < esTotal) {
                    return topEnd - topBegin;
                }
            }
        } catch (Exception e) {
            log.error("amountTopHandle error", e);
        }
        return 0;
    }

    /**
     * 根据条件列表最后一条流水号
     *
     * @param queryBaseBean
     * @return
     */
    @Override
    public String builderMarketingWithSwiftNumber(QueryBaseBean queryBaseBean) {
        try {
            if (StringUtils.isNotBlank(queryBaseBean.getApiCode())) {
                MarketingEsBuilder esBuilder = new MarketingEsBuilder(queryBaseBean);
                //根据批次号判断查询ES索引
                Set<String> indexSet = esBuilder.builderHistoryWithIndexSet();
                Map<String, Object> params = esBuilder.builderMarketingWithSwiftNumber();
                log.info("ES builderMarketingWithSwiftNumber indexSet:{} params:{}",
                        JSON.toJSONString(indexSet), JSON.toJSONString(params));
                String[] indexArr = indexSet.toArray(new String[indexSet.size()]);
                SearchHits hits = EsUtil.selectByTemplate(indexArr, EsConstants.PAGE_TEMPLATE, params);
                if (hits != null) {
                    for (SearchHit hit : hits) {
                        JSONObject swiftNumberObj = JSON.parseObject(hit.getSourceAsString());
                        if (swiftNumberObj != null) {
                            return swiftNumberObj.getString("swift_number");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("ES builderMarketingWithSwiftNumber error,queryBaseBean:{}", JSON.toJSONString(queryBaseBean), e);
        }
        return null;
    }

    /**
     * 列表查询带列表返参
     *
     * @param queryBaseBean
     * @return
     */
    @Override
    public List<MarketingHistory> builderMarketingWithList(QueryBaseBean queryBaseBean) {
        return builderMarketingWithList(queryBaseBean, null);
    }

    /**
     * 列表查询带列表返参
     *
     * @param queryBaseBean
     * @param columns
     * @return
     */
    @Override
    public List<MarketingHistory> builderMarketingWithList(QueryBaseBean queryBaseBean, String columns) {
        List<MarketingHistory> result = new ArrayList<>();
        try {
            if (StringUtils.isNotBlank(queryBaseBean.getApiCode())) {
                MarketingEsBuilder esBuilder = new MarketingEsBuilder(queryBaseBean);
                //根据批次号判断查询ES索引
                Set<String> indexSet = esBuilder.builderHistoryWithIndexSet();
                Map<String, Object> params = esBuilder.builderMarketingWithList(columns);
                log.info("ES builderMarketingWithList indexSet:{} params:{}",
                        JSON.toJSONString(indexSet), JSON.toJSONString(params));
                String[] indexArr = indexSet.toArray(new String[indexSet.size()]);
                SearchHits hits = EsUtil.selectByTemplate(indexArr, EsConstants.PAGE_TEMPLATE, params);
                if (hits != null) {
                    for (SearchHit hit : hits) {
                        String sourceAsString = hit.getSourceAsString();
                        MarketingHistory history = JSON.parseObject(sourceAsString, MarketingHistory.class);
                        historyJsonColumnHandle(history);
                        result.add(history);
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("ES builderMarketingWithList error,params:{}", JSON.toJSONString(queryBaseBean), e);
        }
        return result;
    }

    /**
     * 字段处理
     *
     * @param history
     * @return
     */
    private void historyJsonColumnHandle(MarketingHistory history) {
        //id解密
        history.setIdCard(BrCipherMaker.getInstance().decode(getValue(history.getIdCard())));
        //cell解密
        history.setCell(BrCipherMaker.getInstance().decode(getValue(history.getCell())));
        //name解密
        history.setName(BrCipherMaker.getInstance().decode(getValue(history.getName())));
    }

    /**
     * value置空
     *
     * @param value
     * @return
     */
    private String getValue(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}
