package com.br.marketing.service.Impl.xc;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.xiecheng.XieChengServiceNew;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataLog;
import com.br.marketing.entity.XieChengCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.mapper.XiechengCollidingDataEliminationMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private XieChengCollidingResultHandleService handleService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private XieChengCollidingDataLogService logService;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private XiechengCollidingDataEliminationMapper eliminationMapper;
    @Resource
    private XieChengCollidingDataRobMapper robMapper;

    private final static int PARTATION_SIZE = 50;

    /**
     * 50条数据一个批次，推送撞库手机号并处理返回结果
     * 
     * @param list
     */
    @Override
    public void pushDataAndHandleResult(List<XieChengCollidingDataLoopCycle> list) {
        try {
            // 组装撞库用cell
            List<String> originalCells = list.stream().map(XieChengCollidingDataLoopCycle::getCellSha256CodeList).collect(Collectors.toList());
            List<String> cells = excludeData(originalCells, "T");
            if (CollectionUtils.isEmpty(cells)) {
                return;
            }

            Result resultInfo = xieChengServiceNew.pushXieChengSmsCollidingDataNew(cells);
            JSONObject resMap = JSONObject.parseObject((String)resultInfo.getData());
            String httpcode = resMap.getString("httpcode");

            if (ResultCode.FAIL.getValue().equals(resultInfo.getCode())) {
                // httpcode非200或code非0
                // 更新TRUE数据表retry_count=retry_count+1
                List<Long> ids = list.stream().map(XieChengCollidingDataLoopCycle::getId).collect(Collectors.toList());
                dataLoopCycleMapper.updateBatchByIdOfRetryCount(ids);

                // 发送mq记录日志
                List<XieChengCollidingDataLog> collidingLogs = list.stream()
                    .map(t -> logService.buildFailXieChengCollidingDataLog(t.getId(), t.getPackageId(), null, "T", t.getCellSha256CodeList(), resMap))
                    .collect(Collectors.toList());

                logService.pushLogMessage(collidingLogs);
                return;
            }

            JSONObject resultJson = JSONObject.parseObject(resMap.getString("content"));
            Integer businessCode = resultJson.getInteger("code");
            JSONArray returnDataList = resultJson.getJSONArray("data");

            if (CollectionUtils.isEmpty(returnDataList)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), JSON.toJSONString(resMap)
                        , "携程TRUE数据撞库，接口返回code为0，但数据为空。resMap"));
                return;
            }

            // 根据手机号对实体分组
            Map<String, XieChengCollidingDataLoopCycle> cellMaps =
                list.stream().collect(Collectors.toMap(XieChengCollidingDataLoopCycle::getCellSha256CodeList, Function.identity(), (t1, t2) -> t1));

            // true数据处理
            trueHandle(returnDataList, cellMaps);

            // false数据处理
            falseHandle(returnDataList, cellMaps);

            // 发送mq记录日志
            List<XieChengCollidingDataLog> collidingLogs = returnDataList.stream().map(t -> (JSONObject)t)
                .map(t -> logService.buildSuccessXieChengCollidingDataLog(cellMaps.get(t.get("sha256Code")).getId(),
                    cellMaps.get(t.get("sha256Code")).getPackageId(), null, "T", t, httpcode, businessCode))
                .collect(Collectors.toList());

            logService.pushLogMessage(collidingLogs);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程TRUE数据撞库,单线程处理异常"), e);
        }
    }

    /**
     * 剔除转化数据convType=107或105
     */
    @Override
    public List<String> excludeData(List<String> cells, String dataSourceType) {
        try {
            List<String> excludeData = eliminationMapper.getExcludeData(cells);
            if (!CollectionUtils.isEmpty(excludeData)) {
                List<String> distinctExcludeData = excludeData.stream().distinct().collect(Collectors.toList());
                String extend = DateUtil.today() + " 转化数据convType=107或105";
                if (Objects.equals(dataSourceType, "T")) {
                    dataLoopCycleMapper.batchDeleteExcludeCollidingData(excludeData, extend);
                } else if (Objects.equals(dataSourceType, "F")) {
                    robMapper.batchDeleteExcludeCollidingData(excludeData, extend);
                }
                cells.removeAll(distinctExcludeData);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程剔除撞库数据异常"), e);
        }

        return cells;
    }

    /**
     * 返回为TRUE的结果处理
     * 
     * @param returnDataList
     * @param cellMaps
     */
    private void trueHandle(JSONArray returnDataList, Map<String, XieChengCollidingDataLoopCycle> cellMaps) {
        List<XieChengCollidingDataLoopCycle> trueList = returnDataList.stream().map(t -> (JSONObject)t)
            .filter(t -> t.getBoolean("result").equals(Boolean.TRUE)).map(t -> buildTrueDataDto(t, cellMaps)).collect(Collectors.toList());
        trueList.forEach((XieChengCollidingDataLoopCycle t) -> dataLoopCycleMapper.updateByTrueData(t));
    }

    /**
     * 返回为FALSE的结果处理
     * 
     * @param returnDataList
     * @param cellMaps
     */
    private void falseHandle(JSONArray returnDataList, Map<String, XieChengCollidingDataLoopCycle> cellMaps) {
        // 设置packageId为package表优先级为0的id
        Long packageId = getPackageId();
        returnDataList.stream().map(obj -> (JSONObject)obj).filter(returnData -> !returnData.getBoolean("result"))
            .forEach((JSONObject returnData) -> {
                String sha256Code = returnData.getString("sha256Code");
                XieChengCollidingDataLoopCycle loopCycle = cellMaps.get(sha256Code);
                if (loopCycle == null) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), sha256Code
                            , "携程TRUE数据撞库，返回未知sha256Code"));
                    return;
                }
                XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
                dto.setId(loopCycle.getId());
                dto.setCellSha256CodeList(sha256Code);
                Date releaseDate =
                    StringUtils.isNotEmpty(returnData.getString("releaseDate")) ? DateUtil.parse(returnData.getString("releaseDate")) : null;
                try {
                    handleService.cycleDataHandle(dto, packageId, releaseDate);
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程TRUE数据撞库，同时写true和false表失败"), e);
                }
            });
    }

    private Long getPackageId() {
        XieChengCollidingDataPackageExample packageExample = new XieChengCollidingDataPackageExample();
        packageExample.createCriteria().andPriorityEqualTo(0);
        List<XieChengCollidingDataPackage> packages = packageMapper.selectByExample(packageExample);
        return CollectionUtils.isEmpty(packages) ? null : packages.get(0).getId();
    }

    private XieChengCollidingDataLoopCycle buildTrueDataDto(JSONObject t, Map<String, XieChengCollidingDataLoopCycle> cellMaps) {
        XieChengCollidingDataLoopCycle dto = new XieChengCollidingDataLoopCycle();
        String sha256Code = t.getString("sha256Code");
        XieChengCollidingDataLoopCycle loopCycle = cellMaps.get(sha256Code);
        if (loopCycle == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), sha256Code
                    , "携程TRUE数据撞库，返回未知sha256Code"));
            return new XieChengCollidingDataLoopCycle();
        }

        dto.setId(loopCycle.getId());
        // 更新pushTime
        dto.setPushTime(new Date());
        dto.setUpdateTime(new Date());
        // 更新retryCount
        dto.setRetryCount(0);
        // 更新releaseTime
        dto.setReleaseTime(DateUtil.parse(t.getString("releaseTime"), DatePattern.NORM_DATETIME_PATTERN));
        // 更新客群标志
        dto.setCustomerGroup(1);
        // 更新info
        dto.setInfo(t.getString("info"));
        try {
            JSONArray jsonArray = t.getJSONArray("marketCouponList");
            if (jsonArray != null && !jsonArray.isEmpty()) {
                dto.setMarketCouponList(jsonArray.toJSONString());
                JSONObject firstCoupon = jsonArray.getJSONObject(0);
                String couponCode = firstCoupon.getString("couponCode");
                String couponDesc = firstCoupon.getString("couponDesc");
                dto.setCouponCode(couponCode);
                dto.setCouponDesc(couponDesc);
            }
        } catch (Exception e) {
            dto.setMarketCouponList(String.valueOf(t.get("marketCouponList")));
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程周期撞库析出marketCouponList异常"), e);
        }
        return dto;
    }

    @Override
    public void process() {
        // 创建线程池
        ThreadPoolExecutor threadPool =
            BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingThread(), marketingCommonConfig.getXieChengSmsCollidingThread());
        // 分页大小
        Integer pageSize = marketingCommonConfig.getXiechengCollidingPageSize();

        Long minId = null;
        while (canStart()) {
            // 开始时间：当天前一天的23:00
            Date startDate = Date.from(LocalDate.now().minusDays(1).atTime(23, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
            // 结束时间：当前时间
            Date endDate = new Date();

            List<XieChengCollidingDataLoopCycle> list = dataLoopCycleMapper.selectCycleDataByReleaseTime(minId, startDate, endDate, pageSize);
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), ex.getMessage()
                    , "携程TRUE数据撞库：日志保存线程池结束异常！"), ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 修改线程池大小
     * 
     * @param pool
     */
    private void modifyThreadPool(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getXieChengSmsCollidingThread();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    /**
     * 是否开启撞库
     * 
     * @return true:是。false:否
     */
    @Override
    public boolean canStart() {
        // 获取强制开关
        Boolean forceOpenSwitch = marketingCommonConfig.getXieChengForceOpenSwitch();
        // 获取条件开关，异常报警
        String redisSwitch;
        try {
            redisSwitch = redisChgService.get(RedisKeyConstant.XIECHENG_CONDITIONSWITCH);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                    , "携程TRUE数据撞库，获取redis条件开关失败"), e);
            return false;
        }

        Boolean conditionSwitch = "true".equalsIgnoreCase(redisSwitch);
        // 终止条件：强制开关关闭 且 条件开关关闭
        if (forceOpenSwitch || conditionSwitch) {
            return true;
        }

        return false;
    }
}
