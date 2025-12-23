package com.br.marketing.service.Impl.xc;

import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.enums.XcProcessTaskEnum;
import com.br.marketing.enums.XieChengBlackListEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class XieChengPreCollidingBlackListDeleteServiceImpl implements XieChengPreCollidingBlackListDeleteService {
    private final static int PAGE_SIZE = 2000;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XiechengCollidingDataProcessTaskMapper taskMapper;
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
            //1.当天所有动态包剔除任务是否全部完成
            if (!xieChengCollidingDataProcessService.queryDeletingTaskCount(apiCode, XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE)) {
                return;
            }
            //2.剔除流程
            deleteProcess(apiCode, XcProcessTaskEnum.PROCESS_BALCKLIST_DELETE);
        });
    }

    /**
     * @param apiCode
     * @param xcProcessTaskEnum
     * @description 剔除流程
     **/
    private void deleteProcess(String apiCode, XcProcessTaskEnum xcProcessTaskEnum) {
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
                deletePublicBlackList();
                //3.自研AI业务黑名单/百应业务黑名单剔除
                deleteNoPublicBlackList(task);
                batchUpdateNoPublicBlackList(task);
                //4.更新task状态
                processAfterDeleteForBatch(task);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                        "携程黑名单剔除流程出现异常，taskId=" + task.getId() + "errorMessage=" + e.getMessage()), e);
                return;
            }
        }
    }

    private void deleteNoPublicBlackList(XiechengCollidingDataProcessTask task) {
        String conditions = task.getTaskExecutionConditions();
        if (StringUtils.isBlank(conditions)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程批量更新分组黑名单剔除，条件为空"));
            return;
        }
        List<String> batchNumbers = Arrays.asList(task.getBatchNumber().split(","));
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        TpDynamicExecutor threadPool = TpDynamicExecutorFactory
                .getThreadPool(ThreadPoolNameEnum.XIECHENG_BLACK_DELETE.getName(), 50, 100);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String batchNumber : batchNumbers) {
            deleteWithBatchNumber(batchNumber, conditions, XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_loop_cycle", today, threadPool);
            deleteWithBatchNumber(batchNumber, conditions, XieChengBlackListEnum.SELF_DEVELOPED_AI_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_rob", today, threadPool);
            deleteWithBatchNumber(batchNumber, conditions, XieChengBlackListEnum.BAIYING_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_loop_cycle", today, threadPool);
            deleteWithBatchNumber(batchNumber, conditions, XieChengBlackListEnum.BAIYING_BUSINESS_BLACKLIST,
                    "b_xiecheng_colliding_data_rob", today, threadPool);
        }
    }

    /**
     * 与跑分文件相关的黑名单剔除
     *
     * @param batchNumber
     * @param conditions
     * @param xieChengBlackListEnum
     * @param tableName
     * @param today
     * @param threadPool
     */
    private void deleteWithBatchNumber(String batchNumber, String conditions, XieChengBlackListEnum xieChengBlackListEnum,
                                       String tableName, String today, TpDynamicExecutor threadPool) {
        String extend = today + "-" + xieChengBlackListEnum.getDesc();
        Long minId = null;
        List<Long> idList = null;
        for (; ; ) {

        }
    }

    /**
     * @Description:公共黑名单剔除
     * @Author: Ethan.Kang
     */
    public void deletePublicBlackList() {
        //周期表公共黑名单提出
        deleteCycPublicBlackList();
        //非周期表公共黑名单剔除
        deleteRobPublicBlackList();
    }

    private void batchUpdateNoPublicBlackList(XiechengCollidingDataProcessTask vo) {
        //条件为空的处理
        String conditions = vo.getTaskExecutionConditions();
        if (StringUtils.isBlank(conditions)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(),
                    "携程批量更新分组黑名单剔除，条件为空"));
            return;
        }
        Integer threadPoolSize = marketingCommonConfig.getXieChengPreCollidingBlackListDeleteThread();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
        //非空
        String batchNumberStr = vo.getBatchNumber();
        List<String> batchNumberList = Arrays.asList(batchNumberStr.split(","));
        Long minId = null;
        while (true) {
            List<XieChengBlackList> blackListCells = blackListMapper.selectCellsByPage(minId, PAGE_SIZE);
            if (CollectionUtils.isEmpty(blackListCells)) {
                threadPoolShutDown(threadPool);
                return;
            }
            minId = (blackListCells.get(blackListCells.size() - 1).getId());
            CompletableFuture.runAsync(() -> {
                try {
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
                    List<String> scoreCells = blackListMapper.selectByBlackListIdsFromScoreFiletikv_(querySql);
                    if (!CollectionUtils.isEmpty(scoreCells)) {
                        Map<String, XieChengBlackList> backMap =
                                blackListCells.stream().collect(Collectors.toMap(XieChengBlackList::getCellSha256, Function.identity(),
                                        (t1, t2) -> {
                                            if (t1.getLabelType() <= t2.getLabelType()) {
                                                return t1;
                                            } else {
                                                return t2;
                                            }
                                        }));
                        //交集
                        List<XieChengBlackList> result = new ArrayList<>(scoreCells.size());
                        scoreCells.forEach(a -> {
                            if (backMap.containsKey(a)) {
                                result.add(backMap.get(a));
                            }
                        });

                        if (!CollectionUtils.isEmpty(result)) {
                            result.forEach(t -> {
                                cycleMapper.batchUpdateCycNoPublicBlackListData(t.getLabelName(), t.getCellSha256());
                                robMapper.batchUpdateRobNoPublicBlackListData(t.getLabelName(), t.getCellSha256());
                            });
                        }
                    }
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程批量更新分组黑名单剔除，子线程处理异常"), e);
                }
            }, threadPool);
        }
    }

    private void deleteRobPublicBlackList() {
        Integer threadPoolSize = marketingCommonConfig.getXieChengPreCollidingBlackListDeleteThread();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
        Long minId = null;
        while (true) {
            List<Long> ids = robMapper.selectRobPublicBlackListIdsByPage(minId, PAGE_SIZE, XieChengBlackListEnum.PUBLIC_BLACKLISTS.getValue());
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            CompletableFuture.runAsync(() -> {
                try {
                    robMapper.batchUpdateRobBlackListData(ids, DateUtils.format(new Date()) + "公共黑名单");
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程批量更新非周期表公共黑名单状态，子线程处理异常"), e);
                }
            }, threadPool);
        }
        threadPoolShutDown(threadPool);
    }

    private void deleteCycPublicBlackList() {
        Integer threadPoolSize = marketingCommonConfig.getXieChengPreCollidingBlackListDeleteThread();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
        Long minId = null;
        while (true) {
            List<Long> ids = cycleMapper.selectCycleBlackListIdsByPage(minId, PAGE_SIZE, XieChengBlackListEnum.PUBLIC_BLACKLISTS.getValue());
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            minId = ids.get(ids.size() - 1);
            CompletableFuture.runAsync(() -> {
                try {
                    cycleMapper.batchUpdateCycPublicBlackListData(ids, DateUtils.format(new Date()) + "公共黑名单");
                } catch (Exception e) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode(), e.getMessage()
                            , "携程批量更新周期表公共黑名单状态，子线程处理异常"), e);
                }
            }, threadPool);
        }
        threadPoolShutDown(threadPool);
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
