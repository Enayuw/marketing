package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 携程先关表备份具体处理类
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-03-20
 */
@Service
@Slf4j
public class TableBackupServiceImpl implements TableBackupService{

    /**
     * 周期表
     */
    @Resource
    private XieChengCollidingDataLoopCycleMapper xieChengCollidingDataLoopCycleMapper;
    /**
     * 周期表-备份
     */
    @Resource
    private XieChengCollidingDataLoopCycleArchiveMapper xieChengCollidingDataLoopCycleArchiveMapper;
    /**
     * 非周期表
     */
    @Resource
    private XieChengCollidingDataRobMapper xieChengCollidingDataRobMapper;
    /**
     * 非周期表-备份
     */
    @Resource
    private XieChengCollidingDataRobArchiveMapper xieChengCollidingDataRobArchiveMapper;
    /**
     * 日志表
     */
    @Resource
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;
    /**
     * 日志表-备份
     */
    @Resource
    private XieChengCollidingDataLogArchiveMapper xieChengCollidingDataLogArchiveMapper;
    /**
     * 对比表
     */
    @Resource
    private XieChengCollidingDataContrastMapper xieChengCollidingDataContrastMapper;


    @Override
    public void loopCycleHandle(String daysAgo14,int limit) {
        // 创建撞库线程池
        ThreadPoolExecutor loopCycleThread = BrExecutors.getThreadPool(30, 30, "loopCycleBackup");
        int count = 0;
        while(true){
            List<XieChengCollidingDataLoopCycle> xieChengCollidingDataLoopCycles =
                    xieChengCollidingDataLoopCycleMapper.selectDeleteData(daysAgo14, limit);
            int size = xieChengCollidingDataLoopCycles.size();
            if(size<1){
                break;
            }
            count = count + size;
            List<List<XieChengCollidingDataLoopCycle>> loopCycleListPartition =
                    Lists.partition(xieChengCollidingDataLoopCycles, 200);
            List<Future<String>> futureList = new ArrayList<>();
            loopCycleListPartition.forEach((List<XieChengCollidingDataLoopCycle> p) -> {
                Future<String> submit = loopCycleThread.submit(() -> loopCycleBackupAndDelete(p));
                futureList.add(submit);
            });
            // 等待上面执行结束，确保下次循环开始时能正常查询需要备份的数据量
            for (int i = 0; i < futureList.size(); i++) {
                Future<String> stringFuture = futureList.get(i);
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
        loopCycleThread.shutdown();
        try {
            while (!loopCycleThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程周期表备份线程池关闭");
            }
        } catch (InterruptedException ex) {
            loopCycleThread.shutdownNow();
            log.error("携程周期表备份线程池关闭异常！", ex);
            Thread.currentThread().interrupt();
        }
        log.warn("本次job需要备份的xc周期数据量为:{}--daysAgo14[{}]--limit[{}]",count,daysAgo14,limit);
    }
    /**
     * 分批执行周期表备份和删除
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/22 11:38
     * @param loopCycleList list
     * @return String 无实际意义
     */
    public String loopCycleBackupAndDelete(List<XieChengCollidingDataLoopCycle> loopCycleList) {
        try {
            int listSize =  loopCycleList.size();
            List<XieChengCollidingDataLoopCycleArchive> archiveList = new ArrayList(listSize);
            List<Long> idList = new ArrayList(listSize);
            for (int i = 0; i < loopCycleList.size(); i++) {
                XieChengCollidingDataLoopCycle xieChengCollidingDataLoopCycle = loopCycleList.get(i);
                idList.add(xieChengCollidingDataLoopCycle.getId());
                XieChengCollidingDataLoopCycleArchive archive = new XieChengCollidingDataLoopCycleArchive(xieChengCollidingDataLoopCycle);
                archiveList.add(archive);
            }
            xieChengCollidingDataLoopCycleArchiveMapper.saveBatch(archiveList);
            // 数据删除
            xieChengCollidingDataLoopCycleMapper.deleteByIdList(idList,listSize);
        }catch (Exception e){
            log.error("携程周期数据备份异常,数据[{}]--", JSONObject.toJSON(loopCycleList),e);
        }
        return "";
    }

    @Override
    public void robHandle(String daysAgo14,int limit) {
        // 创建撞库线程池
        ThreadPoolExecutor robThread = BrExecutors.getThreadPool(30, 30, "robBackup");
        int count = 0;
        while(true){
            List<XieChengCollidingDataRob> xieChengCollidingDataRob =
                    xieChengCollidingDataRobMapper.selectDeleteData(daysAgo14, limit);
            int size = xieChengCollidingDataRob.size();
            if(size<1){
                break;
            }
            count = count + size;
            List<List<XieChengCollidingDataRob>> robListPartition =
                    Lists.partition(xieChengCollidingDataRob, 200);
            List<Future<String>> futureList = new ArrayList<>();
            robListPartition.forEach((List<XieChengCollidingDataRob> p) -> {
                Future<String> submit = robThread.submit(() -> robBackupAndDelete(p));
                futureList.add(submit);
            });
            // 等待上面执行结束，确保下次循环开始时能正常查询已经撞得数据量
            for (int i = 0; i < futureList.size(); i++) {
                Future<String> stringFuture = futureList.get(i);
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
        robThread.shutdown();
        try {
            while (!robThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程非周期表备份线程池关闭");
            }
        } catch (InterruptedException ex) {
            robThread.shutdownNow();
            log.error("携程非周期表备份线程池关闭异常！", ex);
            Thread.currentThread().interrupt();
        }
        log.warn("本次job需要备份的xc非周期数据量为:{}--daysAgo14[{}]--limit[{}]",count,daysAgo14,limit);
    }
    /**
     * 分批执行非周期表备份和删除
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/22 11:39
     * @param robList list
     * @return String 无实际意义
     */
    public String robBackupAndDelete(List<XieChengCollidingDataRob> robList) {
        try {
            int listSize =  robList.size();
            List<XieChengCollidingDataRobArchive> archiveList = new ArrayList(listSize);
            List<Long> idList = new ArrayList(listSize);
            for (int i = 0; i < robList.size(); i++) {
                XieChengCollidingDataRob xieChengCollidingDataRob = robList.get(i);
                idList.add(xieChengCollidingDataRob.getId());
                XieChengCollidingDataRobArchive archive = new XieChengCollidingDataRobArchive(xieChengCollidingDataRob);
                archiveList.add(archive);
            }
            xieChengCollidingDataRobArchiveMapper.saveBatch(archiveList);
            // 数据删除
            xieChengCollidingDataRobMapper.deleteByIdList(idList,listSize);
        }catch (Exception e){
            log.error("携程非周期数据备份异常,数据[{}]--", JSONObject.toJSON(robList),e);
        }
        return "";
    }

    @Override
    public void logHandle(String daysAgo14,int limit) {
        // 创建撞库线程池
        ThreadPoolExecutor logThread = BrExecutors.getThreadPool(30, 30, "logBackup");
        int count = 0;
        while(true){
            List<XieChengCollidingDataLog> xieChengCollidingDataLog =
                    xieChengCollidingDataLogMapper.selectDeleteData(daysAgo14, limit);
            int size = xieChengCollidingDataLog.size();
            if(size<1){
                break;
            }
            count = count + size;
            List<List<XieChengCollidingDataLog>> robListPartition =
                    Lists.partition(xieChengCollidingDataLog, 200);
            List<Future<String>> futureList = new ArrayList<>();
            robListPartition.forEach((List<XieChengCollidingDataLog> p) -> {
                Future<String> submit = logThread.submit(() -> logBackupAndDelete(p));
                futureList.add(submit);
            });
            // 等待上面执行结束，确保下次循环开始时能正常查询需要备份的数据量
            for (int i = 0; i < futureList.size(); i++) {
                Future<String> stringFuture = futureList.get(i);
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
        logThread.shutdown();
        try {
            while (!logThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程日志表备份线程池关闭");
            }
        } catch (InterruptedException ex) {
            logThread.shutdownNow();
            log.error("携程日志表备份线程池关闭异常！", ex);
            Thread.currentThread().interrupt();
        }
        log.warn("本次job需要备份的xc-log数据量为:{}--daysAgo14[{}]--limit[{}]",count,daysAgo14,limit);
    }
    /**
     * 分批执行日志表备份和删除
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/22 11:40
     * @param logList list
     * @return String 无实际意义
     */
    public String logBackupAndDelete(List<XieChengCollidingDataLog> logList) {
        try {
            int listSize =  logList.size();
            List<XieChengCollidingDataLogArchive> archiveList = new ArrayList(listSize);
            List<Long> idList = new ArrayList(listSize);
            for (int i = 0; i < logList.size(); i++) {
                XieChengCollidingDataLog xieChengCollidingDataLog = logList.get(i);
                idList.add(xieChengCollidingDataLog.getId());
                XieChengCollidingDataLogArchive archive = new XieChengCollidingDataLogArchive(xieChengCollidingDataLog);
                archiveList.add(archive);
            }
            xieChengCollidingDataLogArchiveMapper.saveBatch(archiveList);
            // 数据删除
            xieChengCollidingDataLogMapper.deleteByIdList(idList,listSize);
        }catch (Exception e){
            log.error("携程log数据备份异常,数据[{}]--", JSONObject.toJSON(logList),e);
        }
        return "";
    }

    @Override
    public void contrastHandle(String daysAgo14,int limit) {
        // 创建撞库线程池
        ThreadPoolExecutor contrastThread = BrExecutors.getThreadPool(30, 30, "contrastDelete");
        int count = 0;
        while(true){
            List<XieChengCollidingDataContrast> xieChengCollidingDataContrast =
                    xieChengCollidingDataContrastMapper.selectDeleteData(daysAgo14, limit);
            int size = xieChengCollidingDataContrast.size();
            if(size<1){
                break;
            }
            count = count + size;
            List<List<XieChengCollidingDataContrast>> contrastListPartition =
                    Lists.partition(xieChengCollidingDataContrast, 200);
            List<Future<String>> futureList = new ArrayList<>();
            contrastListPartition.forEach((List<XieChengCollidingDataContrast> p) -> {
                Future<String> submit = contrastThread.submit(() -> contrastDelete(p));
                futureList.add(submit);
            });
            // 等待上面执行结束，确保下次循环开始时能正常查询需要备份的数据量
            for (int i = 0; i < futureList.size(); i++) {
                Future<String> stringFuture = futureList.get(i);
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
        contrastThread.shutdown();
        try {
            while (!contrastThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("携程对比表备份线程池关闭");
            }
        } catch (InterruptedException ex) {
            contrastThread.shutdownNow();
            log.error("携程对比表备份线程池关闭异常！", ex);
            Thread.currentThread().interrupt();
        }
        log.warn("本次job需要删除的xc对比数据数据量为:{}--nowString[{}]--limit[{}]",count,daysAgo14,limit);
    }

    /**
     * 分批执行对比表删除
     * @Author yu.xia@brgroup.com
     * @Date 2024/3/22 11:41
     * @param contrastList  list
     * @return String 无实际意义
     */
    public String contrastDelete(List<XieChengCollidingDataContrast> contrastList) {
        try {
            int listSize =  contrastList.size();
            List<Long> idList = new ArrayList(listSize);
            for (int i = 0; i < contrastList.size(); i++) {
                XieChengCollidingDataContrast xieChengCollidingDataContrast = contrastList.get(i);
                idList.add(xieChengCollidingDataContrast.getId());
            }
            // 数据删除
            xieChengCollidingDataContrastMapper.deleteByIdList(idList,listSize);
        }catch (Exception e){
            log.error("携程对比表数据delete异常,数据[{}]--", JSONObject.toJSON(contrastList),e);
        }
        return "";
    }
}
