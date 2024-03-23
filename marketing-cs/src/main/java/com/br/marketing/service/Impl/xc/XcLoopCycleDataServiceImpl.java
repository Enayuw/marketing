package com.br.marketing.service.Impl.xc;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description 携程TRUE数据撞库作业实现类
 * @Author hong.chen
 * @CreateTime 2024/03/21
 */
@Service
@Slf4j
public class XcLoopCycleDataServiceImpl implements XcLoopCycleDataService {
    @Resource
    private XieChengServiceNew xieChengServiceNew;
    @Resource
    private XieChengCollidingDataLoopCycleMapper dataLoopCycleMapper;
    @Resource
    private XieChengCollidingDataPackageMapper packageMapper;
    @Resource
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private XieChengCollidingResultHandleService handleService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RedisChgService redisChgService;
    private final static int PARTATION_SIZE = 50;

    @Override
    public void pushDataAndHandleResult(List<XieChengCollidingDataLoopCycle> list) {
        try {
            // 组装撞库用cell
            List<String> cells = list.stream().map(XieChengCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());

            Result resultInfo = xieChengServiceNew.pushXieChengSmsCollidingDataNew(cells);
            JSONObject resMap = JSONObject.parseObject(resultInfo.getData().toString());

            if (ResultCode.FAIL.getValue().equals(resultInfo.getCode())) {
                // httpcode非200或code非0
                // 更新TRUE数据表retry_count
                List<Long> ids = list.stream().map(XieChengCollidingDataLoopCycle::getId).collect(Collectors.toList());
                dataLoopCycleMapper.updateBatchByIdOfRetryCount(ids);

                // 发送mq记录日志
                String msg = JSONObject.parseObject(resMap.getString("content")).getString("msg");
                List<XieChengCollidingDataLog> collidingLogs =
                        list.stream().map(t -> buildFailXieChengCollidingDataLog(t, msg)).collect(Collectors.toList());

                pushLogMessage(collidingLogs);
                return;
            }

            JSONObject resultJson = JSONObject.parseObject(resMap.getString("content"));
            JSONArray returnDataList = resultJson.getJSONArray("data");
            // 根据手机号对实体分组
            Map<String, XieChengCollidingDataLoopCycle> cellMaps =
                    list.stream().collect(Collectors.toMap(XieChengCollidingDataLoopCycle::getCellSha256CodeList, Function.identity(),
                            (t1, t2) -> t1));

            // true数据操作
            trueHandle(returnDataList, cellMaps);

            // false数据操作
            falseHandle(returnDataList, cellMaps);

            // 发送mq记录日志
            List<XieChengCollidingDataLog> collidingLogs =
                    returnDataList.stream().map(t -> (JSONObject) t)
                            .map(t -> buildXieChengCollidingDataLog(cellMaps.get(t.getString("sha256Code")), t))
                            .collect(Collectors.toList());

            pushLogMessage(collidingLogs);
        } catch (Exception e) {
            log.error("携程TRUE数据撞库,单线程处理异常：" + e.getMessage(), e);
        }
    }

    private void trueHandle(JSONArray returnDataList, Map<String, XieChengCollidingDataLoopCycle> cellMaps) {
        List<XieChengCollidingDataLoopCycle> trueList =
                returnDataList.stream().map(t -> (JSONObject) t).filter(t -> t.getBoolean("result").equals(Boolean.TRUE)).map(t -> buildTrueDataDto(t, cellMaps)).collect(Collectors.toList());
        dataLoopCycleMapper.updateBatchByIdOfTrueDataList(trueList);
    }

    private void falseHandle(JSONArray returnDataList, Map<String, XieChengCollidingDataLoopCycle> cellMaps) {
        List<XieChengCollidingDataLoopCycle> falseList =
                returnDataList.stream().map(t -> (JSONObject) t).filter(t -> t.getBoolean("result").equals(Boolean.FALSE)).map(t -> buildFalseDataDto(t, cellMaps)).collect(Collectors.toList());

        // 查询package表优先级为0
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andPriorityEqualTo(0);
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);
        Long packageId = CollectionUtils.isEmpty(packages) ? null : packages.get(0).getId();

