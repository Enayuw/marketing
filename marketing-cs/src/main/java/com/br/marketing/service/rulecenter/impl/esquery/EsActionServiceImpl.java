package com.br.marketing.service.rulecenter.impl.esquery;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.service.rulecenter.IEsActionService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EsActionServiceImpl implements IEsActionService {

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;


    @Override
    public Integer getTotal(QueryBaseBean queryBaseBean) {
        List<String> ruleCenterPushView = marketingCommonConfig.getRuleCenterPushView();
        Long start = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(ruleCenterPushView)
                && ruleCenterPushView.contains(queryBaseBean.getApiCode())) {
            Integer total = marketingHistoryEsService.builderMarketingWithFilterTotal(queryBaseBean);
            Long end = System.currentTimeMillis();
            log.warn("getTotal pushReview with filter: {} time: {}", queryBaseBean.getApiCode(), end - start);
            return total;
        }
        Integer total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        Long end = System.currentTimeMillis();
        log.warn("getTotal pushReview with must: {} time: {}", queryBaseBean.getApiCode(), end - start);
        return total;
    }

    @Override
    public Integer getTotal(QueryBaseBean queryBaseBean, String apiCode, Integer pushType) {
        Map<String, JSONObject> ruleCenterPushType = marketingCommonConfig.getRuleCenterPushType();
        JSONObject type = ruleCenterPushType.get(apiCode);
        Long start = System.currentTimeMillis();
        if (ObjectUtil.isNotEmpty(type)
                && "filter".equals(type.getString(pushType.toString()))) {
            Integer total = marketingHistoryEsService.builderMarketingWithFilterTotal(queryBaseBean);
            Long end = System.currentTimeMillis();
            log.warn("getTotal job with filter: {} time: {}", queryBaseBean.getApiCode(), end - start);
            return total;
        }
        Integer total = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
        Long end = System.currentTimeMillis();
        log.warn("getTotal job with must: {} time: {}", queryBaseBean.getApiCode(), end - start);
        return total;
    }


    @Override
    public List<MarketingHistory> getMarketingHistorys(QueryBaseBean queryBaseBean, String apiCode, Integer pushType) {
        Map<String, JSONObject> ruleCenterPushType = marketingCommonConfig.getRuleCenterPushType();
        Long start = System.currentTimeMillis();
        JSONObject type = ruleCenterPushType.get(apiCode);
        if (ObjectUtil.isNotEmpty(type)
                && "filter".equals(type.getString(pushType.toString()))) {
            List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithFilterList(queryBaseBean);
            Long end = System.currentTimeMillis();
            log.warn("getList job with filter: {} time: {}", queryBaseBean.getApiCode(), end - start);
            return marketingHistories;
        }
        List<MarketingHistory> marketingHistories = marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
        Long end = System.currentTimeMillis();
        log.warn("getList job with must: {} time: {}", queryBaseBean.getApiCode(), end - start);
        return marketingHistories;
    }
}
