package com.br.marketing.service.Impl.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.wuba.WuBaChangeSubmitDataDto;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.entity.WubaSubmitConversionDataExample;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description 58新客提交营销名单修改上报数据
 * @Author lixiang
 * @Date 2024-08-05
 */
@Service
@Slf4j
public class WuBaSubmitConversionChangeDataService {

    private static final String TITLE = "【58新客提交营销名单修改上报数据】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WubaSubmitConversionDataMapper dataMapper;


    public Result action(Page2Condition<WuBaChangeSubmitDataDto> condition) {
        return scanData(condition);
    }

    public Result scanData(Page2Condition<WuBaChangeSubmitDataDto> condition) {
        Result result = new Result<>().failure();
        try {
            ThreadPoolExecutor processPool = BrExecutors.getThreadPool(12, 12, 20);

            WuBaChangeSubmitDataDto param = condition.getParam();
            String apiCode = param.getApiCode();
            String marketingTimeStart = param.getMarketingTimeStart();
            String marketingTimeEnd = param.getMarketingTimeEnd();
            Integer pageSize = condition.getPageSize();
            // futureList
            List<Future<Result<Integer>>> futureList = new ArrayList<>();

            Long indexId = null;
            while(true) {
                // 循环获取条件数据，每次pageSize条
                final List<WubaSubmitConversionData> pageList = dataMapper.findWithMarketingTimeByIndex(apiCode,
                        marketingTimeStart, marketingTimeEnd, indexId, pageSize);

                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }

                indexId = pageList.get(pageList.size() - 1).getId();

                setThreadPoolParam(processPool);

                log.warn(TITLE+"action, 加入processPool");
                futureList.add(processPool.submit(() -> processData(pageList)));
            }

            for (Future<Result<Integer>> future : futureList) {
                try {
                    future.get(1, TimeUnit.MINUTES);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), TITLE+ e.getMessage()));
                    result.setCode(ResultCode.FAIL.getValue());
                }
            }

            long taskCount = -1;
            processPool.shutdown();
            try {
                while (!processPool.awaitTermination(30, TimeUnit.SECONDS)) {
                    long completedTask2Count = processPool.getCompletedTaskCount();
                    if (taskCount == completedTask2Count) {
                        result.setCode(ResultCode.FAIL.getValue());
                        log.warn(TITLE+"业务线程等待超时, {}, {}", apiCode, marketingTimeStart);
                        break;
                    }
                    taskCount = completedTask2Count;
                }
            } catch (InterruptedException e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), TITLE+ e.getMessage()));
                result.setCode(ResultCode.FAIL.getValue());
                Thread.currentThread().interrupt();
            }

            log.warn(TITLE + "修改成功");
        }catch (Exception e){
            log.warn(TITLE + "修改异常");
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), TITLE+ e.getMessage()));
            Thread.currentThread().interrupt();
        }
        return result.success();
    }

    public Result<Integer> processData(List<WubaSubmitConversionData> pageList) {
        try {
            List<Long> idList = pageList.stream().map(WubaSubmitConversionData::getId).collect(Collectors.toList());

            WubaSubmitConversionData updateData = new WubaSubmitConversionData();
            updateData.setStatus(1);
            updateData.setPushStatus(0);

            WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
            dataExample.createCriteria().andIdIn(idList);
            int n = dataMapper.updateByExampleSelective(updateData, dataExample);
            return new Result<>().success().setDate(n);
        } catch (Exception e) {
            log.warn(TITLE + "processData修改异常");
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), TITLE+ e.getMessage()));
        }
        return new Result<>().success().setDate(0);
    }

    private void setThreadPoolParam(ThreadPoolExecutor processPool) {
        Map<String, String> threadConfig = marketingCommonConfig.getWuBaSubmitConversionChangeDataThreadConfig();
        int processPoolSize = Integer.parseInt(threadConfig.get("processPoolSize"));

        if (ObjectUtils.isEmpty(processPoolSize) || processPoolSize < 1) {
            processPoolSize = Runtime.getRuntime().availableProcessors() * 10;
        }

        int curProcessPoolSize = processPool.getCorePoolSize();

        if(processPoolSize != curProcessPoolSize){
            ThreadPoolAdjustmentUtil.adjustThreadPoolSize(processPool, processPoolSize);
        }
    }
}
