package com.br.marketing.service.mark.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.mark.FlagDataCarryLogCell;
import com.br.marketing.entity.*;
import com.br.marketing.entity.mark.MarketingConditionVariant;
import com.br.marketing.enums.DataMarkEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.mapper.DataMarkConfigMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataHighRiskMarkService;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @description 高风险打标实现
 * @author hedongshuo
 * @date 2025/2/19 21:30
 **/
@Service
@Slf4j
public class DataHighRiskMarkServiceImpl implements DataHighRiskMarkService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    RedisChgService redisChgService;

    @Resource
    FlagDataMapper flagDataMapper;

    @Resource
    DataMarkConfigMapper markConfigMapper;

    @Resource
    DataMarkCommonService dataMarkCommonService;

    private final static Integer splitNum = 1500;
    private final static Integer esPageSize = 2000;

    @Override
    public void process(String scoreDate) {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            //1.查询跑分任务表
            StraHisFile straHisFile = dataMarkCommonService.getStraHisFile(apiCode, scoreDate);
            if (null == straHisFile) {
                return;
            }
            //2.创建线程池
            ThreadPoolExecutor threadPool = dataMarkCommonService.getThreadPoolExecutor(true);
            //3.打标主流程
            markProcess(apiCode, straHisFile, threadPool);
        });
    }

    /**
     * @description 打标主流程
     * @param apiCode
     * @param straHisFile
     * @param threadPool
     */
    private void markProcess(String apiCode, StraHisFile straHisFile, ThreadPoolExecutor threadPool) {
        //获取打标配置[b_data_mark_config]
        List<DataMarkConfig> markConfigs = getMarkConfigs(apiCode);
        //按mark_out_value_type排序，按mark_out_field分组
        Map<String, List<DataMarkConfig>> markCOnfigsGroupMap =
                markConfigs.stream().sorted(Comparator.comparing(DataMarkConfig::getMarkOutValueType)).collect(Collectors.toList())
                        .stream().collect(Collectors.groupingBy(DataMarkConfig::getMarkOutField));
        String key = RedisKeyConstant.prefix.concat(DataMarkEnum.MARK_HIGHRISK.getMarkRedisKey()).concat(":").concat(apiCode);
        List<Long> ids = new ArrayList<>();
        for (; ; ) {
            String lockValue = UUID.randomUUID().toString();
            try{
                //1.抢锁
                redisChgService.lock(key, lockValue);
                //2.查数据
                List<FlagDataCarryLogCell> flagDataList = getFlagData(apiCode);
                if (CollectionUtils.isEmpty(flagDataList)) {
                    redisChgService.unlock(key, lockValue);
                    dataMarkCommonService.threadPoolShutDown(threadPool, "pp停车高风险&白名单打标");
                    break;
                }
                ids = flagDataList.stream().map(FlagDataCarryLogCell::getId).collect(Collectors.toList());
                //3.更新数据
                flagDataMapper.batchUpdateHighRiskStatusByIds(ids, 0, 0);
                //4.释放锁
                redisChgService.unlock(key, lockValue);
                //5.数据拆分，打标
                markWithThread(apiCode, straHisFile, flagDataList, markCOnfigsGroupMap, threadPool);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                        "pp停车高风险&白名单打标流程出现异常，" + "errorMessage=" + e.getMessage()), e);
                flagDataMapper.batchUpdateHighRiskStatusByIds(ids, null, null);
                redisChgService.unlock(key, lockValue);
                dataMarkCommonService.threadPoolShutDown(threadPool, "pp停车高风险&白名单打标");
                break;
            }

        }
    }

    /**
     * @param apiCode
     * @param straHisFile
     * @param flagDataList
     * @param markCOnfigsGroupMap
     * @param threadPool
     * @return void
     * @description 通过线程拆分数据，打标
     * @author hedongshuo
     * @date 2025/2/21 15:48
     **/
    private void markWithThread(String apiCode, StraHisFile straHisFile, List<FlagDataCarryLogCell> flagDataList,
                                Map<String, List<DataMarkConfig>> markCOnfigsGroupMap, ThreadPoolExecutor threadPool) {
        dataMarkCommonService.modifyCorePoolSize(threadPool, true);
        List<List<FlagDataCarryLogCell>> partitions = Lists.partition(flagDataList, splitNum);
        partitions.forEach((List <FlagDataCarryLogCell> flagDataCarryLogCells) -> {
            threadPool.submit(() -> {
                List<Long> ids = flagDataCarryLogCells.stream().map(FlagDataCarryLogCell::getId).collect(Collectors.toList());
                try {
                    markForThread(apiCode, straHisFile, flagDataCarryLogCells, markCOnfigsGroupMap);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                            "pp停车高风险&白名单打标子线程流程中出现异常，" + "errorMessage=" + e.getMessage()), e);
                    flagDataMapper.batchUpdateHighRiskStatusByIds(ids, null, null);
                }
            });
        });
        
    }

    /**
     * @param apiCode
     * @param straHisFile
     * @param flagDataCarryLogCells
     * @param markCOnfigsGroupMap
     * @return void
     * @description 在线程中对数据打标
     * @author hedongshuo
     * @date 2025/2/21 16:00
     **/
    private void markForThread(String apiCode, StraHisFile straHisFile, List<FlagDataCarryLogCell> flagDataCarryLogCells,
                               Map<String, List<DataMarkConfig>> markCOnfigsGroupMap) throws Exception {
        List<String> cellLogs = flagDataCarryLogCells.stream().map(FlagDataCarryLogCell::getCellLog).collect(Collectors.toList())
                .stream().distinct().collect(Collectors.toList());
        //1.查询es
        List<MarketingHistory> marketingHistories;
        Map<String, Object> markEsMockConfig = marketingCommonConfig.getDataMarkEsMockConfig();
        long start = System.currentTimeMillis();
        if ((Boolean) markEsMockConfig.get("isMock")) {
            List<JSONObject> jsonList = (List<JSONObject>) markEsMockConfig.get("marketingHistories");
            marketingHistories = jsonList.stream().map(jsonObject -> new ObjectMapper().convertValue(jsonObject, MarketingHistory.class))
                    .collect(Collectors.toList());
        } else {
            marketingHistories =
                    dataMarkCommonService.getScoreWithEs(apiCode, straHisFile.getBatchNumber(), straHisFile.getId(), cellLogs, esPageSize, false);
        }
        long afterEs = System.currentTimeMillis();
        log.warn("pp高风险打标job-查询es，耗时：{}s", (afterEs - start) / 1000);
        if (CollectionUtils.isEmpty(marketingHistories)){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    "pp停车高风险&白名单打标子线程es未返回数据！"));
            return;
        }
        if (marketingHistories.size() != cellLogs.size()) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PP_MARKING_SERVICEERROR.getCode(),
                    "pp停车高风险&白名单打标子线程es返回数据条数与待打标数据条数不符！"));
        }
        //把数据处理成Map<cell, List<MarketingCondition>>
        Map<String, List<MarketingCondition>> conditionMapOri =
                marketingHistories.stream()
                        .collect(Collectors.toMap(MarketingHistory::getCell, MarketingHistory::getCondition, (o1, o2) -> o2));
        //把List<MarketingCondition>处理成Map格式，方便spel表达式使用
        Map<String, Map<String, Object>> conditionMap = new HashMap<>();
        for (String cell : conditionMapOri.keySet()) {
            List<MarketingCondition> marketingConditions = conditionMapOri.get(cell);
//            List<JSONObject> jsonList = marketingConditions.stream()
//                    .map(marketingCondition -> new ObjectMapper().convertValue(marketingCondition, JSONObject.class))
//                    .collect(Collectors.toList());
//            List<MarketingConditionVariant> marketingConditionVariants = jsonList.stream()
//                    .map(jsonObject -> new ObjectMapper().convertValue(jsonObject, MarketingConditionVariant.class))
//                    .collect(Collectors.toList());
//            Map<String, Object> condition =
//                    marketingConditionVariants.stream().collect(
//                            Collectors.toMap(MarketingConditionVariant::getFieldKey, MarketingConditionVariant::doubleConvert, (o1, o2) -> o2));
            List<MarketingConditionVariant> marketingConditionVariants = marketingConditions.stream()
                    .map(marketingCondition -> new ObjectMapper().convertValue(marketingCondition, MarketingConditionVariant.class))
                    .collect(Collectors.toList());
            Map<String, Object> condition =
                    marketingConditionVariants.stream().collect(
                            Collectors.toMap(MarketingConditionVariant::getFieldKey, MarketingConditionVariant::doubleConvert, (o1, o2) -> o2));
            conditionMap.put(cell, condition);
        }
        long afterProcessData = System.currentTimeMillis();
        log.warn("pp高风险打标job-es数据处理，耗时：{}s", (afterProcessData - afterEs) / 1000);
        //2.打标
        //遍历每一条待打标数据
        for (FlagDataCarryLogCell flagDataCarryLogCell : flagDataCarryLogCells) {
            FlagData flagData = new FlagData();
            flagData.setId(flagDataCarryLogCell.getId());
            Class<FlagData> flagDataClass = FlagData.class;
            //对于一条打标数据，es返回的跑分分值
            Map scoreMap = conditionMap.get(flagDataCarryLogCell.getCellLog());
            //将客群标志加到condition中
            scoreMap.put("flag_riskgroup", flagDataCarryLogCell.getFlagRiskgroup());
            //遍历Map<data属性名, 配置list>
            for (String markOutField : markCOnfigsGroupMap.keySet()) {
                List<DataMarkConfig> dataMarkConfigs = markCOnfigsGroupMap.get(markOutField);
                //todo 目前标记字段类型都是整形，后续有其他类型标记，代码需要修改
                Integer markOutValue = null;
                //遍历配置List，理论上最后一条是默认值
                for (DataMarkConfig dataMarkConfig : dataMarkConfigs) {
                    if (dataMarkConfig.getMarkOutValueType() == 1
                            || dataMarkCommonService.isMatch(scoreMap, dataMarkConfig.getMarkCondition())) {
                        markOutValue = Integer.parseInt(dataMarkConfig.getMarkOutValue());
                        break;
                    }
                }
                Field declaredField = flagDataClass.getDeclaredField(markOutField);
                declaredField.setAccessible(true);
                //给flagData的属性declaredField赋值markOutValue
                declaredField.set(flagData, markOutValue);
            }
            flagData.setFlagHighRiskComputation(1);
            flagData.setFlagWhitelistComputation(1);
            flagDataMapper.updateByPrimaryKeySelective(flagData);
        }
        long afterMark = System.currentTimeMillis();
        log.warn("pp高风险打标job-数据打标，耗时：{}s", (afterMark - afterProcessData) / 1000);
    }

    /**
     * @description 获取打标配置
     * @param apiCode
     * @return List<DataMarkConfig>
     * @author hedongshuo
     * @date 2025/2/21 11:11
     **/
    private List<DataMarkConfig> getMarkConfigs(String apiCode) {
        DataMarkConfigExample markConfigExample = new DataMarkConfigExample();
        markConfigExample.createCriteria()
                .andIsDelEqualTo(0)
                .andApiCodeEqualTo(apiCode)
                .andMarkTypeIn(Arrays.asList(DataMarkEnum.MARK_HIGHRISK.getMarkType(), DataMarkEnum.MARK_WHITELIST.getMarkType()));
        return markConfigMapper.selectByExample(markConfigExample);
    }

    /**
     * @description 查询当天未打标的数据
     * @param apiCode
     * @return java.util.List<com.br.marketing.entity.FlagData>
     * @author hedongshuo
     * @date 2025/2/20 17:36
     **/
    private List<FlagDataCarryLogCell> getFlagData(String apiCode) {
        Integer dataMarkPageSize = marketingCommonConfig.getDataMarkPageSize();
        return flagDataMapper.queryLogCellByDate(
                apiCode,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dataMarkPageSize);
    }

}
