package com.br.marketing.service.mark.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.mark.FlagDataCarryLogCell;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DataMarkEnum;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.service.impl.MarketingHistoryEsServiceImpl;
import com.br.marketing.mapper.DataMarkConfigMapper;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.mark.DataHighRiskMarkService;
import com.br.marketing.service.mark.DataMarkCommonService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
    StraHisFileMapper straHisFileMapper;

    @Resource
    RedisChgService redisChgService;

    @Resource
    FlagDataMapper flagDataMapper;

    @Resource
    DataMarkConfigMapper markConfigMapper;

    @Resource
    DataMarkCommonService dataMarkCommonService;

    @Resource
    MarketingHistoryEsService marketingHistoryEsService;

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
                markWithThread(apiCode, straHisFile, flagDataList, threadPool);

            } catch (Exception e) {

            }

        }
    }

    /**
     * @description 通过线程拆分数据，打标
     * @param apiCode
     * @param straHisFile
     * @param flagDataList
     * @param threadPool
     * @return void
     * @author hedongshuo
     * @date 2025/2/21 15:48
     **/
    private void markWithThread(String apiCode, StraHisFile straHisFile, List<FlagDataCarryLogCell> flagDataList, ThreadPoolExecutor threadPool) {
        List<List<FlagDataCarryLogCell>> partition = Lists.partition(flagDataList, splitNum);
        partition.forEach((List <FlagDataCarryLogCell> flagDataCarryLogCells) -> {
            threadPool.submit(() -> {
                markForThread(apiCode, straHisFile, flagDataCarryLogCells);
            });
        });
        
    }

    /**
     * @description 在线程中对数据打标
     * @param apiCode
     * @param straHisFile
     * @param flagDataCarryLogCells
     * @return void
     * @author hedongshuo
     * @date 2025/2/21 16:00
     **/
    private void markForThread(String apiCode, StraHisFile straHisFile, List<FlagDataCarryLogCell> flagDataCarryLogCells) {
        //1.查询es
        List<String> cellLogs = flagDataCarryLogCells.stream().map(FlagDataCarryLogCell::getCellLog).collect(Collectors.toList());
        List<MarketingHistory> marketingHistories =
                dataMarkCommonService.getScoreWithEs(apiCode, straHisFile.getBatchNumber(), straHisFile.getId(), cellLogs, esPageSize);
        //2.打标



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
        flagDataMapper.batchUpdateHighRiskStatusById(ids, 0);
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
        return flagDataMapper.queryLogCellByDatebI_(
                apiCode,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dataMarkPageSize);
    }

}
