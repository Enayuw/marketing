package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.XcProcessTaskEnum;
import com.br.marketing.enums.XieChengBlackListEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class XieChengPreCollidingBlackListDeleteServiceImpl implements XieChengPreCollidingBlackListDeleteService {
    private final static int PAGE_SIZE = 2000;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XiechengCollidingDataProcessTaskMapper taskMapper;
    @Autowired
    private DingDingRobotHookService dingDingRobotHookService;
    @Resource
    XieChengCollidingDataLoopCycleMapper cycleMapper;
    @Resource
    XieChengCollidingDataRobMapper robMapper;
    @Resource
    XieChengBlackListMapper blackListMapper;
    @Resource
    XieChengCollidingDataProcessService xieChengCollidingDataProcessService;

    @Override
    public void process() {
        marketingCommonConfig.getXieChengCollidingDataProcessApiCodes().forEach((String apiCode) ->
        {
            //1.创建线程池
            Integer threadPoolSize = marketingCommonConfig.getXieChengPreCollidingBlackListDeleteThread();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            //2.当天所有动态包剔除任务是否全部完成
            if (!xieChengCollidingDataProcessService.queryDeletingTaskCount(apiCode, XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE)) {
                threadPoolShutDown(threadPool);
                return;
            }
            //3.剔除流程
            deleteProcess(apiCode, XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE, threadPool);
            threadPoolShutDown(threadPool);
        });
    }

    /**
     * @param apiCode
     * @param xcProcessTaskEnum
     * @param threadPool
     * @description 剔除流程
     **/
    private void deleteProcess(String apiCode, XcProcessTaskEnum xcProcessTaskEnum, ThreadPoolExecutor threadPool) {
        XiechengCollidingDataProcessTaskExample example = new XiechengCollidingDataProcessTaskExample();
        example.createCriteria().andApiCodeEqualTo(apiCode)
                .andTaskStatusEqualTo(0).andTaskTypeEqualTo(xcProcessTaskEnum.getTaskType()).
                andTaskStartTimeEqualTo(getStartOfDate()).andIsDeleteEqualTo(0);
        List<XiechengCollidingDataProcessTask> tasks = taskMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        for (XiechengCollidingDataProcessTask task : tasks) {
            try {
                //1、携程撞库黑名单任务剔除开始,更新task任务状态
                processBeforeDeleteForBatch(task);
                //2.公共黑名单剔除
                deleteForPublicBlacklists(threadPool);
                //3.自研AI业务黑名单/百应业务黑名单剔除
                deleteForNoPublicBlacklists(task, threadPool);
                //7.更新task状态
                processAfterDeleteForBatch(task);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程黑名单剔除流程出现异常，taskId=" + task.getId() + "errorMessage=" + e.getMessage()), e);
                return;
            }
        }
    }

    /**
     * @Description:公共黑名单剔除
     * @Author: Ethan.Kang
     */
    private void deleteForPublicBlacklists(ThreadPoolExecutor threadPool) {
        //公共黑名单周期表剔除
        deleteCycPublicBlackList(threadPool, DateUtils.format(new Date()) + " 公共黑名单剔除",
                XieChengBlackListEnum.PUBLIC_BLACKLISTS.getValue());
        //公共黑名单rob表剔除
        deleteRobPublicBlackList(threadPool, DateUtils.format(new Date()) + " 公共黑名单剔除",
                XieChengBlackListEnum.PUBLIC_BLACKLISTS.getValue());
    }

    /**
     * @Description:自研AI业务黑名单/百应业务黑名单剔除
     * @Author: Ethan.Kang
     */
    private void deleteForNoPublicBlacklists(XiechengCollidingDataProcessTask vo, ThreadPoolExecutor threadPool) {
        //非公共黑名单周期表剔除(自研AI业务黑名单)
        batchUpdateCycNoPublicBlackList(vo,DateUtils.format(new Date()) + " 自研AI业务黑名单剔除",
                XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST.getValue());
        //非公共黑名单周期表剔除(百应业务黑名单)
        batchUpdateCycNoPublicBlackList(vo, DateUtils.format(new Date()) + " 百应业务黑名单剔除",
                XieChengBlackListEnum.BAIYING_BUSINESS_BLACKLIST.getValue());
        //非公共黑名单rob表剔除(自研AI业务黑名单)
        batchUpdateRobNoPublicBlackList(vo, DateUtils.format(new Date()) + " 自研AI业务黑名单剔除",
                XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST.getValue());
        //非公共黑名单rob表剔除(百应业务黑名单)
        batchUpdateRobNoPublicBlackList(vo, DateUtils.format(new Date()) + " 百应业务黑名单剔除",
                XieChengBlackListEnum.BAIYING_BUSINESS_BLACKLIST.getValue());

    }

    private void batchUpdateCycNoPublicBlackList(XiechengCollidingDataProcessTask vo,
                                                 String extend, Integer groupType) {
        //条件为空的处理
        String conditions = vo.getTaskExecutionConditions();
        if (StringUtils.isBlank(conditions)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程批量更新周期表分组黑名单剔除，条件为空"));
            return;
        }
        //非空
        String batchNumberStr = vo.getBatchNumber();
        List<String> batchNumberList = Arrays.asList(batchNumberStr.split(","));
        Long minId = null;
        while (true) {
            try {
                List<XieChengBlackList> blackListCells = blackListMapper.selectCellsByPage(minId, PAGE_SIZE, groupType);
                if (CollectionUtils.isEmpty(blackListCells)) {
                    break;
                }
                minId = blackListCells.get(blackListCells.size() - 1).getId();
                StringBuilder builderStr = new StringBuilder();
                for (int i = 0; i < blackListCells.size(); i++) {
                    XieChengBlackList black = blackListCells.get(i);
                    if (i == 0) {
                        builderStr.append(black.getCellSha256()).append("\",");
                    } else if (i == blackListCells.size() - 1) {
                        builderStr.append("\"").append(black.getCellSha256());
                    } else {
                        builderStr.append("\"").append(black.getCellSha256()).append("\",");
                    }
                }
                String querySql = "";
                for (int i = 0; i < batchNumberList.size(); i++) {
                    if (i == batchNumberList.size() - 1) {
                        querySql = querySql.concat("select ").concat("cell").concat(" from ")
                                .concat("b_xiecheng_colliding_" + batchNumberList.get(i)) + " where cell in(" + "\"" + builderStr + "\"" + ") and " + conditions;
                    } else {
                        querySql = querySql.concat("select ").concat("cell").concat(" from ")
                                .concat("b_xiecheng_colliding_" + batchNumberList.get(i)).concat(" where cell in(" + "\"" + builderStr + "\"" + ") and " + conditions)
                                .concat(" union all ");
                    }
                }
                List<String> scoreCells = blackListMapper.selectByBlackListIdsFromScoreFile(querySql);
                //全部满足 不剔除
                if (scoreCells.size() >= blackListCells.size()) {
                    continue;
                }
                List<String> originCell = blackListCells.stream().map(XieChengBlackList::getCellSha256).distinct().collect(Collectors.toList());
                //获取差集(上面获取到scoreCells.size=0 本次都删)
                List<String> differenceCells = originCell.stream().filter(element -> !scoreCells.contains(element)).collect(Collectors.toList());
                cycleMapper.batchUpdateCycNoPublicBlackListData(differenceCells, extend);
            } catch (Exception e) {
                if (groupType.equals(XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST.getValue())) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程批量更新周期表自研AI黑名单状态，子线程处理异常"), e);
                } else {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程批量更新周期表百应业务黑名单状态，子线程处理异常"), e);
                }
            }
        }
    }

    private void batchUpdateRobNoPublicBlackList(XiechengCollidingDataProcessTask vo, String extend, Integer groupType) {
        //条件为空的处理
        String conditions = vo.getTaskExecutionConditions();
        if (StringUtils.isBlank(conditions)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程批量更新非周期表分组黑名单剔除，条件为空"));
            return;
        }
        //非空
        String batchNumberStr = vo.getBatchNumber();
        List<String> batchNumberList = Arrays.asList(batchNumberStr.split(","));
        Long minId = null;
        while (true) {
            try {
                List<XieChengBlackList> blackListCells = blackListMapper.selectCellsByPage(minId, PAGE_SIZE, groupType);
                if (CollectionUtils.isEmpty(blackListCells)) {
                    break;
                }
                minId = blackListCells.get(blackListCells.size() - 1).getId();
                StringBuilder builderStr = new StringBuilder();
                for (int i = 0; i < blackListCells.size(); i++) {
                    XieChengBlackList black = blackListCells.get(i);
                    if (i == 0) {
                        builderStr.append(black.getCellSha256()).append("\",");
                    } else if (i == blackListCells.size() - 1) {
                        builderStr.append("\"").append(black.getCellSha256());
                    } else {
                        builderStr.append("\"").append(black.getCellSha256()).append("\",");
                    }
                }
                String querySql = "";
                for (int i = 0; i < batchNumberList.size(); i++) {
                    if (i == batchNumberList.size() - 1) {
                        querySql = querySql.concat("select ").concat("cell").concat(" from ")
                                .concat("b_xiecheng_colliding_" + batchNumberList.get(i)) + " where cell in(" + "\"" + builderStr + "\"" + ") and " + conditions;
                    } else {
                        querySql = querySql.concat("select ").concat("cell").concat(" from ")
                                .concat("b_xiecheng_colliding_" + batchNumberList.get(i)).concat(" where cell in(" + "\"" + builderStr + "\"" + ") and " + conditions)
                                .concat(" union all ");
                    }
                }
                List<String> scoreCells = blackListMapper.selectByBlackListIdsFromScoreFile(querySql);
                //全部满足 不剔除
                if (scoreCells.size() >= blackListCells.size()) {
                    continue;
                }
                List<String> originCell = blackListCells.stream().map(XieChengBlackList::getCellSha256).distinct().collect(Collectors.toList());
                //获取差集(上面获取到scoreCells.size=0 本次都删)
                List<String> differenceCells = originCell.stream().filter(element -> !scoreCells.contains(element)).collect(Collectors.toList());
                robMapper.batchUpdateRobNoPublicBlackListData(differenceCells, extend);
            } catch (Exception e) {
                if (groupType.equals(XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST.getValue())) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程批量更新非周期表自研AI黑名单状态，子线程处理异常"), e);
                } else {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程批量更新非周期表百应业务黑名单状态，子线程处理异常"), e);
                }
            }
        }
    }


    private void deleteCycPublicBlackList(ThreadPoolExecutor threadPool, String extend, Integer groupType) {
        Long minId = null;
        while (true) {
            List<Long> ids = cycleMapper.selectCycleBlackListIdsByPage(minId, PAGE_SIZE, groupType);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            CompletableFuture.runAsync(() -> {
                try {
                    cycleMapper.batchUpdateCycPublicBlackListData(ids, extend);
                } catch (Exception e) {
                    if (groupType.equals(XieChengBlackListEnum.PUBLIC_BLACKLISTS.getValue())) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新周期表公共黑名单状态，子线程处理异常"), e);
                    } else if (groupType.equals(XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST.getValue())) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新周期表自研AI业务黑名单状态，子线程处理异常"), e);
                    } else {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新周期表百应业务黑名单状态，子线程处理异常"), e);
                    }
                }
            }, threadPool);
        }
    }

    private void deleteRobPublicBlackList(ThreadPoolExecutor threadPool, String extend, Integer groupType) {
        Long minId = null;
        while (true) {
            List<Long> ids = robMapper.selectRobPublicBlackListIdsByPage(minId, PAGE_SIZE, groupType);
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            CompletableFuture.runAsync(() -> {
                try {
                    robMapper.batchUpdateBlackListData(ids, extend);
                } catch (Exception e) {
                    if (groupType.equals(XieChengBlackListEnum.PUBLIC_BLACKLISTS.getValue())) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新非周期表公共黑名单状态，子线程处理异常"), e);
                    } else if (groupType.equals(XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST.getValue())) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新非周期表自研AI业务黑名单状态，子线程处理异常"), e);
                    } else {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                                , "携程批量更新非周期表百应业务黑名单状态，子线程处理异常"), e);
                    }
                }
            }, threadPool);
        }
    }

    private void processAfterDeleteForBatch(XiechengCollidingDataProcessTask vo) {
        XiechengCollidingDataProcessTask processTask = new XiechengCollidingDataProcessTask();
        processTask.setId(vo.getId());
        processTask.setTaskStatus(2);
        processTask.setTaskEndTime(new Date());
        processTask.setUpdateTime(new Date());
        taskMapper.updateByPrimaryKeySelective(processTask);
    }

    private void processBeforeDeleteForBatch(XiechengCollidingDataProcessTask task) {
        XiechengCollidingDataProcessTask entity = new XiechengCollidingDataProcessTask();
        entity.setId(task.getId());
        entity.setTaskStatus(1);
        entity.setUpdateTime(new Date());
        taskMapper.updateByPrimaryKeySelective(entity);
    }


    /**
     * @return java.util.Date
     * @description 获取当天0时，精确到秒
     * @author KP
     * @date 2024/8/7 20:53
     **/
    private static Date getStartOfDate() {
        LocalDate localDate = LocalDate.now();
        Date nowDate = Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        return nowDate;
    }

    private void threadPoolShutDown(ThreadPoolExecutor threadPool) {
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库数据黑名单剔除作业线程池关闭");
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程撞库数据黑名单剔除作业，日志保存线程池结束异常！errorMessage=" + ex.getMessage()), ex);
            Thread.currentThread().interrupt();
        }
    }
}
