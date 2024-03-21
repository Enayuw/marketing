package com.br.marketing.service.Impl.xc;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sun.security.provider.MD5;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
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

    @Override
    public void testInsert() {
        for (int i = 0; i < 100000; i++) {
            String s = i + "";
            String s1 = Md5Utils.cell32(s);
            XieChengCollidingDataLoopCycle loopCycle = new XieChengCollidingDataLoopCycle();
            loopCycle.setPackageId(Long.valueOf(s));
            loopCycle.setDataSourceType("T");
            loopCycle.setCellSha256CodeList(s1);
            loopCycle.setReleaseTime(new Date());
//            loopCycle.setpushTime()
            loopCycle.setIsDelete(1);
//            loopCycle.setExtend("LoopCycle extend");
            loopCycle.setCreateTime(new Date());
            loopCycle.setUpdateTime(new Date());
            loopCycle.setRetryCount(0);

            xieChengCollidingDataLoopCycleMapper.insert(loopCycle);

            XieChengCollidingDataRob rob = new XieChengCollidingDataRob();
            rob.setPackageId(Long.valueOf(s));
            rob.setDataSourceType("T");
            rob.setCellSha256CodeList(s1);
            rob.setReleaseTime(new Date());
            rob.setPushTime(new Date());
            rob.setIsDelete(1);
//            rob.setExtend("rob extend");
            rob.setCreateTime(new Date());
            rob.setUpdateTime(new Date());
            xieChengCollidingDataRobMapper.insert(rob);

            XieChengCollidingDataLog log = new XieChengCollidingDataLog();
            log.setSmsCollidingDataId(Long.valueOf(i));
            log.setPackageId(Long.valueOf(i));
            log.setDataSourceType("T");
            log.setCellSha256CodeList(s1);
            log.setReleaseTime("2024-03-20 00:00:00");
            log.setOrgChannel("xc");
            log.setMktLevel("重点营销");
            log.setInfo("后续可再次撞库");
            log.setResult(Boolean.TRUE);
            log.setHttpCode(200);
            log.setBusinessCode(00);
            log.setReturnContent("null");
            log.setIsDelete(1);
            log.setCreateTime(new Date());
            log.setUpdateTime(new Date());
            xieChengCollidingDataLogMapper.insert(log);

            XieChengCollidingDataContrast contrast = new XieChengCollidingDataContrast();
            contrast.setRuleTypeFlag(i);
            contrast.setCellSha256CodeList(s1);
            contrast.setIsDelete(1);
//            contrast.setExtend("contrast extend");
            contrast.setCreateTime(new Date());
            contrast.setUpdateTime(new Date());
            xieChengCollidingDataContrastMapper.insert(contrast);
        }

    }
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
        List<XieChengCollidingDataLoopCycle> xieChengCollidingDataLoopCycles =
                xieChengCollidingDataLoopCycleMapper.selectDeleteData(daysAgo14, limit);
        List<List<XieChengCollidingDataLoopCycle>> loopCycleListPartition =
                Lists.partition(xieChengCollidingDataLoopCycles, 200);
        List<Future<String>> futureList = new ArrayList<>();
        loopCycleListPartition.forEach((List<XieChengCollidingDataLoopCycle> p) -> {
            Future<String> submit = loopCycleThread.submit(() -> loopCycleBackupAndDelete(p));
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
        List<XieChengCollidingDataRob> xieChengCollidingDataRob =
                xieChengCollidingDataRobMapper.selectDeleteData(daysAgo14, limit);
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
        List<XieChengCollidingDataLog> xieChengCollidingDataLog =
                xieChengCollidingDataLogMapper.selectDeleteData(daysAgo14, limit);
        List<List<XieChengCollidingDataLog>> robListPartition =
                Lists.partition(xieChengCollidingDataLog, 200);
        List<Future<String>> futureList = new ArrayList<>();
        robListPartition.forEach((List<XieChengCollidingDataLog> p) -> {
            Future<String> submit = logThread.submit(() -> logBackupAndDelete(p));
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
    public void contrastHandle(String nowString,int limit) {
        // 创建撞库线程池
        ThreadPoolExecutor logThread = BrExecutors.getThreadPool(30, 30, "contrastDelete");
        List<XieChengCollidingDataContrast> xieChengCollidingDataContrast =
                xieChengCollidingDataContrastMapper.selectDeleteData(nowString, limit);
        List<List<XieChengCollidingDataContrast>> contrastListPartition =
                Lists.partition(xieChengCollidingDataContrast, 200);
        List<Future<String>> futureList = new ArrayList<>();
        contrastListPartition.forEach((List<XieChengCollidingDataContrast> p) -> {
            Future<String> submit = logThread.submit(() -> contrastDelete(p));
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
