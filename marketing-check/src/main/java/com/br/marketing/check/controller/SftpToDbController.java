package com.br.marketing.check.controller;

import com.br.marketing.check.service.SftpToDbService;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.check.utils.UploadDataFileUtil;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.common.utils.RabbitMqSenderUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.Impl.ValidDataAlarmServiceImpl;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Author: Bairong
 * @Time: 2020/12/9 13:31
 * @Company：百融
 * @Description: 功能描述
 */

@RestController
@RequestMapping("/sftpToDb/")
@Slf4j
public class SftpToDbController {
    @Value("${otherConfig.warning.path:00}")
    private String warningPath;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    @Resource
    SftpToDbService sftpToDbServiceImpl;
    @Resource
    LoadResultMapper loadResultMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    RedisChgService redisChgService;
    @Resource
    MarketingUserMapper marketingUserMapper;
    @Resource(name = "rabbitTemplate")
    private RabbitTemplate rabbitTemplate;
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private static final Pattern MYREGEX1 = Pattern.compile("_");
    @Resource
    ValidDataAlarmServiceImpl validDataAlarmService;
    @GetMapping("deleteMonitor")
    public String deleteMonitor(){
        Map<String, Set<String>> map=new HashMap<>();
        SftpClient sftpClient = new SftpClient(sftpHost,sftpPort,sftpUsername,sftpPwd);
        try {
            boolean connect = sftpClient.connect();
            if(connect){
                log.info("======登录成功===开始剔除文件处理======");
            }else{
                log.info("======登录失败=========");
                return "success";
            }
            SftpToDbUtils.listFtpFile("/UploadFiles/loanwarn/",map,true,sftpClient);
            if(!map.isEmpty()){
                log.info("----------SftpToDb开始处理新上传的剔除文件-------------");
                dealDeleteMonitorFile(map,sftpClient);
            }
        } catch (Exception e) {
            log.error("获取ftp上的剔除文件列表出错",e);
        }finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error("断开sftp连接出错",e);
            }
        }
        return "success";
    }

    /**
     * 处理新上传的剔除监控的文件
     * @param map 存储新上传的剔除监控的文件路径和名称
     * @param sftpClient
     */
    private void dealDeleteMonitorFile(Map<String, Set<String>> map,  SftpClient sftpClient) {
        log.info("dealDeleteMonitorFile:{}",map);
        for(Map.Entry<String,Set<String>> entry:map.entrySet()){
            String key = entry.getKey();
            Set<String> value = entry.getValue();
            MerchantParam merchantParam = SftpToDbUtils.vaildApicode(key);
            if(merchantParam==null){
                log.error("vaildApicode error {}",key);
                continue;
            }
            String apiCode = merchantParam.getApiCode();
            StringBuilder localFile=new StringBuilder(warningPath)
                    .append("delete")
                    .append("/")
                    .append(apiCode)
                    .append("/")
                    .append(DateHelper.getDateAddYyMmDd(0)).append("/");
            if(StringUtils.isNotEmpty(apiCode)&&(apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA))){
                List<String> finishList =UploadDataFileUtil.isFinish(value);
                for(String finishName:finishList){
                    log.debug("finishName:{}",finishName);
                    for(String fileName:value){
                        if(fileName.endsWith(".zip")){
                            String s1 = MYREGEX.split(fileName)[0];
                            if(s1.length()<2){
                                log.warn("fileName is error{}",fileName);
                                continue;
                            }
                            String[] s = MYREGEX1.split(s1);
                            if(s.length<5){
                                log.warn("fileName is error{}",fileName);
                                continue;
                            }
                            String  name=s[0]+"_"+s[1]+"_"+s[3]+"_"+s[4];
                            log.debug("fileName:{},name:{}",fileName,name);
                            if(finishName.equals(name)){
                                StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
                                if(SftpToDbUtils.vaildFileName(fileName, apiCode,errorMessage,true)){
                                    sftpToDbServiceImpl.parsingDeleteFile(key,fileName,localFile.toString(),apiCode,merchantParam,finishName,sftpClient);
                                }else{
                                    SftpToDbUtils.returnDeleteErrorFile(apiCode, localFile.toString(), fileName, errorMessage,sftpClient);
                                    LoadResult lr=new LoadResult(apiCode,finishName,fileName,errorMessage.toString(),"0","",0,0,"delete");
                                    loadResultMapper.insertLoadResult(lr);
                                }
                            }
                        }
                    }
                    try {
                        validDataAlarmService.deleteMonitorFileUpload(apiCode,finishName);
                        String sftpPath = "/UploadFiles/loanwarn/" + apiCode + "/input/";
                        sftpClient.rename(sftpPath+finishName+".finish",sftpPath+finishName+".finish"+".bak");
                    } catch (Exception e) {
                        log.error("rename finish error ",e);
                    }
                }
            }else{
                for(String fileName:value){
                    if(fileName.endsWith(".zip")){
                        String successFile=fileName+".success";
                        if(value.contains(successFile)){
                            String[] split = MYREGEX.split(fileName);
                            String zipName = split[0];
                            StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
                            if(SftpToDbUtils.vaildFileName(fileName, apiCode,errorMessage,true)){
                                sftpToDbServiceImpl.parsingDeleteFile(key,fileName,localFile.toString(),apiCode,merchantParam,zipName,sftpClient);
                            }else{
                                SftpToDbUtils.returnDeleteErrorFile(apiCode, localFile.toString(), fileName, errorMessage,sftpClient);
                                LoadResult lr=new LoadResult(apiCode,zipName,fileName,errorMessage.toString(),"0","",0,0,"delete");
                                loadResultMapper.insertLoadResult(lr);
                            }
                            validDataAlarmService.deleteMonitorFileUpload(apiCode,zipName);
                            String sftpPath = "/UploadFiles/loanwarn/" + apiCode + "/input/";
                            try {
                                sftpClient.rename(sftpPath+fileName+".success",sftpPath+fileName+".success.bak");
                            } catch (SftpException e) {
                                log.error("rename success error ",e);
                            }
                        }
                    }
                }
            }
        }
    }


    @GetMapping("monitorData")
    public String monitorData(){
        Map<String, Set<String>> map=new HashMap<>();
        SftpClient sftpClient = new SftpClient(sftpHost,sftpPort,sftpUsername,sftpPwd);
        try {
            boolean connect = sftpClient.connect();
            if(connect){
                log.info("======登录成功===开始数据文件处理======");
            }else{
                log.info("======登录失败=========");
                return "success";
            }
            SftpToDbUtils.listFtpFile("/UploadFiles/loanwarn/",map,false,sftpClient);
            if(!map.isEmpty()){
                log.info("----------SftpToDb开始处理新上传的数据文件-------------");
                dealDataFile(map,sftpClient);
            }
        } catch (Exception e) {
            log.error("获取ftp上的剔除文件列表出错",e);
        }finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error("断开sftp连接出错",e);
            }
        }
        return "success";
    }


    /**
     * 开始处理新上传的文件
     * @param map key ftp上的路径loanwarn/4200333/input
     *            value 对应目录下新上传的文件
     * @param sftpClient
     */
    private void dealDataFile(Map<String, Set<String>> map, SftpClient sftpClient) {
        for(Map.Entry<String,Set<String>> entry:map.entrySet()){
            String key = entry.getKey();
            Set<String> value = entry.getValue();
            MerchantParam merchantParam = SftpToDbUtils.vaildApicode(key);
            if(merchantParam==null){
                log.error("vaildApicode error {}",key);
                continue;
            }
            String callMethod = merchantParam.getCallMethod();
            int monitorType;
            if("1".equals(callMethod)){
                monitorType=3;
            }else if("3".equals(callMethod)){
                monitorType=1;
            }else{
                monitorType=Integer.parseInt(callMethod);
            }

            String apiCode = merchantParam.getApiCode();
            String tableName="b_marketing_user_"+apiCode;
            marketingUserMapper.createUserTable(tableName);
            StringBuilder localFile=new StringBuilder(warningPath)
                    .append("ftp_data")
                    .append("/")
                    .append(apiCode)
                    .append("/");

            /**
             * 360定制逻辑：
             * 从新上传的文件列表中找出finish文件的列表
             * 循环finish文件列表，找出对应的数据文件，按finish文件一个批次一个批次处理
             */
            if(StringUtils.isNotEmpty(apiCode)&&(apiCode.equals(Constants.APICODE_360)||apiCode.equals(Constants.APICODE_360_QA))){
                List<String> finishList = UploadDataFileUtil.isFinish(value);
                for(String finishName:finishList){
                    log.info("finishName:{}",finishName);
                    String batchNumber=UploadDataFileUtil.getBatchNumber(apiCode);
                    MarketingTask lt =new MarketingTask();
                    lt.setApiCode(apiCode);
                    lt.setBatchNumber(batchNumber);
                    lt.setCusBatch(finishName.split("_")[1]);
                    lt.setFileName(finishName);
                    lt.setMonitorType(monitorType);
                    lt.setStatus(2);
                    marketingTaskMapper.insertTask(lt);
                    for(String fileName:value){
                        if(fileName.endsWith(".zip")){
                            String s1 = MYREGEX.split(fileName)[0];
                            String[] s = MYREGEX1.split(s1);
                            if(s.length<4){
                                log.warn("filename:{}",fileName);
                                continue;
                            }
                            String  name=s[0]+"_"+s[1]+"_"+s[3];
                            String  name1=s[0]+"_UploadCustomFileName"+s[3]+"_"+s[3];
                            log.debug("fileName:{},name:{}",fileName,name);
                            if(finishName.equals(name)||finishName.equals(name1)){
                                StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
                                if(UploadDataFileUtil.vaildFileName(fileName, apiCode,errorMessage,false)){
                                    sftpToDbServiceImpl.parsingFile(key,fileName,localFile.toString(),apiCode,merchantParam,
                                            finishName,batchNumber,lt,sftpClient);
                                }else{
                                    SftpToDbUtils.returnErrorFile(apiCode, localFile.toString(), fileName, errorMessage,sftpClient);
                                    LoadResult lr=new LoadResult(apiCode,finishName,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
                                    loadResultMapper.insertLoadResult(lr);
                                }
                                String path = "/UploadFiles/loanwarn/" + apiCode + "/input/";
                                try {
                                    sftpClient.rename(path+fileName,path+fileName+".bak");
                                } catch (Exception e) {
                                    log.warn("rename zip error ",e);
                                    try {
                                        sftpClient.disconnect();
                                        sftpClient.connect();
                                        sftpClient.rename(path+fileName,path+fileName+".bak");
                                    } catch (Exception ex) {
                                        log.error("rename zip error ",ex);
                                    }
                                }
                            }
                        }
                    }
                    String taskNumber = redisChgService.get(Constants.UPLOAD_DATA_NUM + batchNumber);
                    String failNumber = redisChgService.get(Constants.UPLOAD_FAILDATA_NUM + batchNumber);
                    lt.setTableName("b_marketing_user_"+apiCode);
                    Integer actualNumber = marketingUserMapper.queryCount(lt);
                    log.info("taskNumber:{},FailNumber:{}, actualNumber:{}",taskNumber,failNumber,actualNumber);
                    lt.setTaskNumber(StringUtils.isNotEmpty(taskNumber)?Integer.parseInt(taskNumber):0);
                    lt.setActualNumber(actualNumber);
                    log.info("LoanTask:{}",lt);
                    marketingTaskMapper.modifyTask(lt);
                    validDataAlarmService.fileUpload(apiCode,batchNumber);
                    String path = "/UploadFiles/loanwarn/" + apiCode + "/input/";
                    try {
                        sftpClient.rename(path+finishName+".finish",path+finishName+".finish"+".bak");
                    } catch (Exception e) {
                        log.warn("rename finish error ",e);
                        try {
                            sftpClient.disconnect();
                            sftpClient.connect();
                            sftpClient.rename(path+finishName+".finish",path+finishName+".finish"+".bak");
                        } catch (Exception ex) {
                            log.error("rename finish error ",ex);
                        }
                    }
                }
            }else{
                for(String fileName:value){
                    if(fileName.endsWith(".zip")){
                        String successFile=fileName+".success";
                        if(value.contains(successFile)){
                            String batchNumber=UploadDataFileUtil.getBatchNumber(apiCode);
                            MarketingTask lt =new MarketingTask();
                            lt.setApiCode(apiCode);
                            lt.setBatchNumber(batchNumber);
                            lt.setMonitorType(monitorType);
                            lt.setMonitorStatus(0);
                            lt.setStatus(2);
                            lt.setFileName(MYREGEX.split(fileName)[0]);
                            marketingTaskMapper.insertTask(lt);
                            String[] split = MYREGEX.split(fileName);
                            String zipName = split[0];
                            StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
                            if(UploadDataFileUtil.vaildFileName(fileName, apiCode,errorMessage,false)){
                                sftpToDbServiceImpl.parsingFile(key,fileName,localFile.toString(),
                                        apiCode,merchantParam,zipName,batchNumber,lt,sftpClient);
                            }else{
                                SftpToDbUtils.returnErrorFile(apiCode, localFile.toString(), fileName, errorMessage,sftpClient);
                                LoadResult lr=new LoadResult(apiCode,zipName,fileName,errorMessage.toString(),"0",batchNumber,0,0,"");
                                loadResultMapper.insertLoadResult(lr);
                            }
                            String taskNumber = redisChgService.get(Constants.UPLOAD_DATA_NUM + batchNumber);
                            String failNumber = redisChgService.get(Constants.UPLOAD_FAILDATA_NUM + batchNumber);
                            lt.setTableName("b_marketing_user_"+apiCode);
                            Integer actualNumber = marketingUserMapper.queryCount(lt);
                            log.info("taskNumber:{},FailNumber:{}, actualNumber:{}",taskNumber,failNumber,actualNumber);
                            lt.setTaskNumber(StringUtils.isNotEmpty(taskNumber)?Integer.parseInt(taskNumber):0);
                            lt.setActualNumber(actualNumber);
                            log.info("LoanTask:{}",lt);
                            marketingTaskMapper.modifyTask(lt);
                            validDataAlarmService.fileUpload(apiCode,batchNumber);
                            String path = "/UploadFiles/loanwarn/" + apiCode + "/input/";
                            try {
                                sftpClient.rename(path+successFile,path+successFile+".bak");
                                sftpClient.rename(path+fileName,path+fileName+".bak");
                            } catch (Exception e) {
                                log.warn("rename file error ",e);
                                try {
                                    sftpClient.disconnect();
                                    sftpClient.connect();
                                    sftpClient.rename(path+successFile,path+successFile+".bak");
                                    sftpClient.rename(path+fileName,path+fileName+".bak");
                                } catch (Exception ex) {
                                    log.error("rename file error ",ex);
                                }
                            }
                        }
                    }
                }
            }

            if(Constants.APICODE_SHAZI.contains(apiCode)){
                RabbitMqSenderUtils.convertAndSendPriority(rabbitTemplate,MQConstants.exchangerName, MQConstants.taskRoutingKey,apiCode);
            }
        }
    }
}
