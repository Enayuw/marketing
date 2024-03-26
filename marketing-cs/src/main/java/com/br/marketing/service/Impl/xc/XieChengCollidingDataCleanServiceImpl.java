package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.XieChengCollidingDataContrastMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.osgi.service.log.LogEntry;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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


    private final static Integer PARTITIONCOUNT = 5000;

    /**
     * 主流程
     *
     * @param jobParameter job 参数
     */
    @Override
    public void process(String jobParameter) {

        // 创建线程池
        ThreadPoolExecutor xieChengCollidingCleanThread = getThreadPoolExecutor();

        // 参数解析
        ParameterToJson result = getParameter(jobParameter);
        if (result == null) {return;}

        // 清洗主流程
        processWork(result, xieChengCollidingCleanThread);

        // 关闭线程池
        closeThreadPool(xieChengCollidingCleanThread);
    }

    private ThreadPoolExecutor getThreadPoolExecutor() {
        return BrExecutors.getThreadPool(marketingCommonConfig.getXieChengCleanThreadCount(),
                        marketingCommonConfig.getXieChengCleanThreadCount());
    }

    private ParameterToJson getParameter(String jobParameter) {
        // 解析job 参数
        ParameterToJson result = getParameterToJson(jobParameter);
        if (result == null) {
            return null;
        }
        List<Map<String, String>> maps = xieChengCollidingDataContrastMapper.temporaryCellCountRepeattiflash_(result.temporaryTable);
        if (!maps.isEmpty()) {
            log.error("携程撞库跑分临时表含有重复数据请手动处理！{}", maps);
            return null;
        }
        return result;
    }

    private static void closeThreadPool(ThreadPoolExecutor xieChengCollidingCleanThread) {
        xieChengCollidingCleanThread.shutdown();
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

    private void processWork(ParameterToJson result, ThreadPoolExecutor xieChengCollidingCleanThread) {
        // 跑分数据存入对比表
        xieChengCollidingDataContrastProcess(result.packageRuleInfo,xieChengCollidingCleanThread, result.temporaryTable);
        // 对比表数据同周期数据处理逻辑
        xieChengCollidingLoopDataCycleProcess(xieChengCollidingCleanThread, result.loopCycleSwitch);
        // 对比表数据同非周期数据处理逻辑
        xieChengCollidingRobDataProcess(xieChengCollidingCleanThread, result.robSwitch);
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
            JSONArray packageRuleInfo = parseJson.getJSONArray("package_rule_info");
            Boolean loopCycleSwitch = parseJson.getBoolean("loop_cycle_switch");
            Boolean robSwitch = parseJson.getBoolean("rob_switch");
            return new ParameterToJson(temporaryTable, packageRuleInfo, loopCycleSwitch, robSwitch);
        } catch (Exception e) {
            log.error("携程清洗job参数异常：{}", jobParameter);
        }
        return null;
    }

    private static class ParameterToJson {
        public final String temporaryTable;
        public final JSONArray packageRuleInfo;
        public final Boolean loopCycleSwitch;
        public final Boolean robSwitch;

        public ParameterToJson(String temporaryTable,
                               JSONArray packageRuleInfo, Boolean loopCycleSwitch, Boolean robSwitch
        ) {
            this.temporaryTable = temporaryTable;
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
    private void xieChengCollidingDataContrastProcess(JSONArray filterInfoArray,
                                                      ThreadPoolExecutor xieChengCollidingCleanThread, String temporaryTable) {

        for (int i = 0; i < filterInfoArray.size(); i++) {
            try {
                String filterInfo = filterInfoArray.get(i).toString();
                JSONObject filterInfoJson = JSONObject.parseObject(filterInfo);
                String splitFilterScore = filterInfoJson.getString("split_filter_score");
                String packageName = filterInfoJson.getString("package_name");
                String collidingTime = filterInfoJson.getString("colliding_time");
                Integer priority = filterInfoJson.getInteger("priority");
                // 根据包名称创建package
                Long packageId = getPackageId(packageName, priority, collidingTime, splitFilterScore);
                // 携程数据清洗进入到对比表
                xieChengCollidingDataCleanProcess(xieChengCollidingCleanThread,
                        temporaryTable, i + 1, packageId, splitFilterScore);
            }catch (Exception e){
                log.error("携程清洗job‘参数处理异常",e);
            }

        }
    }

    private Long getPackageId(String packageName, Integer priority, String collidingTime, String splitFilterScore) {
        XieChengCollidingDataPackageExample xe = new XieChengCollidingDataPackageExample();
        xe.createCriteria().andPackageNameEqualTo(packageName).andIsDeleteEqualTo(0);
        List<XieChengCollidingDataPackage> xieChengCollidingDataPackages = xieChengCollidingDataPackageMapper.selectByExample(xe);
        if (xieChengCollidingDataPackages.size() > 1) {
            log.error("携程数据清洗packageName重复：{}", packageName);
        }
        Long packageId;
        if (xieChengCollidingDataPackages.isEmpty()) {
            // 生成新的packageId
            packageId = savePackage(packageName, priority, collidingTime, splitFilterScore);
        } else {
            packageId = xieChengCollidingDataPackages.get(0).getId();
        }
        return packageId;
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
            // 删除对比表中的数据
            deleteContrastData(xieChengCollidingCleanThread);
        }

    }

    private void splitRobData(ThreadPoolExecutor xieChengCollidingCleanThread) {
        try {
            while (marketingCommonConfig.getXieChengCleanSwitch()) {
                setThreadCount(xieChengCollidingCleanThread);
                List<XieChengCollidingDataContrast> idLists =
                        xieChengCollidingDataContrastMapper.robCelltiflash_(marketingCommonConfig.getXieChengCleanLimitCount());
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
        }catch (Exception e){
            log.error("携程清洗job非周期数据存储异常",e);
        }

    }
    private void deleteContrastData(ThreadPoolExecutor xieChengCollidingCleanThread) {
        try {
            while (marketingCommonConfig.getXieChengCleanSwitch()) {
                setThreadCount(xieChengCollidingCleanThread);
                List<Long> idLists =
                        xieChengCollidingDataContrastMapper.contrastExistCelltiflash_(marketingCommonConfig.getXieChengCleanLimitCount());
                if (idLists.isEmpty()) {
                    break;
                }
                List<List<Long>> partition = Lists.partition(idLists, PARTITIONCOUNT);
                List<Future<Integer>> futureList = new ArrayList<>();
                for (List<Long> p : partition) {
                    Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> deleteContrastData(p));
                    futureList.add(submit);
                }
                futureFinish(futureList);
            }
        }catch (Exception e){
            log.error("携程清洗job非周期数据删除对比表异常",e);
        }

    }
    private void deleteRobData(ThreadPoolExecutor xieChengCollidingCleanThread) {
        try {
            while (marketingCommonConfig.getXieChengCleanSwitch()) {
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
        }catch (Exception e){
            log.error("携程清洗job删除非周期表数据异常",e);
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
        try {
            while (marketingCommonConfig.getXieChengCleanSwitch()) {
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
        }catch (Exception e){
            log.error("携程清洗job周期True删除对比表异常",e);
        }

    }

    /**
     * 周期数据处理
     *
     * @param xieChengCollidingCleanThread 线程池
     */
    private void doLoopCycleWork(ThreadPoolExecutor xieChengCollidingCleanThread) {
        try {
            while (marketingCommonConfig.getXieChengCleanSwitch()) {
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
        }catch (Exception e){
            log.error("携程清洗job周期True表数据处理异常",e);
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
                stringFuture.get(30,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("InterruptedException:", e);
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException e) {
                log.warn("ExecutionException:", e);
            }
        }
    }

    /**
     * 携程撞库数据清理主流程
     *
     * @param xieChengCollidingCleanThread 线程池
     * @param tableName                    临时表
     * @param ruleTypeFlag                 规则
     * @param packageId                    包id
     */
    private void xieChengCollidingDataCleanProcess(ThreadPoolExecutor xieChengCollidingCleanThread,
                                                   String tableName, Integer ruleTypeFlag, Long packageId, String splitFilterScore) {
        try {
            while (marketingCommonConfig.getXieChengCleanSwitch()) {
                setThreadCount(xieChengCollidingCleanThread);
                List<XieChengCollidingDataTemp> cellList = xieChengCollidingDataContrastMapper.temporaryCelltiflash_(tableName, splitFilterScore,
                        marketingCommonConfig.getXieChengCleanLimitCount());
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
        }catch (Exception e){
            log.error("携程清洗job对比表操作异常",e);
        }

    }

    private void setThreadCount(ThreadPoolExecutor xieChengCollidingCleanThread) {
        xieChengCollidingCleanThread.setMaximumPoolSize(marketingCommonConfig.getXieChengCleanThreadCount());
        xieChengCollidingCleanThread.setCorePoolSize(marketingCommonConfig.getXieChengCleanThreadCount());
    }


    private int saveDataContrast(List<XieChengCollidingDataTemp> p, Integer ruleTypeFlag, Long packageId, String splitFilterScore) {
        List<XieChengCollidingDataContrast> xieChengCollidingDataContrastList = new ArrayList<>();
        try {
        for (XieChengCollidingDataTemp xt : p) {
            XieChengCollidingDataContrast xieChengCollidingDataContrast = new XieChengCollidingDataContrast();
            xieChengCollidingDataContrast.setRuleTypeFlag(ruleTypeFlag);
            xieChengCollidingDataContrast.setCellSha256CodeList(xt.getCell());
            xieChengCollidingDataContrast.setPackageId(packageId);
            xieChengCollidingDataContrast.setBatchNumber(xt.getBatchNumber());
            xieChengCollidingDataContrast.setExtend(splitFilterScore);
            xieChengCollidingDataContrastList.add(xieChengCollidingDataContrast);
        }
            xieChengCollidingDataContrastMapper.saveBatch(xieChengCollidingDataContrastList);
        } catch (DuplicateKeyException keyException) {
            log.warn("携程清洗job保存对比表数据异常");
        }
        return 0;
    }

    private int saveRobData(List<XieChengCollidingDataContrast> p) {
        List<XieChengCollidingDataRob> xieChengCollidingDataContrastList = new ArrayList<>();
        try {
            for (XieChengCollidingDataContrast x : p) {
                XieChengCollidingDataRob xieChengCollidingDataRob = new XieChengCollidingDataRob();
                xieChengCollidingDataRob.setPackageId(x.getPackageId());
                xieChengCollidingDataRob.setCellSha256CodeList(x.getCellSha256CodeList());
                xieChengCollidingDataRob.setDataSourceType("F");
                xieChengCollidingDataContrastList.add(xieChengCollidingDataRob);
            }
            xieChengCollidingDataRobMapper.saveBatch(xieChengCollidingDataContrastList);
        } catch (Exception e) {
            log.warn("携程数据清洗有重复数据",e);
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
        xieChengCollidingDataPackage.setCreateTime(new Date());
        xieChengCollidingDataPackage.setUpdateTime(new Date());
        xieChengCollidingDataPackageMapper.insertSelective(xieChengCollidingDataPackage);
        return xieChengCollidingDataPackage.getId();
    }
}