        falseList.forEach(t -> {
            try {
                handleService.cycleDataHandle(t, packageId);
            } catch (Exception e) {
                log.error("携程TRUE数据撞库，同时写true和false表失败，cell：" + t.getCellSha256CodeList(), e.getMessage());
            }
        });
    }

    private static XieChengCollidingDataLoopCycle buildTrueDataDto(JSONObject t, Map<String, XieChengCollidingDataLoopCycle> cellMaps) {
        XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
        String sha256Code = t.getString("sha256Code");
        XieChengCollidingDataLoopCycle loopCycle = cellMaps.get(sha256Code);

        dto.setId(loopCycle.getId());
        // 更新pushTime
        dto.setPushTime(new Date());
        dto.setUpdateTime(new Date());
        // 更新retryCount
        dto.setRetryCount(0);
        // 更新releaseTime
        dto.setReleaseTime(DateUtil.parse(t.getString("releaseTime"), DatePattern.NORM_DATETIME_PATTERN));

        return dto;
    }

    private static XieChengCollidingDataLoopCycle buildFalseDataDto(JSONObject t, Map<String, XieChengCollidingDataLoopCycle> cellMaps) {
        XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
        String sha256Code = t.getString("sha256Code");
        XieChengCollidingDataLoopCycle loopCycle = cellMaps.get(sha256Code);

        dto.setId(loopCycle.getId());
        dto.setCellSha256CodeList(sha256Code);

        return dto;
    }

    @Override
    public void process() {
        // 创建线程池
        ThreadPoolExecutor threadPool =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingThread(),
                        marketingCommonConfig.getXieChengSmsCollidingThread());

        Long minId = null;

        Integer pageSize = marketingCommonConfig.getXiechengCollidingPageSize();
        while (true) {
            if (stop()) {
                break;
            }

            List<XieChengCollidingDataLoopCycle> list = dataLoopCycleMapper.selectCycleDataByReleaseTime(minId, new Date(), pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }

            minId = list.get(list.size() - 1).getId();

            // 修改线程池大小
            modifyThreadPool(threadPool);

            List<List<XieChengCollidingDataLoopCycle>> partitions = Lists.partition(list, PARTATION_SIZE);
            for (List<XieChengCollidingDataLoopCycle> partition : partitions) {
                threadPool.submit(() -> pushDataAndHandleResult(partition));
            }
        }

        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程TRUE数据撞库：线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.error("携程TRUE数据撞库：日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
    }

    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getXieChengSmsCollidingThread();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }

    // TODO
    private XieChengCollidingDataLog buildFailXieChengCollidingDataLog(XieChengCollidingDataLoopCycle loopCycle, String msg) {
        XieChengCollidingDataLog xieChengCollidingDataLog = new XieChengCollidingDataLog();
        xieChengCollidingDataLog.setSmsCollidingDataId(loopCycle.getId());
        xieChengCollidingDataLog.setPackageId(loopCycle.getPackageId());
        xieChengCollidingDataLog.setDataSourceType("T");
        xieChengCollidingDataLog.setCellSha256CodeList(loopCycle.getCellSha256CodeList());
        xieChengCollidingDataLog.setReturnContent(msg);
        xieChengCollidingDataLog.setCreateTime(new Date());
        xieChengCollidingDataLog.setUpdateTime(new Date());
        return xieChengCollidingDataLog;
    }

    // TODO
    private XieChengCollidingDataLog buildXieChengCollidingDataLog(XieChengCollidingDataLoopCycle loopCycle, JSONObject returnData) {
        String sha256Code = returnData.getString("sha256Code");
        Boolean result = returnData.getBoolean("result");
        String orgChannel = returnData.getString("orgChannel");
        String mktLevel = returnData.getString("mktLevel");
        String info = returnData.getString("info");
        String releaseTime = returnData.getString("releaseTime");

        XieChengCollidingDataLog xieChengCollidingDataLog = new XieChengCollidingDataLog();
        xieChengCollidingDataLog.setSmsCollidingDataId(loopCycle.getId());
        xieChengCollidingDataLog.setPackageId(loopCycle.getPackageId());
        xieChengCollidingDataLog.setDataSourceType("T");
        xieChengCollidingDataLog.setCellSha256CodeList(sha256Code);
        xieChengCollidingDataLog.setReleaseTime(releaseTime);
        xieChengCollidingDataLog.setOrgChannel(orgChannel);
        xieChengCollidingDataLog.setMktLevel(mktLevel);
        xieChengCollidingDataLog.setInfo(info);
        xieChengCollidingDataLog.setResult(result);
        xieChengCollidingDataLog.setReturnContent(returnData.toJSONString());
        xieChengCollidingDataLog.setCreateTime(new Date());
        xieChengCollidingDataLog.setUpdateTime(new Date());

        return xieChengCollidingDataLog;
    }

    // TODO
    private void pushLogMessage(List<XieChengCollidingDataLog> collidingLogs) {
        try {
            rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG, JSONObject.toJSONString(collidingLogs));
        } catch (Exception e) {
            log.error("推送携程撞库日志消息异常", e);
        }
    }


    @Override
    public boolean stop(){
        // 获取强制开关
        Boolean forceOpenSwitch = marketingCommonConfig.getXieChengForceOpenSwitch();
        // 获取条件开关，取不到报警
        String redisSwitch = null;
        try {
            redisSwitch = redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH);
            if (StringUtils.isEmpty(redisSwitch)) {
                // 开关失效截至日期已过，打开条件开关
                redisChgService.set(RedisKeyConstant.XIECHENG_CONDITIONSWITCH, "true");
            }
        } catch (Exception e) {
            log.error("携程TRUE数据撞库，获取redis条件开关失败:" + e.getMessage(), e);
            return true;
        }

        Boolean conditionSwitch = "true".equalsIgnoreCase(redisSwitch);
        // 终止条件：强制开关关闭 且 条件开关关闭
        if (!forceOpenSwitch && !conditionSwitch) {
            return true;
        }

        return false;
    }
}