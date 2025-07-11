package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.marketing.client.xiecheng.XieChengService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.XieChengCpsCollidingDataRob;
import com.br.marketing.mapper.XieChengCpsCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 携程CPS非周期数据撞库服务实现类
 * @Author chenh
 * @Date 2025-06-26
 */
@Service
@Slf4j
public class XieChengCpsRobDataCollidingServiceImpl implements XieChengCpsRobDataCollidingService {

    @Resource
    private XieChengService xieChengService;

    @Resource
    private XieChengCpsCollidingDataRobMapper cpsRobMapper;

    @Resource
    private XieChengCpsCollidingResultHandleService handleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    private final static int PARTITION_SIZE = 50;

    @Override
    public void process() {
        // 创建线程池
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.XIECHENG_CPS_ROB_3710090.getName(), 5, 10);

        Long minId = null;
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        while (true) {
            // 分页大小
            Integer pageSize = marketingCommonConfig.getXieChengSmsCollidingDataVtPageSize();

            // 查询未撞库数据：retryCount=0，push_time=null
            List<XieChengCpsCollidingDataRob> list = cpsRobMapper.selectUnprocessedRobData(minId, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1).getId();

            // 异步处理
            List<List<XieChengCpsCollidingDataRob>> partitions = Lists.partition(list, PARTITION_SIZE);
            for (List<XieChengCpsCollidingDataRob> partition : partitions) {
                futures.add(CompletableFuture.runAsync(() -> {
                    try {
                        pushDataAndHandleResult(partition);
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程CPS非周期数据撞库，处理异常"), e);
                    }
                }, threadPool));
            }
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.warn("携程CPS非周期数据撞库完成，处理批次数：{}", futures.size());

        // 关闭线程池
        threadPool.shutdownAndAwaitTermination();
    }

    @Override
    public void pushDataAndHandleResult(List<XieChengCpsCollidingDataRob> list) {
        try {
            // 组装撞库用cell
            List<String> originalCells = list.stream()
                    .map(XieChengCpsCollidingDataRob::getCellSha256CodeList)
                    .collect(Collectors.toList());

            // 调用携程CPS撞库接口
            Result<String> resultInfo = xieChengService.pushXieChengCpsCollidingData(originalCells);

            // 根据手机号对实体分组
            Map<String, XieChengCpsCollidingDataRob> cellMaps = list.stream()
                    .collect(Collectors.toMap(XieChengCpsCollidingDataRob::getCellSha256CodeList,
                            Function.identity(), (t1, t2) -> t1));

            // 处理撞库结果
            handleService.robDataHandle(resultInfo, cellMaps);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程CPS非周期数据撞库异常"), e);
        }
    }
} 