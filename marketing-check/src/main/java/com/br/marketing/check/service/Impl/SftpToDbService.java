//package com.br.marketing.check.service.Impl;
//
//import com.br.marketing.check.dto.FileContext;
//import com.br.marketing.check.enums.ErrorFileTypeEnum;
//import com.br.marketing.check.service.AbstractDataToDbService;
//import com.br.marketing.check.utils.SftpToDbUtils;
//import com.br.marketing.client.RedisChgService;
//import com.br.marketing.client.SftpClient;
//import com.br.marketing.common.utils.*;
//import com.br.marketing.common.utils.file.MyFileUtil;
//import com.br.marketing.entity.LoadResult;
//import com.br.marketing.entity.MarketingTask;
//import com.br.marketing.mapper.LoadResultMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.map.HashedMap;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.io.*;
//import java.util.*;
//
///**
// * @Author: Bairong
// * @Time: 2020/12/9 15:06
// * @Company：百融
// * @Description: 功能描述
// */
//@Service
//@Slf4j
//public class SftpToDbService extends AbstractDataToDbService {
//
//    /**
//     * The Load result mapper.
//     */
//    @Resource
//    LoadResultMapper loadResultMapper;
//    /**
//     * The File ckeck servicce.
//     */
//    @Resource
//    FileCheckServiceImpl fileCheckService;
//    /**
//     * The Redis chg service.
//     */
//    @Resource
//    RedisChgService redisChgService;
//
//    private final static Integer SPLITSIZE=5000;
//
//    @Override
//    public Boolean checkTxtFile(FileContext context) {
//        StringBuilder errorMessage=new StringBuilder("数据文件异常,");
//        if(!fileCheckService.checkTxtfile(context,errorMessage)){
//            return false;
//        }
//        String txtFilePathAndName=context.getLocalTxtFilePath().concat(context.getTxtFileName());
//        StringBuilder head;
//        int totalLines = MyFileUtil.getTotalLines(new File(txtFilePathAndName));
//        if(totalLines==0){
//            errorMessage.append("文件内容为空");
//            fileCheckService.errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
//            return false;
//        }
//        head=MyFileUtil.gethead(txtFilePathAndName);
//
//        if(!SftpToDbUtils.checkHead(context,head.toString())){
//            log.error("文件表头异常-文件名-{}，head-{}",context.getTxtFileName(),head);
//            errorMessage.append("文件表头异常");
//            fileCheckService.errorDetail(context,errorMessage.toString(),ErrorFileTypeEnum.ERROR_FILE);
//            return false;
//        }
//
//        log.info("================开始去重============");
//        int pageSize= (totalLines + SPLITSIZE-1) /SPLITSIZE;
//        File[] files = MyFileUtil.splitFileByCusNum(txtFilePathAndName,pageSize);
//        MyFileUtil.distinctByCusNum(files,context.getDistinctTxtFilePath(),context.getDistinctTxtFileName(),pageSize,head);
//        log.info("================去重结束============");
//
//        if(totalLines>1&& StringUtils.isNotEmpty(head)){
//            log.warn(" txtFileName:{} totalLines:{} head:{}",context.getTxtFileName(),totalLines,head);
//            String s = Constants.UPLOAD_DATA_NUM + context.getBatchNumber();
//            String s1 = redisChgService.get(s);
//            Integer value=StringUtils.isNotEmpty(s1)?((totalLines-1)+Integer.parseInt(s1)):(totalLines-1);
//            redisChgService.setex(s,value.toString(),172800);
//        }
//        long start = System.currentTimeMillis();
//        fileCheckService.checkSmallDataFile(context);
//        long end = System.currentTimeMillis();
//        if(log.isWarnEnabled()){
//            log.warn(String.format("数据入库时长:%d",end-start));
//        }
//        dealErrorResultFile(context);
//        log.info("parseConfigFile done");
//        String s = redisChgService.get(Constants.INSERT_DB_NUMBER + context.getTxtFileName());
//        Integer actualNumber =StringUtils.isNotEmpty(s)?Integer.parseInt(s):0 ;
//        LoadResult lr=new LoadResult();
//        lr.setApiCode(context.getTask().getApiCode());
//        lr.setFileName(context.getTxtFileName());
//        lr.setBatchNumber(context.getTask().getBatchNumber());
//        lr.setStatus("1");
//        lr.setActualNumber(actualNumber);
//        lr.setTaskNumber(totalLines-1);
//        loadResultMapper.insertLoadResult(lr);
//        return true;
//    }
//
//
//    @Override
//    public void checkConfigFile(FileContext context) {
//        MarketingTask task =context.getTask();
//        String configFilePathAndName=context.getLocalTxtFilePath().concat(context.getConfigFileName());
//            File configFile= new File(configFilePathAndName);
//            if(configFile.exists()&&configFile.isFile()){
//
//                Map<String,String> configMap = new HashedMap();
//                try(FileReader read = new FileReader(configFilePathAndName);
//                    BufferedReader br = new BufferedReader(read)){
//                    String row;
//                    while ((row = br.readLine()) != null) {
//                        String trim = row.trim();
//                        if(StringUtils.isNotEmpty(trim)){
//                            String[] split = trim.split("=");
//                            if(split.length>=2){
//                                configMap.put(split[0],split[1]);
//                            }
//                        }
//                    }
//                    String dataVolume=configMap.get("dataVolume");
//                    if(StringUtils.isNotEmpty(dataVolume)){
//                        try{
//                            int count = Integer.parseInt(dataVolume);
//                            task.setDataVolume(count);
//                        }catch (Exception e){
//                            log.error("dataVolume error",e);
//                        }
//                    }
//                    log.warn("{}，内容为{}",context.getConfigFileName(),configMap);
//                    if(task.getMonitorType()==1){
//                        if(StringUtils.isNotEmpty(configMap.get("strategyId"))&&fileCheckService.checkConfig("strategyId", configMap.get("strategyId"), task.getApiCode(), "")){
//                            task.setStrategyId(configMap.get("strategyId"));
//                            task.setFrequency(0+"");
//                            task.setCloseDate(DateHelper.getDateAdd(2));
//                            task.setStartDate(DateHelper.getDateAdd(0));
//                        }else {
//                            task.setMonitorStatus(3);
//                            task.setStatus(1);
//                            task.setErrorMessage("配置文件异常,策略编号异常");
//                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
//                            return;
//                        }
//                    }else if(task.getMonitorType()==2||task.getMonitorType()==3||task.getMonitorType()==4){
//                        if(StringUtils.isNotEmpty(configMap.get("strategyId"))&&fileCheckService.checkConfig("strategyId", configMap.get("strategyId"), task.getApiCode(), "")){
//                            task.setStrategyId(configMap.get("strategyId"));
//                        }else {
//                            task.setMonitorStatus(3);
//                            task.setStatus(1);
//                            task.setErrorMessage("配置文件异常,策略编号异常");
//                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
//                            return;
//                        }
//                        if(StringUtils.isNotEmpty(configMap.get("monitorFrequency"))&&fileCheckService.checkConfig("monitorFrequency", configMap.get("monitorFrequency"), task.getApiCode(), "")){
//                            task.setFrequency(configMap.get("monitorFrequency"));
//                        }else {
//                            task.setMonitorStatus(3);
//                            task.setStatus(1);
//                            task.setErrorMessage("配置文件异常,监控周期异常");
//                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
//                            return;
//                        }
//                        if(StringUtils.isNotEmpty(configMap.get("monitorStartTime"))&&fileCheckService.checkConfig("monitorStartTime", configMap.get("monitorStartTime"), task.getApiCode(), "")){
//                            task.setStartDate(configMap.get("monitorStartTime"));
//                        }else {
//                            task.setMonitorStatus(3);
//                            task.setStatus(1);
//                            task.setErrorMessage("配置文件异常,监控开始日期异常");
//                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
//                            return;
//                        }
//                        if(StringUtils.isNotEmpty(configMap.get("monitorStartTime"))&&fileCheckService.checkConfig("monitorendTime", configMap.get("monitorendTime"), task.getApiCode(), configMap.get("monitorStartTime"))){
//                            task.setCloseDate(configMap.get("monitorStartTime"));
//                        }else {
//                            task.setMonitorStatus(3);
//                            task.setStatus(1);
//                            task.setErrorMessage("配置文件异常,监控截止日期异常");
//                            fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
//                            return;
//                        }
//                    }else{
//                        task.setMonitorStatus(3);
//                        task.setStatus(1);
//                        task.setErrorMessage("监控模式异常");
//                        fileCheckService.errorDetail(context,task.getErrorMessage(),ErrorFileTypeEnum.ERROR_CONFIG);
//                        return;
//                    }
//
//                } catch (FileNotFoundException e) {
//                    log.error("FileNotFoundException",e);
//                } catch (IOException e) {
//                    log.error("IOException",e);
//                }
//                LoadResult lr=new LoadResult();
//                lr.setApiCode(task.getApiCode());
//                lr.setFileName(context.getConfigFileName());
//                lr.setBatchNumber(task.getBatchNumber());
//                lr.setStatus("1");
//                loadResultMapper.insertLoadResult(lr);
//                task.setMonitorStatus(1);
//            }
//        task.setStatus(1);
//    }
//
//
//
//    /**
//     * 处理三要素校验失败的内容
//     * @param context 参数对象
//     */
//    private void dealErrorResultFile(FileContext context) {
//        /**
//         * 处理解密校验失败的三要素
//         */
//        String errorFilePathAndName=context.getErrorFilePath().concat(context.getErrorDataFileName());
//        SftpClient client=(SftpClient)context.getBaseFtpClient();
//        int erroNUm = MyFileUtil.getTotalLines(new File(errorFilePathAndName));
//        if(erroNUm>1){
//            File errorresultFile=new File(errorFilePathAndName);
//            if(errorresultFile.isFile()){
//                try {
//                    String sftpInErrorPath=Constants.SFTP_IN_ERROR_PATH.replace("apiCode",context.getTask().getApiCode());
//                    client.uploadFile(sftpInErrorPath,context.getErrorDataFileName(),errorFilePathAndName);
//                    File successFile=new File(errorFilePathAndName+".success");
//                    successFile.createNewFile();
//                    if(successFile.exists()){
//                        client.uploadFile(sftpInErrorPath,context.getErrorDataFileName()+".success",errorFilePathAndName+".success");
//                    }
//                } catch (Exception e) {
//                    log.error("上传错误文件到sftp出错",e);
//                }
//            }
//        }
//    }
//}
