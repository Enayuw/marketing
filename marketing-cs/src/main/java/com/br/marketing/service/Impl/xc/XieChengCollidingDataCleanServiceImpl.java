package com.br.marketing.service.Impl.xc;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.XieChengCollidingDataContrast;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengSmsCollidingData;
import com.br.marketing.mapper.XieChengCollidingDataContrastMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.whois.WhoisClient;
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



    @Override
    public void process(String tableName,
                        String filterScore,
                        String packageName,
                        Boolean loopCycle,
                        Boolean loopCycleNon,
                        Integer priority,
                        String collidingTime,
                        Integer ruleTypeFlag) {
        // 携程数据清洗进入到对比表
        xieChengCollidingDataCleanProcess(tableName, filterScore, ruleTypeFlag);
        // 对比表数据同周期数据处理逻辑
        if(loopCycle){
            // 周期表内不符合分值条件的数据要删除掉

            // 对比表内符合分值条件且数据是true的数据删除掉
        }
        // 对比表数据同非周期数据处理逻辑
        if(loopCycleNon){
            // 生成新的packageId
            // 删除非周期表内对应的packageId
            // 删除非周期表内不符合分值的数据
            // 更新符合分值的非周期数据的packageId
        }
    }

    private void xieChengCollidingDataCleanProcess(String tableName, String filterScore, Integer ruleTypeFlag) {
        ThreadPoolExecutor xieChengCollidingCleanThread =
                BrExecutors.getThreadPool(50, 50);
        while (true) {
            List<Map<String, String>> cellList = xieChengCollidingDataContrastMapper.temporaryCelltiflash_(tableName, filterScore, 100000);
            if (cellList.isEmpty()) {
                break;
            }
            // 多线程插入对比表
            List<List<Map<String, String>>> partition = Lists.partition(cellList, 10000);
            List<Future<Integer>> futureList = new ArrayList<>();
            for (List<Map<String, String>> p : partition) {
                Future<Integer> submit =   xieChengCollidingCleanThread.submit(() -> saveDataContrast(p, ruleTypeFlag));
                futureList.add(submit);
            }
            for (int i = 0; i < futureList.size(); i++) {
                Future<Integer> stringFuture = futureList.get(i);
                try {
                    stringFuture.get(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    log.warn("InterruptedException:",e);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    log.warn("InterruptedException:",e);
                } catch (TimeoutException e) {
                    log.warn("TimeoutException:",e);
                }
            }
    }
        try {
            while (!xieChengCollidingCleanThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程撞库数据清洗线程池关闭");
            }
        } catch (InterruptedException ex) {
            xieChengCollidingCleanThread.shutdownNow();
            log.error("携程撞库数据清洗线程池关闭异常！",ex);
            Thread.currentThread().interrupt();
        }
    }

    private int saveDataContrast(List<Map<String, String>> p, Integer ruleTypeFlag) {
        List<XieChengCollidingDataContrast> xieChengCollidingDataContrastList = new ArrayList<>();
        for (Map<String, String> map : p) {
            XieChengCollidingDataContrast xieChengCollidingDataContrast = new XieChengCollidingDataContrast();
            xieChengCollidingDataContrast.setRuleTypeFlag(ruleTypeFlag);
            xieChengCollidingDataContrast.setCellSha256CodeList(map.get("cell"));
            xieChengCollidingDataContrastList.add(xieChengCollidingDataContrast);
        }
       int n = xieChengCollidingDataContrastMapper.saveBatch(xieChengCollidingDataContrastList);
        return n;
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
        Long packageId = xieChengCollidingDataPackage.getId();
        return packageId;
    }
}
