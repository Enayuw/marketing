package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.wuba.WuBaSubmitConversionDataDto;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 58新客通话明细入库-3710155
 *
 * @Author lixiang
 * @Date 2024-07-23
 */
@Service
@Slf4j
public class WuBaCallRecordAddToDbHandler extends AbstractExternalInterfaceHandler<WuBaSubmitConversionDataDto> {
    private final static String TITLE = "【58新客通话明细入库-3710155】";

    private Integer PARTITION_SIZE = 50;

    ThreadPoolExecutor dbActionPool = BrExecutors.getThreadPool(10, 10);
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private WubaSubmitConversionDataMapper dataMapper;

    @Override
    JSONObject call(List<WuBaSubmitConversionDataDto> list, ProcessHandlerContext context) {
        List<WubaSubmitConversionData> dataList = list.stream().map(WuBaSubmitConversionDataDto::getWubaSubmitConversionData)
                .collect(Collectors.toList());

        // batAddData
        dbActionPool.setCorePoolSize(marketingCommonConfig.getWuBaQueryConversionBatDBThreadPool());
        dbActionPool.setMaximumPoolSize(marketingCommonConfig.getWuBaQueryConversionBatDBThreadPool());
        PARTITION_SIZE = marketingCommonConfig.getWuBaQueryConversionBatDBPartitionSize();

        List<CompletableFuture<Void>> dataFutures = Lists.newArrayList();
        List<List<WubaSubmitConversionData>> dataLogPartitions = Lists.partition(dataList, PARTITION_SIZE);
        for (List<WubaSubmitConversionData> partition : dataLogPartitions) {
            dataFutures.add(CompletableFuture.runAsync(() -> {
                try {
                    dataMapper.batchAdd(partition);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(),
                            TITLE + "批量入库异常"));
                }
            }, dbActionPool));
        }
        CompletableFuture.allOf(dataFutures.toArray(new CompletableFuture[0])).join();
        log.warn(TITLE + "批量入库成功");
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.WUBA_CALL_RECORD_ADD_DB;
    }
}
