package com.br.marketing.xc.job;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataHitRequestNoInit;
import com.br.marketing.entity.XieChengCollidingDataHitRequestNoMapping;
import com.br.marketing.mapper.XieChengCollidingDataHitRequestNoInitMapper;
import com.br.marketing.mapper.XieChengCollidingDataHitRequestNoMappingMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 2025-11-26 业务暂停
 * @Description 携程撞库一次性初始化撞库流水号作业
 * @Author hong.chen
 * @CreateTime 2024/09/25
 */
@Component
@Slf4j
public class XieChengCollidingHitRequestNoInitJob extends AbstractSimpleElasticJob {
    @Resource
    private XieChengCollidingDataHitRequestNoInitMapper initMapper;
    @Resource
    private XieChengCollidingDataHitRequestNoMappingMapper mappingMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    private final static int PARTATION_SIZE = 50;
    @Resource
    private XieChengServiceNew xieChengServiceNew;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        process();
        log.warn("携程撞库一次性初始化撞库流水号作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }

    private void process() {
        // 创建线程池
        ThreadPoolExecutor threadPool =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingThread(),
                        marketingCommonConfig.getXieChengSmsCollidingThread());
        // 分页大小
        Integer pageSize = marketingCommonConfig.getXiechengCollidingPageSize();

        Long minId = null;
        while (marketingCommonConfig.getXieChengCollidingHitRequestNoSwitch()) {
            List<XieChengCollidingDataHitRequestNoInit> list = initMapper.selectData(minId, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1).getId();

            List<List<XieChengCollidingDataHitRequestNoInit>> partitions = Lists.partition(list, PARTATION_SIZE);
            for (List<XieChengCollidingDataHitRequestNoInit> partition : partitions) {
                threadPool.submit(() -> queryDataAndSaveToMapping(partition));
            }
        }

        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库一次性初始化撞库流水号作业：线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                    , "携程撞库一次性初始化撞库流水号作业：日志保存线程池结束异常！"), ex);
            Thread.currentThread().interrupt();
        }
    }

    private void queryDataAndSaveToMapping(List<XieChengCollidingDataHitRequestNoInit> list) {
        try {
            // 组装撞库用cell
            List<String> cells = list.stream().map(XieChengCollidingDataHitRequestNoInit::getCellSha256CodeList).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cells)) {
                return;
            }

            Result resultInfo = xieChengServiceNew.pushXieChengForInitHitRequestNo(cells);
            JSONObject resMap = JSONObject.parseObject((String) resultInfo.getData());

            // httpcode非200或code非0
            if (ResultCode.FAIL.getValue().equals(resultInfo.getCode())) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_INTERFACEERROR.getCode(), JSON.toJSONString(resMap)
                        , "携程撞库初始化撞库流水号，接口返回httpcode非200或code非0"));
                // 更新数据表retry_count=retry_count+1,query_status = 2
                // 若该部分数据重新请求，该如何操作：query_status置为0，retry_count置为0
                List<Long> ids = list.stream().map(XieChengCollidingDataHitRequestNoInit::getId).collect(Collectors.toList());
                initMapper.updateBatchByIdOfRetryCount(ids);
                return;
            }

            JSONObject resultJson = JSONObject.parseObject(resMap.getString("content"));
            JSONArray returnDataList = resultJson.getJSONArray("data");

            if (CollectionUtils.isEmpty(returnDataList)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), JSON.toJSONString(resMap)
                        , "携程撞库一次性初始化撞库流水号作业，接口返回code为0，但数据为空。resMap"));
                return;
            }

            Map<String, XieChengCollidingDataHitRequestNoInit> cellMaps =
                    list.stream().collect(Collectors.toMap(XieChengCollidingDataHitRequestNoInit::getCellSha256CodeList, Function.identity()
                            , (t1, t2) -> t1));

            List<XieChengCollidingDataHitRequestNoInit> resultList = returnDataList.stream().map(t -> (JSONObject) t)
                    .map(t -> buildInitDataDto(t, cellMaps)).collect(Collectors.toList());

            resultList.forEach((XieChengCollidingDataHitRequestNoInit t) -> {
                initMapper.updateByPrimaryKeySelective(t);
                mappingMapper.insertSelective(buildMappingDataDto(t));
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程撞库一次性初始化撞库流水号作业：子线程异常！"), e);
        }
    }

    private XieChengCollidingDataHitRequestNoInit buildInitDataDto(JSONObject t, Map<String, XieChengCollidingDataHitRequestNoInit> cellMaps) {
        XieChengCollidingDataHitRequestNoInit dto = new XieChengCollidingDataHitRequestNoInit();
        String sha256Code = t.getString("sha256Code");
        XieChengCollidingDataHitRequestNoInit initData = cellMaps.get(sha256Code);
        if (initData == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), sha256Code
                    , "携程撞库一次性初始化撞库流水号作业，返回未知sha256Code"));
            return new XieChengCollidingDataHitRequestNoInit();
        }

        dto.setId(initData.getId());
        dto.setCellSha256CodeList(initData.getCellSha256CodeList());
        // 更新pushTime
        dto.setPushTime(new Date());
        dto.setUpdateTime(new Date());
        // 更新retryCount
        dto.setRetryCount(0);
        // 更新流水号
        String hitRequestNo = t.getString("hitRequestNo");
        if (Objects.isNull(hitRequestNo)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), sha256Code
                    , "携程撞库一次性初始化撞库流水号作业，客户未返回hitRequestNo"));
        }
        dto.setHitRequestNo(hitRequestNo);
        // 更新查询状态
        dto.setQueryStatus(1);
        return dto;
    }

    private XieChengCollidingDataHitRequestNoMapping buildMappingDataDto(XieChengCollidingDataHitRequestNoInit t) {
        XieChengCollidingDataHitRequestNoMapping mapping = new XieChengCollidingDataHitRequestNoMapping();
        mapping.setHitRequestNo(t.getHitRequestNo());
        mapping.setCellSha256CodeList(t.getCellSha256CodeList());
        mapping.setExtend("现有数据一次性初始化");
        mapping.setCreateDate(Integer.valueOf(DateUtil.format(DateUtil.date(), DatePattern.PURE_DATE_PATTERN)));
        return mapping;
    }
}
