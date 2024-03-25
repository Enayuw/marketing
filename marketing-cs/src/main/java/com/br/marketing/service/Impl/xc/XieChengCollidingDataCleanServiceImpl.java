package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataContrast;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataTemp;
import com.br.marketing.mapper.XieChengCollidingDataContrastMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
public class XieChengCollidingDataCleanServiceImpl implements XieChengCollidingDataCleanService {

    @Resource
    private XieChengCollidingDataContrastMapper xieChengCollidingDataContrastMapper;

    @Resource
    private XieChengCollidingDataPackageMapper xieChengCollidingDataPackageMapper;

    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;

    @Resource
    private XieChengCollidingDataRobMapper xieChengCollidingDataRobMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    private final static Integer PARTITIONCOUNT = 10000;
    /**
     * 主流程
     *
     * @param jobParameter job 参数
     */
    @Override
    public void process(String jobParameter) {

        ThreadPoolExecutor xieChengCollidingCleanThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getXieChengCleanThreadCount(),
                        marketingCommonConfig.getXieChengCleanThreadCount());

        // 解析job 参数
        ParameterToJson result = getParameterToJson(jobParameter);
        if (result == null) {
            return;
        }
        List<Map<String, String>> maps = xieChengCollidingDataContrastMapper.temporaryCellCountRepeattiflash_(result.temporaryTable);
        if (maps.size() > 0) {
            log.error("携程撞库跑分临时表含有重复数据请手动处理！{}", maps);
            return;
        }
        // 跑分数据存入对比表
        xieChengCollidingDataContrastProcess(result.filterScore, result.packageRuleInfo,
                xieChengCollidingCleanThread, result.temporaryTable);
//        // 对比表数据同周期数据处理逻辑
        xieChengCollidingLoopDataCycleProcess(xieChengCollidingCleanThread, result.loopCycleSwitch);
//        // 对比表数据同非周期数据处理逻辑
        xieChengCollidingRobDataProcess(xieChengCollidingCleanThread, result.robSwitch);

        try {
            while (!xieChengCollidingCleanThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库数据清洗线程池关闭");
            }
        } catch (InterruptedException ex) {
            xieChengCollidingCleanThread.shutdownNow();
            log.error("携程撞库数据清洗线程池关闭异常！", ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * job参数解析
     *
     * @param jobParameter job参数
     * @return 参数对象
     */
    private static ParameterToJson getParameterToJson(String jobParameter) {
        try {
            JSONObject parseJson = JSONObject.parseObject(jobParameter);
            String temporaryTable = parseJson.getString("temporary_table");
            String filterScore = parseJson.getString("filter_score");
            JSONArray packageRuleInfo = parseJson.getJSONArray("package_rule_info");
            Boolean loopCycleSwitch = parseJson.getBoolean("loop_cycle_switch");
            Boolean robSwitch = parseJson.getBoolean("rob_switch");
            return new ParameterToJson(temporaryTable, filterScore, packageRuleInfo, loopCycleSwitch, robSwitch);
        } catch (Exception e) {
            log.error("携程清洗job参数异常：{}", jobParameter);
        }
        return null;
    }

    private static class ParameterToJson {
        public final String temporaryTable;
        public final String filterScore;
        public final JSONArray packageRuleInfo;
        public final Boolean loopCycleSwitch;
        public final Boolean robSwitch;

        public ParameterToJson(String temporaryTable, String filterScore,
                               JSONArray packageRuleInfo, Boolean loopCycleSwitch, Boolean robSwitch
        ) {
            this.temporaryTable = temporaryTable;
            this.filterScore = filterScore;
            this.packageRuleInfo = packageRuleInfo;
            this.loopCycleSwitch = loopCycleSwitch;
            this.robSwitch = robSwitch;
        }
    }

    /**
     * 携程撞库数据对比表数据处理
     *
     * @param filterInfoArray              过滤规则
     * @param xieChengCollidingCleanThread 处理线程池
     * @param temporaryTable               临时表名
     */
    private void xieChengCollidingDataContrastProcess(String filterScore, JSONArray filterInfoArray,
                                                      ThreadPoolExecutor xieChengCollidingCleanThread, String temporaryTable) {

        for (int i = 0; i < filterInfoArray.size(); i++) {
            String filterInfo = filterInfoArray.get(i).toString();
            JSONObject filterInfoJson = JSONObject.parseObject(filterInfo);
            String splitFilterScore = filterInfoJson.getString("split_filter_score");
            String packageName = filterInfoJson.getString("package_name");
            String collidingTime = filterInfoJson.getString("colliding_time");
            Integer priority = filterInfoJson.getInteger("priority");
            int counttiflash = xieChengCollidingDataContrastMapper.temporaryCellCounttiflash_(temporaryTable, filterScore);
            if (counttiflash == 0) {
                continue;
            }
            // 生成新的packageId
            Long packageId = savePackage(packageName, priority, collidingTime, splitFilterScore);
            // 携程数据清洗进入到对比表
            xieChengCollidingDataCleanProcess(xieChengCollidingCleanThread,
                    temporaryTable, filterScore, i + 1, packageId, splitFilterScore);
        }
    }

    /**
     * 携程撞库数据非周期表数据处理流程
     *
     * @param xieChengCollidingCleanThread 线程池
     * @param robSwitch                    开关
     */
    private void xieChengCollidingRobDataProcess(ThreadPoolExecutor xieChengCollidingCleanThread, Boolean robSwitch) {
        if (robSwitch) {
            // 删除非周期表中的数据
            deleteRobData(xieChengCollidingCleanThread);
            // 拆分非周期数据
            splitRobData(xieChengCollidingCleanThread);
        }

    }

    private void splitRobData(ThreadPoolExecutor xieChengCollidingCleanThread) {
       while(marketingCommonConfig.getXieChengCleanSwitch()) {
            setThreadCount(xieChengCollidingCleanThread);
            List<XieChengCollidingDataContrast> idLists = xieChengCollidingDataContrastMapper.robCelltiflash_(marketingCommonConfig.getXieChengCleanLimitCount());
            if (idLists.isEmpty()) {
                break;
            }
            List<List<XieChengCollidingDataContrast>> partition = Lists.partition(idLists, PARTITIONCOUNT);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<XieChengCollidingDataContrast> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> saveRobData(p));
                futureList.add(submit);
            }
            futureFinish(futureList);
        }
    }

