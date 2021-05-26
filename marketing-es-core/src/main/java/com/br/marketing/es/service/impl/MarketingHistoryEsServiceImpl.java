package com.br.marketing.es.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.EsConstants;
import com.br.marketing.es.util.MarketingEsBuilder;
import com.br.marketing.es.util.SwiftNumberManager;
import com.br.marketing.es.util.es.EsHandleUtil;
import com.br.marketing.es.util.es.EsUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
    public int builderMarketingCount(QueryBaseBean queryBaseBean) {
        try {
            if (StringUtils.isNotBlank(queryBaseBean.getApiCode())) {
                MarketingEsBuilder esBuilder = new MarketingEsBuilder(queryBaseBean);
                //根据时间判断查询ES索引
                Set<String> indexSet = esBuilder.builderHistoryWithIndexSet();
                Map<String, Object> paramsCount = new HashMap<>();
                // esBuilder.historyWhereCount(paramsCount);
                log.info("ES builderMarketingCount indexSet:{}", JSON.toJSONString(indexSet));
                String[] indexArr = indexSet.toArray(new String[indexSet.size()]);
                //查询数量
                return (int) EsUtil.selectByTemplateCount(indexArr, EsConstants.PAGE_TEMPLATE, paramsCount);
            }
        } catch (Exception e) {
            log.error("ES builderMarketingCount error,params:{}", JSON.toJSONString(queryBaseBean), e);
        }
        return 0;
    }


    public static void main(String[] args) {
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setTaskStartTime(new Date());
        Date date = new Date();
        Calendar next = Calendar.getInstance();
        next.setTime(date);
        next.add(Calendar.MONTH, 5);
        date = next.getTime();
        queryBaseBean.setTaskEndTime(date);
        MarketingEsBuilder esBuilder = new MarketingEsBuilder(queryBaseBean);
        Set<String> indexSet = esBuilder.builderHistoryWithIndexSet();
        System.out.println(JSON.toJSON(indexSet));

    }
}
