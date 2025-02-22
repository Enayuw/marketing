package com.br.marketing.service.mark.Impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.mark.FlagDataCarryLogCell;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DataMarkEnum;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.mapper.DataMarkConfigMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.service.mark.DataHighRiskMarkService;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
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
    public void process() {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            //1.查询跑分任务表
            StraHisFile straHisFile = dataMarkCommonService.getStraHisFile(apiCode);
            if (null == straHisFile) {
                return;
            }
            //2.创建线程池
            Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
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
        Map<String, List<DataMarkConfig>> markCOnfigsGroupMap =
                markConfigs.stream().sorted(Comparator.comparing(DataMarkConfig::getMarkOutValueType)).collect(Collectors.toList())
                        .stream().collect(Collectors.groupingBy(DataMarkConfig::getMarkOutField));
        String key = RedisKeyConstant.prefix.concat(DataMarkEnum.MARK_HIGHRISK.getMarkRedisKey()).concat(":").concat(apiCode);
        for (; ; ) {
            String lockValue = UUID.randomUUID().toString();
            try{
                //1.抢锁
                redisChgService.lock(key, lockValue);
                //2.查数据
                List<FlagDataCarryLogCell> flagDataList = getFlagData(apiCode, straHisFile);
                if (CollectionUtils.isEmpty(flagDataList)) {
                    redisChgService.unlock(key, lockValue);
                    break;
                }
                //3.更新数据
                updateFlagData(flagDataList);
                //4.释放锁
                redisChgService.unlock(key, lockValue);
                //5.数据拆分，打标
                markWithThread(apiCode, straHisFile, flagDataList, markCOnfigsGroupMap, threadPool);
            } catch (Exception e) {

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
        List<List<FlagDataCarryLogCell>> partition = Lists.partition(flagDataList, splitNum);
        partition.forEach((List <FlagDataCarryLogCell> flagDataCarryLogCells) -> {
            threadPool.submit(() -> {
                try {
                    markForThread(apiCode, straHisFile, flagDataCarryLogCells, markCOnfigsGroupMap);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
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
                               Map<String, List<DataMarkConfig>> markCOnfigsGroupMap) throws NoSuchFieldException, IllegalAccessException {
        //1.查询es
        List<String> cellLogs = flagDataCarryLogCells.stream().map(FlagDataCarryLogCell::getCellLog).collect(Collectors.toList())
                .stream().distinct().collect(Collectors.toList());
        List<MarketingHistory> marketingHistories =
                dataMarkCommonService.getScoreWithEs(apiCode, straHisFile.getBatchNumber(), straHisFile.getId(), cellLogs, esPageSize);
        //把数据处理成Map<cell, List<MarketingCondition>>
        Map<String, List<MarketingCondition>> conditionMapOri =
                marketingHistories.stream().collect(Collectors.toMap(MarketingHistory::getCell, MarketingHistory::getCondition));
        Map<String, Map<String, String>> conditionMap = new HashMap<>();
        for (String cell : conditionMapOri.keySet()) {
            List<MarketingCondition> marketingConditions = conditionMapOri.get(cell);
            Map<String, String> condition =
                    marketingConditions.stream().collect(Collectors.toMap(MarketingCondition::getFieldKey, MarketingCondition::getStrValue));
            conditionMap.put(cell, condition);
        }
        //2.打标
        //遍历每一条待打标数据
        for (FlagDataCarryLogCell flagDataCarryLogCell : flagDataCarryLogCells) {
            FlagData flagData = new FlagData();
            Class<FlagData> flagDataClass = FlagData.class;
            flagData.setId(flagDataCarryLogCell.getId());
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
                    }
                }
                Field declaredField = flagDataClass.getDeclaredField(markOutField);
                declaredField.setAccessible(true);
                declaredField.set(flagData, markOutValue);
            }
            flagData.setFlagHighRiskComputation(1);
            flagData.setFlagWhitelistComputation(1);
            flagDataMapper.updateByPrimaryKeySelective(flagData);
        }
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
                .andIsDelEqualTo(1)
                .andApiCodeEqualTo(apiCode)
                .andMarkTypeIn(Arrays.asList(DataMarkEnum.MARK_HIGHRISK.getMarkType(), DataMarkEnum.MARK_WHITELIST.getMarkType()));
        return markConfigMapper.selectByExample(markConfigExample);
    }

    /**
     * @description 将数据状态更新
     * @param flagDataList
     * @return void
     * @author hedongshuo
     * @date 2025/2/20 20:59
     **/
    private void updateFlagData(List<FlagDataCarryLogCell> flagDataList) {
        List<Long> ids = flagDataList.stream().map(FlagDataCarryLogCell::getId).collect(Collectors.toList());
        flagDataMapper.batchUpdateHighRiskStatusById(ids, 0, 0);
    }

    /**
     * @description 查询当天未打标的数据
     * @param apiCode
     * @param straHisFile
     * @return java.util.List<com.br.marketing.entity.FlagData>
     * @author hedongshuo
     * @date 2025/2/20 17:36
     **/
    private List<FlagDataCarryLogCell> getFlagData(String apiCode, StraHisFile straHisFile) {
        Integer dataMarkPageSize = marketingCommonConfig.getDataMarkPageSize();
        return flagDataMapper.queryLogCellByDate(
                apiCode,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dataMarkPageSize);
    }

}
