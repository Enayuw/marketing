package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
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
    private RabbitMqProducter rabbitMqProducter;
    @Resource
    private XieChengCollidingResultHandleService handleService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RedisChgService redisChgService;
    private final static int PARTATION_SIZE = 50;

    @Override
    public void pushDataAndHandleResult(List<XieChengCollidingDataLoopCycle> list, AtomicInteger failNum) {
        try {
            List<String> cells = list.stream().map(XieChengCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());
            Result resultInfo = xieChengServiceNew.pushXieChengSmsCollidingDataNew(cells);

            JSONObject resMap = JSONObject.parseObject(resultInfo.getMessage());
            String httpcode = resMap.getString("httpcode");
            JSONObject resultJson = JSONObject.parseObject(resMap.getString("content"));
            Integer businessCode = resultJson.getInteger("code");

            JSONArray returnDataList = resultJson.getJSONArray("data");
            // 根据手机号对实体分组
            Map<String, XieChengCollidingDataLoopCycle> collect =
                    list.stream().collect(Collectors.toMap(XieChengCollidingDataLoopCycle::getCellSha256CodeList, Function.identity()));
            if (ResultCode.SUCCESS.getValue().equals(resultInfo.getCode())) {
                // code==0
                // 更新数据表
                for (int i = 0; i < returnDataList.size(); i++) {
                    JSONObject returnData = returnDataList.getJSONObject(i);
                    String sha256Code = returnData.getString("sha256Code");
                    Boolean result = returnData.getBoolean("result");
                    String orgChannel = returnData.getString("orgChannel");
                    String mktLevel = returnData.getString("mktLevel");
                    String info = returnData.getString("info");
                    String releaseTime = returnData.getString("releaseTime");

                    XieChengCollidingDataLoopCycle loopCycle = collect.get(sha256Code);
                    if (loopCycle == null) {
                        // todo 报警
                        continue;
                    }

                    XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
                    dto.setId(loopCycle.getId());
                    dto.setCellSha256CodeList(sha256Code);
                    dto.setPackageId(loopCycle.getPackageId());
                    // 更新pushTime
                    dto.setPushTime(new Date());
                    // 更新retryCount
                    dto.setRetryCount(0);

                    if (result) {
                        // 更新releaseTime
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime releaseTimeDate = LocalDateTime.parse(releaseTime, formatter);
                        Date releaseDate = Date.from(releaseTimeDate.atZone(ZoneId.systemDefault()).toInstant());
                        dto.setReleaseTime(releaseDate);
    //                    dto.setDataSourceType("T");

                        dataLoopCycleMapper.updateByPrimaryKeySelective(dto);
                    } else {
                        handleService.cycleDataHandle(dto);
                    }

                    // 插入log表
                    rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG, returnData.toJSONString());
                }


            } else {
                failNum.incrementAndGet();

                for (int i = 0; i < returnDataList.size(); i++) {
                    JSONObject returnData = returnDataList.getJSONObject(i);
                    String sha256Code = returnData.getString("sha256Code");
                    XieChengCollidingDataLoopCycle loopCycle = collect.get(sha256Code);
                    if (loopCycle == null) {
                        // todo 报警
                        continue;
                    }

                    // 更新TRUE数据表
                    XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
                    dto.setRetryCount(loopCycle.getRetryCount() + 1);
                    dto.setId(loopCycle.getId());

                    dataLoopCycleMapper.updateByPrimaryKeySelective(dto);

                    // 发MQ插入log表
                    rabbitMqProducter.send(MQConstants.ROUTING_KEY_MARKETING_XIECHENG_COLLIDING_LOG, returnData.toJSONString());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage() ,e);
        }
    }

    @Override
    public void process() {
        // 创建线程池
        ThreadPoolExecutor threadPool =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingThread(),
                        marketingCommonConfig.getXieChengSmsCollidingThread());

        Long minId = null;
        AtomicInteger failNum = new AtomicInteger(0);
        while (true) {
            // 判断强制和条件开关
            Boolean forceOpenSwitch = marketingCommonConfig.getXieChengForceOpenSwitch();
            String redisSwitch = redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH);
            Boolean conditionSwitch = "true".equalsIgnoreCase(redisSwitch);
            if (forceOpenSwitch || conditionSwitch) {
                // todo 增加分页条数
                List<XieChengCollidingDataLoopCycle> list = dataLoopCycleMapper.selectCycleDataByReleaseTime(minId, new Date());

                if (CollectionUtils.isEmpty(list)) {
                    break;
                }

                minId = list.get(list.size() - 1).getId();

                // 修改线程池大小
                modifyThreadPool(threadPool);

                List<List<XieChengCollidingDataLoopCycle>> partitions = Lists.partition(list, PARTATION_SIZE);
                for (List<XieChengCollidingDataLoopCycle> partition : partitions) {
                    threadPool.submit(() -> pushDataAndHandleResult(partition, failNum));
                }
            }
        }

        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.error("日志保存线程池结束异常！", ex);
            Thread.currentThread().interrupt();
        }
    }

    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getXieChengSmsCollidingThread();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }
}