    private void deleteRobData(ThreadPoolExecutor xieChengCollidingCleanThread) {
       while(marketingCommonConfig.getXieChengCleanSwitch()) {
            setThreadCount(xieChengCollidingCleanThread);
            List<Long> idLists = xieChengCollidingDataRobMapper.robCelltiflash_(marketingCommonConfig.getXieChengCleanLimitCount());
            if (idLists.isEmpty()) {
                break;
            }
            List<List<Long>> partition = Lists.partition(idLists, PARTITIONCOUNT);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<Long> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> deleteRobData(p));
                futureList.add(submit);
            }
            futureFinish(futureList);
        }
    }

    /**
     * 携程周期数据处理流程
     *
     * @param xieChengCollidingCleanThread 线程池
     * @param loopCycleSwitch              开关
     */
    private void xieChengCollidingLoopDataCycleProcess(ThreadPoolExecutor xieChengCollidingCleanThread, Boolean loopCycleSwitch) {
        if (loopCycleSwitch) {
            // 周期表内不符合分值条件的数据要删除掉
            doLoopCycleWork(xieChengCollidingCleanThread);
            // 对比表内符合分值条件且数据是true的数据删除掉
            doContrastWork(xieChengCollidingCleanThread);
        }
    }

    /**
     * 对比数据处理
     *
     * @param xieChengCollidingCleanThread 线程池
     */
    private void doContrastWork(ThreadPoolExecutor xieChengCollidingCleanThread) {
       while(marketingCommonConfig.getXieChengCleanSwitch()) {
            setThreadCount(xieChengCollidingCleanThread);
            List<Long> idLists = xieChengCollidingDataContrastMapper.loopCycleCellExisttiflash_(marketingCommonConfig.getXieChengCleanLimitCount());
            if (idLists.isEmpty()) {
                break;
            }
            // 多线程删除对比表
            List<List<Long>> partition = Lists.partition(idLists, PARTITIONCOUNT);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<Long> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> deleteContrastData(p));
                futureList.add(submit);
            }
            futureFinish(futureList);
        }
    }

    /**
     * 周期数据处理
     *
     * @param xieChengCollidingCleanThread 线程池
     */
    private void doLoopCycleWork(ThreadPoolExecutor xieChengCollidingCleanThread) {
       while(marketingCommonConfig.getXieChengCleanSwitch()) {
            setThreadCount(xieChengCollidingCleanThread);
            List<Long> idLists = xieChengCollidingDataContrastMapper.loopCycleCelltiflash_(marketingCommonConfig.getXieChengCleanLimitCount());
            if (idLists.isEmpty()) {
                break;
            }
            // 多线程删除周期表
            List<List<Long>> partition = Lists.partition(idLists, PARTITIONCOUNT);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<Long> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> deleteLoopCycleData(p));
                futureList.add(submit);
            }
            futureFinish(futureList);
        }
    }

    /**
     * 线程处理结果获取
     *
     * @param futureList 线程处理
     */
    private static void futureFinish(List<Future<Integer>> futureList) {
        for (Future<Integer> stringFuture : futureList) {
            try {
                stringFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("InterruptedException:", e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                log.warn("InterruptedException:", e);
            } catch (TimeoutException e) {
                log.warn("TimeoutException:", e);
            }
        }
    }

    /**
     * 携程撞库数据清理主流程
     *
     * @param xieChengCollidingCleanThread 线程池
     * @param tableName                    临时表
     * @param filterScore                  过滤条件
     * @param ruleTypeFlag                 规则
     * @param packageId                    包id
     */
    private void xieChengCollidingDataCleanProcess(ThreadPoolExecutor xieChengCollidingCleanThread,
                                                   String tableName, String filterScore, Integer ruleTypeFlag, Long packageId, String splitFilterScore) {
       while(marketingCommonConfig.getXieChengCleanSwitch()) {
            setThreadCount(xieChengCollidingCleanThread);
            List<XieChengCollidingDataTemp> cellList = xieChengCollidingDataContrastMapper.temporaryCelltiflash_(
                    tableName, filterScore, marketingCommonConfig.getXieChengCleanLimitCount());
            if (cellList.isEmpty()) {
                break;
            }
            // 多线程插入对比表
            List<List<XieChengCollidingDataTemp>> partition = Lists.partition(cellList, PARTITIONCOUNT);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<XieChengCollidingDataTemp> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> saveDataContrast(p, ruleTypeFlag, packageId, splitFilterScore));
                futureList.add(submit);
            }
            futureFinish(futureList);
        }
    }

    private void setThreadCount(ThreadPoolExecutor xieChengCollidingCleanThread) {
        xieChengCollidingCleanThread.setMaximumPoolSize(marketingCommonConfig.getXieChengCleanThreadCount());
        xieChengCollidingCleanThread.setCorePoolSize(marketingCommonConfig.getXieChengCleanThreadCount());
    }


    private int saveDataContrast(List<XieChengCollidingDataTemp> p, Integer ruleTypeFlag, Long packageId, String splitFilterScore) {
        List<XieChengCollidingDataContrast> xieChengCollidingDataContrastList = new ArrayList<>();
        for (XieChengCollidingDataTemp xt : p) {
            XieChengCollidingDataContrast xieChengCollidingDataContrast = new XieChengCollidingDataContrast();
            xieChengCollidingDataContrast.setRuleTypeFlag(ruleTypeFlag);
            xieChengCollidingDataContrast.setCellSha256CodeList(xt.getCell());
            xieChengCollidingDataContrast.setPackageId(packageId);
            xieChengCollidingDataContrast.setBatchNumber(xt.getBatchNumber());
            xieChengCollidingDataContrast.setExtend(splitFilterScore);
            xieChengCollidingDataContrastList.add(xieChengCollidingDataContrast);
        }
        try {
            xieChengCollidingDataContrastMapper.saveBatch(xieChengCollidingDataContrastList);
        } catch (DuplicateKeyException keyException) {
            log.warn("携程数据清洗有重复数据");
        }
        return 0;
    }

    private int saveRobData(List<XieChengCollidingDataContrast> p) {
        List<XieChengCollidingDataRob> xieChengCollidingDataContrastList = new ArrayList<>();
        for (XieChengCollidingDataContrast x : p) {
            XieChengCollidingDataRob xieChengCollidingDataRob = new XieChengCollidingDataRob();
            xieChengCollidingDataRob.setPackageId(x.getPackageId());
            xieChengCollidingDataRob.setCellSha256CodeList(x.getCellSha256CodeList());
            xieChengCollidingDataRob.setDataSourceType("F");
            xieChengCollidingDataContrastList.add(xieChengCollidingDataRob);
        }
        try {
            xieChengCollidingDataRobMapper.saveBatch(xieChengCollidingDataContrastList);
        } catch (DuplicateKeyException keyException) {
            log.warn("携程数据清洗有重复数据");
        }
        return 0;
    }

    private int deleteRobData(List<Long> ids) {
        // 删除非周期表内所有数据
        return xieChengCollidingDataRobMapper.updateBatchByIdToIsDeleted(ids);
    }

    private int deleteLoopCycleData(List<Long> ids) {
        return xieChengCollidingDataLoopCycleMapper.updateBatchByIdToIsDeleted(ids);
    }

    private int deleteContrastData(List<Long> ids) {
        return xieChengCollidingDataContrastMapper.updateBatchByIdToIsDeleted(ids);
    }

    private Long savePackage(String packageName, Integer priority, String collidingTime, String splitFilterScore) {
        // 新建package
        XieChengCollidingDataPackage xieChengCollidingDataPackage = new XieChengCollidingDataPackage();
        xieChengCollidingDataPackage.setPackageName(packageName);
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            xieChengCollidingDataPackage.setCollidingTime(simpleFormatter.parse(collidingTime));
        } catch (ParseException e) {
            log.error("携程撞库数据清洗，时间格式异常：{}", collidingTime, e);
        }
        xieChengCollidingDataPackage.setPriority(priority);
        xieChengCollidingDataPackage.setExtend(splitFilterScore);
        xieChengCollidingDataPackageMapper.insertSelective(xieChengCollidingDataPackage);
        return xieChengCollidingDataPackage.getId();
    }
}
