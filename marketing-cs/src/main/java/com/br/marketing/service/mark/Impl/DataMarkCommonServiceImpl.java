package com.br.marketing.service.mark.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DataMarkConfig;
import com.br.marketing.entity.DataMarkConfigExample;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.mapper.DataMarkConfigMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DataMarkCommonServiceImpl implements DataMarkCommonService {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    MarketingHistoryEsService marketingHistoryEsService;

    @Resource
    DataMarkConfigMapper markConfigMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public StraHisFile getStraHisFile(String apiCode, String scoreDate) {
        if (StringUtils.isEmpty(scoreDate)) {
            scoreDate = LocalDate.now().toString();
        }
        List<StraHisFile> straHisFiles = straHisFileMapper.getLatestRecord(apiCode, scoreDate);
        if (CollectionUtils.isEmpty(straHisFiles)) {
            return null;
        }
        return straHisFiles.get(0);
    }

    @Override
    public List<MarketingHistory> getScoreWithEs(String apiCode, String batchNumber, Long id, List<String> cellLogs, Integer esPageSize) {
        QueryBaseBean queryBaseBean = new QueryBaseBean();
        queryBaseBean.setApiCode(apiCode);
        queryBaseBean.setBatchNumbers(batchNumber);
        queryBaseBean.setFileIds(id.toString());
        JSONObject cellCondition = new JSONObject();
        cellCondition.put("type", "operation");
        cellCondition.put("key", "cell");
        cellCondition.put("operation", "in");
        cellCondition.put("value", cellLogs);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(cellCondition);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", jsonArray);
        queryBaseBean.setJsonData(jsonObject.toString());
        queryBaseBean.setPageSize(esPageSize);
        return marketingHistoryEsService.builderMarketingWithList(queryBaseBean);
    }

    @Override
    public List<DataMarkConfig> getMarkConfigs(String apiCode, Integer markType) {
        DataMarkConfigExample markConfigExample = new DataMarkConfigExample();
        markConfigExample.createCriteria()
                .andIsDelEqualTo(0)
                .andApiCodeEqualTo(apiCode)
                .andMarkTypeEqualTo(markType);
        return markConfigMapper.selectByExample(markConfigExample);
    }

    @Override
    public ThreadPoolExecutor getThreadPoolExecutor(Boolean isUsedByEs) {
        Integer threadPoolSize;
        if (isUsedByEs) {
            threadPoolSize = marketingCommonConfig.getDataMarkESThreadNum();
        } else {
            threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
        }
        return BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
    }

    @Override
    public void threadPoolShutDown(ThreadPoolExecutor threadPool, String logPrefix) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info(logPrefix + "-线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    logPrefix + "线程作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Boolean isMatch(Map scoreMap, String condition) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(scoreMap);
        ExpressionParser parser = new SpelExpressionParser();
        Boolean isMatch = parser.parseExpression(condition).getValue(context, Boolean.class);
        return isMatch;
    }


}
