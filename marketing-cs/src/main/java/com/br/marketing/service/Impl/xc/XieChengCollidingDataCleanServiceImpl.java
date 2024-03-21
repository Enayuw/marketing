package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataContrast;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.mapper.XieChengCollidingDataContrastMapper;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XieChengCollidingDataRobMapper;
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


    /**
     * 主流程
     *
     * @param jobParameter job 参数
     */
    @Override
    public void process(String jobParameter) {

        ThreadPoolExecutor xieChengCollidingCleanThread =
                BrExecutors.getThreadPool(50, 50);

        // 解析job 参数
        ParameterToJson result = getParameterToJson(jobParameter);
        if (result == null) {
            return;
        }
        // 跑分数据存入对比表
        xieChengCollidingDataContrastProcess(result.filterInfoArray, xieChengCollidingCleanThread, result.temporaryTable);
        // 对比表数据同周期数据处理逻辑
        xieChengCollidingLoopDataCycleProcess(xieChengCollidingCleanThread, result.loopCycleSwitch);
        // 对比表数据同非周期数据处理逻辑
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
            JSONArray filterInfoArray = parseJson.getJSONArray("filter_info");
            Boolean loopCycleSwitch = parseJson.getBoolean("loop_cycle_switch");
            Boolean robSwitch = parseJson.getBoolean("rob_switch");
            return new ParameterToJson(temporaryTable, filterInfoArray, loopCycleSwitch, robSwitch);
        } catch (Exception e) {
            log.error("携程清洗job参数异常：{}", jobParameter);
        }
        return null;
    }

    private static class ParameterToJson {
        public final String temporaryTable;
        public final JSONArray filterInfoArray;
        public final Boolean loopCycleSwitch;
        public final Boolean robSwitch;

        public ParameterToJson(String temporaryTable, JSONArray filterInfoArray, Boolean loopCycleSwitch, Boolean robSwitch) {
            this.temporaryTable = temporaryTable;
            this.filterInfoArray = filterInfoArray;
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
    private void xieChengCollidingDataContrastProcess(JSONArray filterInfoArray, ThreadPoolExecutor xieChengCollidingCleanThread, String temporaryTable) {
        for (int i = 0; i < filterInfoArray.size(); i++) {
            String filterInfo = filterInfoArray.get(i).toString();
            JSONObject filterInfoJson = JSONObject.parseObject(filterInfo);
            String filterScore = filterInfoJson.getString("filter_score");
            String packageName = filterInfoJson.getString("package_name");
            String collidingTime = filterInfoJson.getString("colliding_time");
            Integer priority = filterInfoJson.getInteger("priority");
            // 生成新的packageId
            Long packageId = savePackage(packageName, priority, collidingTime);
            // 携程数据清洗进入到对比表
            xieChengCollidingDataCleanProcess(xieChengCollidingCleanThread, temporaryTable, filterScore, i + 1, packageId);
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
            // 删除非周期表内所有数据
            xieChengCollidingDataRobMapper.updateOnBatchToIsDeleted();
            // 插入对比表中不在非周期表中的数据
            while (true) {
                List<XieChengCollidingDataContrast> idLists = xieChengCollidingDataContrastMapper.robCelltiflash_(100000);
                if (idLists.isEmpty()) {
                    break;
                }
                // 多线程删除非周期表数据
                List<List<XieChengCollidingDataContrast>> partition = Lists.partition(idLists, 10000);
                List<Future<Integer>> futureList = new ArrayList<>();
                for (List<XieChengCollidingDataContrast> p : partition) {
                    Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> saveRobData(p));
                    futureList.add(submit);
                }
                futureFinish(futureList);
            }
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
     * @param xieChengCollidingCleanLoopCycleThread 线程池
     */
    private void doContrastWork(ThreadPoolExecutor xieChengCollidingCleanLoopCycleThread) {
        while (true) {
            List<Long> idLists = xieChengCollidingDataContrastMapper.loopCycleCellExisttiflash_(100000);
            if (idLists.isEmpty()) {
                break;
            }
            // 多线程插入对比表
            List<List<Long>> partition = Lists.partition(idLists, 10000);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<Long> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanLoopCycleThread.submit(() -> deleteContrastData(p));
                futureList.add(submit);
            }
            futureFinish(futureList);
        }
    }

    /**
     * 周期数据处理
     *
     * @param xieChengCollidingCleanLoopCycleThread 线程池
     */
    private void doLoopCycleWork(ThreadPoolExecutor xieChengCollidingCleanLoopCycleThread) {
        while (true) {
            List<Long> idLists = xieChengCollidingDataContrastMapper.loopCycleCelltiflash_(100000);
            if (idLists.isEmpty()) {
                break;
            }
            // 多线程插入对比表
            List<List<Long>> partition = Lists.partition(idLists, 10000);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<Long> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanLoopCycleThread.submit(() -> deleteLoopCycleData(p));
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
    private void xieChengCollidingDataCleanProcess(ThreadPoolExecutor xieChengCollidingCleanThread, String tableName, String filterScore, Integer ruleTypeFlag, Long packageId) {
        while (true) {
            List<Map<String, String>> cellList = xieChengCollidingDataContrastMapper.temporaryCelltiflash_(tableName, filterScore, 100000);
            if (cellList.isEmpty()) {
                break;
            }
            // 多线程插入对比表
            List<List<Map<String, String>>> partition = Lists.partition(cellList, 10000);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<Map<String, String>> p : partition) {
                Future<Integer> submit = xieChengCollidingCleanThread.submit(() -> saveDataContrast(p, ruleTypeFlag, packageId));
                futureList.add(submit);
            }
            futureFinish(futureList);
        }
    }


    private int saveDataContrast(List<Map<String, String>> p, Integer ruleTypeFlag, Long packageId) {
        List<XieChengCollidingDataContrast> xieChengCollidingDataContrastList = new ArrayList<>();
        for (Map<String, String> map : p) {
            XieChengCollidingDataContrast xieChengCollidingDataContrast = new XieChengCollidingDataContrast();
            xieChengCollidingDataContrast.setRuleTypeFlag(ruleTypeFlag);
            xieChengCollidingDataContrast.setCellSha256CodeList(map.get("cell"));
            xieChengCollidingDataContrast.setPackageId(packageId);
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

    private int deleteLoopCycleData(List<Long> ids) {
        return xieChengCollidingDataLoopCycleMapper.updateBatchByIdToIsDeleted(ids);
    }

    private int deleteContrastData(List<Long> ids) {
        return xieChengCollidingDataContrastMapper.updateBatchByIdToIsDeleted(ids);
    }

    private Long savePackage(String packageName, Integer priority, String collidingTime) {
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
        xieChengCollidingDataPackageMapper.insertSelective(xieChengCollidingDataPackage);
        return xieChengCollidingDataPackage.getId();
    }
}
