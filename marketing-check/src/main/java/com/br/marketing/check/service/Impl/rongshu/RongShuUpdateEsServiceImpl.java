package com.br.marketing.check.service.Impl.rongshu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.es.util.es.EsIceType;
import com.br.marketing.es.util.es.rpcclient.RpcClientProxy;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.EsNewIndexRuleUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RongShuUpdateEsServiceImpl implements RongShuUpdateEsService {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    MarketingHistoryEsServiceImpl marketingHistoryEsService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public static final String Numeric = "^(\\d)+(\\.){1}(\\d)+$|^(\\d)+$";


    @Override
    public void updateESCycleDataByRS(String apiCode) {

        StraHisFile straHisFile;
        straHisFile = straHisFileMapper.getLastTaskByApiCode(apiCode);

        Integer parNum = straHisFile.getIndexNum();
        String batchNumber = straHisFile.getBatchNumber();
        String index = EsNewIndexRuleUtils.indexForModify(batchNumber, straHisFile, marketingCommonConfig);
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(50, 50, 50);
        ThreadPoolExecutor queryPool = BrExecutors.getThreadPool(3, 3, 1);
        HashMap<Integer, Integer> partDataNum = new HashMap<>();
        Integer nowSum = 0;
        for (Integer i = 0; i < parNum; i++) {
            QueryBaseBean queryBaseBean = new QueryBaseBean();
            queryBaseBean.setApiCode(straHisFile.getApiCode());
            queryBaseBean.setBatchNumbers(straHisFile.getBatchNumber());
            queryBaseBean.setFileIds(straHisFile.getId().toString());
            queryBaseBean.setPart(i.toString());
            queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(Collections.singletonList(straHisFile), marketingCommonConfig));
            Integer nowNum = marketingHistoryEsService.builderMarketingWithTotal(queryBaseBean);
            partDataNum.put(i, nowNum);
            nowSum += nowNum;
        }
        log.warn("apiCode={}开始更新ES数据，量级=:{},batchNumber={}", apiCode, nowSum, batchNumber);
        List<Future<Boolean>> futureList = new ArrayList<>();
        for (int i = 0; i < parNum; i++) {
            int finalI = i;
            futureList.add(queryPool.submit(() ->
                    updateEsPartData(straHisFile, finalI, partDataNum.get(finalI), index, pool)
            ));
        }
        // 2. 等待外层任务完成（包括其提交的内层任务）
        for (Future<Boolean> future : futureList) {
            try {
                future.get(); // 阻塞直到外层任务完成（包括内层任务）
            } catch (Exception e) {
                log.error("榕树跑分数据周期清洗-线程池等待任务完成异常", e);
            }
        }
        //关闭外层线程池
        queryPool.shutdown();
        //关闭内层线程池
        pool.shutdown();
        try {
            while (!queryPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("榕树跑分查询线程池关闭");
            }
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("榕树跑分数据周期清洗线程池关闭");
            }
        } catch (InterruptedException ex) {
            pool.shutdownNow();
            queryPool.shutdownNow();
            log.warn("榕树跑分数据周期清洗线程池关闭！异常", ex);
            Thread.currentThread().interrupt();
        }
        log.warn("榕树跑分数据周期清洗完成");

    }

    private Boolean updateEsPartData(StraHisFile straHisFile, Integer part, Integer partDataNum, String index, ThreadPoolExecutor pool) {

        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(straHisFile.getApiCode());
        queryBaseBean.setBatchNumbers(straHisFile.getBatchNumber());
        queryBaseBean.setFileIds(straHisFile.getId().toString());
        queryBaseBean.setPart(part.toString());
        queryBaseBean.setUseNewIndexRule(EsNewIndexRuleUtils.resolveAsMap(Collections.singletonList(straHisFile), marketingCommonConfig));

        Integer pageSize = 2000;
        Integer total = partDataNum;
        int totalYuShu = total % pageSize;
        String searchAfterStr = "";
        int totalPage = total / pageSize + (totalYuShu > 0 ? 1 : 0);
        log.warn("任务id：{}，当前片：{}，总数：{}，页数：{}", straHisFile.getId(), part, total, totalPage);
        for (int i = 1; i <= totalPage; i++) {
            String sn = String.valueOf(i);
            if (i == totalPage && totalYuShu > 0) {
                queryBaseBean.setPageSize(totalYuShu);
            } else {
                queryBaseBean.setPageSize(pageSize);
            }
            queryBaseBean.setSearchAfter(searchAfterStr);
            List<Map<String, MarketingHistory>> marketingHistoryMapList = marketingHistoryEsService.builderMarketingWithIdList(queryBaseBean,
                    null, false);
            log.warn("ES中查询的数据量级为num={}", marketingHistoryMapList.size());
            if (!CollectionUtils.isEmpty(marketingHistoryMapList)) {
                MarketingHistory marketingHistory = marketingHistoryMapList.get(marketingHistoryMapList.size() - 1).values().stream().findFirst().get();
                searchAfterStr = marketingHistory.getSearchAfter();
            }
            pool.submit(() ->
                    updateRsEsData(marketingHistoryMapList, index)
            );
        }
        return Boolean.TRUE;


    }


    private void updateRsEsData(List<Map<String, MarketingHistory>> marketingHistoryMapList, String index) {

        try {
            for (Map<String, MarketingHistory> marketingHistoryMap : marketingHistoryMapList) {
                for (Map.Entry<String, MarketingHistory> entry : marketingHistoryMap.entrySet()) {
                    MarketingHistory marketingHistory = entry.getValue();
                    List<MarketingCondition> marketingConditions = marketingHistory.getCondition();
                    UpdateRSCondition(marketingConditions);
                    JSONObject params = JSON.parseObject(JSON.toJSONString(marketingHistory));
                    params.put("_id", entry.getKey());
                    RpcClientProxy.modify(index, params, EsIceType.EE.getCode(), EsIceType.R_FALSE.getCode(),
                            EsIceType.MARKETING.getCode());
                }
            }
        } catch (Exception e) {
            log.error("榕树跑分数据周期清洗更新ES出错", e);
        }

    }


    private void UpdateRSCondition(List<MarketingCondition> marketingConditions) {
        String fk_score_rh = "";
        List<MarketingCondition> scorescashonzcwjjtyfkList = marketingConditions.stream().filter(condition ->
                "scorescashonzcwjjtyfk1112".equals(condition.getFieldKey())).collect(Collectors.toList());
        List<MarketingCondition> scorescashonscrsyxfList = marketingConditions.stream().filter(condition ->
                "scorescashonscrsyxf".equals(condition.getFieldKey())).collect(Collectors.toList());
        if ((!CollectionUtils.isEmpty(scorescashonzcwjjtyfkList)) && (!CollectionUtils.isEmpty(scorescashonscrsyxfList))) {
            Double scorescashonzcwjjtyfkNum = scorescashonzcwjjtyfkList.get(0).getDValue();
            Double scorescashonscrsyxf = scorescashonscrsyxfList.get(0).getDValue();
            if ((!ObjectUtils.isEmpty(scorescashonzcwjjtyfkNum)) && (!ObjectUtils.isEmpty(scorescashonscrsyxf))) {
                fk_score_rh = String.valueOf(Math.round(scorescashonzcwjjtyfkNum * 0.4 + ((scorescashonscrsyxf - 300) / 700) * 100 * 0.6));

            }
        }
        addOrUpdateCustypeCondition(marketingConditions, "fk_score_rh", fk_score_rh);
        String sx_score_rh = "";
        List<MarketingCondition> scorescashonzcwjjtysxList = marketingConditions.stream().filter(condition ->
                "scorescashonzcwjjtysx1112".equals(condition.getFieldKey())).collect(Collectors.toList());
        if ((!CollectionUtils.isEmpty(scorescashonzcwjjtysxList)) && (!CollectionUtils.isEmpty(scorescashonscrsyxfList))) {
            Double scorescashonzcwjjtysxNum = scorescashonzcwjjtysxList.get(0).getDValue();
            Double scorescashonscrsyxf = scorescashonscrsyxfList.get(0).getDValue();
            if ((!ObjectUtils.isEmpty(scorescashonzcwjjtysxNum)) && (!ObjectUtils.isEmpty(scorescashonscrsyxf))) {
                sx_score_rh = String.valueOf(Math.round(scorescashonzcwjjtysxNum * 0.3 + ((scorescashonscrsyxf - 300) / 700) * 100 * 0.7));
            }
        }
        addOrUpdateCustypeCondition(marketingConditions, "sx_score_rh", sx_score_rh);

        String ged_score_rh = "";
        List<MarketingCondition> scorescashonzcwjjgedfkList = marketingConditions.stream().filter(condition ->
                "scorescashonzcwjjgedfk1112".equals(condition.getFieldKey())).collect(Collectors.toList());
        if ((!CollectionUtils.isEmpty(scorescashonzcwjjgedfkList)) && (!CollectionUtils.isEmpty(scorescashonscrsyxfList))) {
            Double scorescashonzcwjjgedfkNum = scorescashonzcwjjgedfkList.get(0).getDValue();
            Double scorescashonscrsyxf = scorescashonscrsyxfList.get(0).getDValue();
            if ((!ObjectUtils.isEmpty(scorescashonzcwjjgedfkNum)) && (!ObjectUtils.isEmpty(scorescashonscrsyxf))) {
                ged_score_rh = String.valueOf(Math.round(scorescashonzcwjjgedfkNum * 0.4 + ((scorescashonscrsyxf - 300) / 700) * 100 * 0.6));
            }
        }
        addOrUpdateCustypeCondition(marketingConditions, "ged_score_rh", ged_score_rh);
    }

    private void addOrUpdateCustypeCondition(List<MarketingCondition> conditions, String key, String s) {
        boolean isExist = false;
        for (MarketingCondition condition : conditions) {
            if (key.equals(condition.getFieldKey())) {
                condition.setStrValue(s);
                if (Pattern.compile(Numeric).matcher(s).matches()) {
                    condition.setDValue(Double.valueOf(s));

                }
                isExist = true;
                break;
            }
        }
        // 如果 "key" 条件不存在，则添加新的条件
        if (!isExist) {
            MarketingCondition newCondition = new MarketingCondition();
            newCondition.setFieldKey(key);
            newCondition.setStrValue(s);
            if (Pattern.compile(Numeric).matcher(s).matches()) {
                newCondition.setDValue(Double.valueOf(s));

            }
            conditions.add(newCondition);
        }
    }
}
