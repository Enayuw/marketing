package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.DataDistributeDetailLogExample;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WuBaSubmitConversionSoleProcessor {

    private final static String TITLE = "【58新客提交营销名单】";
    private Integer PARTITION_SIZE = 50;

    ThreadPoolExecutor dbActionPool = BrExecutors.getThreadPool(10, 10);

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private DataDistributeDetailLogMapper distributeLogMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public List<Long> process(List<WubaSubmitConversionData> pushList){
        List<Long> notPushIds = new ArrayList<>();
        String key = RedisKeyConstant.WUBA_SUBMIT_CONVERSION_DISTRIBUTE_DATA_SLOE_LOCK;
        Integer distributeType = DistributeTypeEnum.WUBA_SUBMIT_CONVERSION.getValue();
        Integer soleDay = 0;

        if(CollectionUtils.isEmpty(pushList)) {
            return notPushIds;
        }

        Iterator<WubaSubmitConversionData> iterator = pushList.iterator();
        long startTime = System.currentTimeMillis();
        while(iterator.hasNext()){
            WubaSubmitConversionData next = iterator.next();
            String apiCode = next.getApiCode();
            String cell = next.getCell();
            key = key.concat(String.format(":%d:%d:%s:%s", distributeType, soleDay, apiCode, cell));
            String lockValue = UUID.randomUUID().toString();
            try {
                redisChgService.lock(key, lockValue);
                String distributeDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                DataDistributeDetailLogExample logExample = new DataDistributeDetailLogExample();
                logExample.setOrderByClause(" id limit 1 ");
                DataDistributeDetailLogExample.Criteria criteria = logExample.createCriteria();
                criteria.andApiCodeEqualTo(apiCode)
                        .andDistributeTypeEqualTo(distributeType)
                        .andDistributeDateEqualTo(distributeDate)
                        .andCellEqualTo(cell)
                        ;
                List<DataDistributeDetailLog> dataDistributeDetailLogs = distributeLogMapper.selectByExample(logExample);
                if (dataDistributeDetailLogs.size() > 0) {
                    notPushIds.add(next.getId());
                    iterator.remove();
                    redisChgService.unlock(key, lockValue);
                    continue;
                } else {
                    DataDistributeDetailLog distributeLog = new DataDistributeDetailLog();
                    distributeLog.setApiCode(apiCode);
                    distributeLog.setCustNum("");
                    distributeLog.setCell(next.getCell());
                    distributeLog.setStatus("1");
                    distributeLog.setpStatus(2);
                    distributeLog.setDistributeDate(distributeDate);
                    distributeLog.setDistributeType(distributeType);
                    distributeLog.setSuccessDate(distributeDate);
                    distributeLog.setCreateTime(new Date());
                    distributeLog.setSourceId(next.getId());
                    distributeLog.setSourceType(DistributeSourceTypeEnum.TRANSFER.getValue());
                    distributeLogMapper.insertSelective(distributeLog);
                }
                redisChgService.unlock(key, lockValue);
            }catch (Exception e){
                redisChgService.unlock(key, lockValue);
            }
        }
        long endTime = System.currentTimeMillis();
        log.warn(TITLE+"去重一次的耗时："+(endTime-startTime));
        return notPushIds;
    }

    public List<Long> checkExists(List<WubaSubmitConversionData> pushList, WubaSubmitConversionData param){
        List<Long> notPushIds = new ArrayList<>();
        if(CollectionUtils.isEmpty(pushList)) {
            return notPushIds;
        }
        String apiCode = param.getApiCode();
        Integer createDate = param.getCreateDate();
        String userType = param.getUserType();
        Integer distributeType = DistributeTypeEnum.WUBA_SUBMIT_CONVERSION.getValue();
        String distributeDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // soleCellSet
        Set<String> soleCellSet = new HashSet<>();
        // allCells
        Set<String> allCells = pushList.stream().map(WubaSubmitConversionData::getCell).collect(Collectors.toSet());
        // distributeCellSet
        String marketingDate = String.valueOf(createDate);
        Set<String> distributeCellSet = distributeLogMapper.findDistributeLogCellSet(apiCode, distributeType, distributeDate,
                allCells, marketingDate, userType);
        // iterator pushList
        Iterator<WubaSubmitConversionData> iterator = pushList.iterator();
        while (iterator.hasNext()){
            WubaSubmitConversionData next = iterator.next();
            String cell = next.getCell();
            if(distributeCellSet.contains(cell) || soleCellSet.contains(cell)){
                notPushIds.add(next.getId());
                iterator.remove();
                continue;
            }
            soleCellSet.add(cell);
        }
        return notPushIds;
    }

    public void addDistributeLog(List<WubaSubmitConversionData> pushList, Page2Condition<WubaSubmitConversionData> condition) {
        Integer distributeType = DistributeTypeEnum.WUBA_SUBMIT_CONVERSION.getValue();
        String distributeDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        if(CollectionUtils.isEmpty(pushList)) {
            return;
        }
        Integer marketingDate = condition.getParam().getCreateDate();
        String userType = condition.getParam().getUserType();
        List<DataDistributeDetailLog> distributeLogList = pushList.stream().map((WubaSubmitConversionData data) -> {
            DataDistributeDetailLog distributeLog = new DataDistributeDetailLog();
            distributeLog.setApiCode(data.getApiCode());
            distributeLog.setCustNum("");
            distributeLog.setCell(data.getCell());
            distributeLog.setStatus("1");
            distributeLog.setpStatus(2);
            distributeLog.setDistributeDate(distributeDate);
            distributeLog.setDistributeType(distributeType);
            distributeLog.setSuccessDate(distributeDate);
            distributeLog.setCreateTime(new Date());
            distributeLog.setUpdateTime(new Date());
            distributeLog.setSourceId(data.getId());
            distributeLog.setSourceType(DistributeSourceTypeEnum.TRANSFER.getValue());
            distributeLog.setExtend("{\"marketingDate\":\""+ marketingDate+"\",\"userType\":\""+userType+"\"}");
            return distributeLog;
        }).collect(Collectors.toList());

        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(dbActionPool, marketingCommonConfig.getWuBaQueryConversionBatDBThreadPool());
        PARTITION_SIZE = marketingCommonConfig.getWuBaQueryConversionBatDBPartitionSize();

        List<CompletableFuture<Void>> distributeLogFutures = Lists.newArrayList();
        List<List<DataDistributeDetailLog>> distributeLogPartitions = Lists.partition(distributeLogList, PARTITION_SIZE);
        for (List<DataDistributeDetailLog> partition : distributeLogPartitions) {
            distributeLogFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    distributeLogMapper.insertBatch(partition);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                            TITLE + "批量保存去重数据异常"));
                }
            }, dbActionPool));
        }
        CompletableFuture.allOf(distributeLogFutures.toArray(new CompletableFuture[0])).join();
        log.warn(TITLE + "批量保存去重数据成功");
    }
}
