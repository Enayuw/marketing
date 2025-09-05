package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据合法校验异常文件上传失败预警&文件上传成功通知
 * Created by Bairong on 2020/7/11.
 */
@Service
@Slf4j
public  class ValidDataAlarmServiceImpl implements EmailService {
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    LoadResultMapper loadResultMapper;

    @Override
    public void deleteMonitorFileUpload(String apiCode, String message) {
        log.info("apiCode:{}, message:{}", apiCode, message);
        String compShortName= "";
        String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
        log.info("companyMsg:{}", companyMsg);
        if(StringUtils.isNotEmpty(companyMsg)){
            JSONObject companyJSONObj = JSON.parseObject(companyMsg);
            compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
        }
        boolean flag = false;

        String alarmDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Map<String,String> param = new HashMap<>();
        param.put("apiCode", apiCode);
        param.put("cusBatch", message);
        param.put("status","0");
        List<LoadResult> list = loadResultMapper.queryLoadResult(param);
        log.info("flag:{}, size:{}", flag, list.size());
        if(!list.isEmpty()){
            StringBuilder content = new StringBuilder();
            content.append("<br/>您好：【")
                    .append(compShortName)
                    .append("-")
                    .append(apiCode)
                    .append("】剔除监控名单-文件校验失败，请及时跟进<br/>")
                    .append("&nbsp;&nbsp;<br/>");
            for(LoadResult lr:list) {
                content.append("&nbsp;&nbsp;&nbsp;文件名称：")
                        .append(lr.getFileName())
                        .append(" &nbsp;校验时间：")
                        .append(alarmDate)
                        .append(" &nbsp;失败原因：")
                        .append(lr.getMessage())
                        .append("<br/>");
            }
            String title="【紧急报警】【"+compShortName+"-"+apiCode+"】智能营销平台-文件校验失败";
            alarmClient.sendAlarm(content.toString(),title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
        } else {
            param.put("status","1");
            List<LoadResult> successList=loadResultMapper.queryLoadResult(param);
            if(successList != null && successList.size() > 0){
                StringBuilder content = new StringBuilder();
                content.append("<br/>您好：【")
                        .append(compShortName)
                        .append(apiCode)
                        .append("】剔除监控名单-剔除成功<br/>")
                        .append("&nbsp;&nbsp;<br/>");
                for (LoadResult lr: successList){
                    if(lr.getFileName().endsWith(".zip")){
                        content.append("&nbsp;&nbsp;&nbsp;文件名称：")
                                .append(lr.getFileName())
                                .append(" &nbsp;完成时间：")
                                .append(alarmDate)
                                .append(" &nbsp;上传数据量：")
                                .append(lr.getTaskNumber())
                                .append("&nbsp;入库数据量：")
                                .append(lr.getActualNumber())
                                .append("&nbsp;异常数据量：")
                                .append((lr.getTaskNumber()-lr.getActualNumber()))
                                .append("<br/>");
                    }else if(lr.getFileName().endsWith(".bean")){
                        content.append("&nbsp;&nbsp;&nbsp;文件名称：")
                                .append(lr.getFileName())
                                .append(" &nbsp;上传时间：")
                                .append(alarmDate)
                                .append("<br/>");
                    }
                }
                String title="【上传通知】【"+compShortName+"-"+apiCode+"】智能营销平台-文件上传结果通知";
                alarmClient.sendAlarm(content.toString(),title, AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
            }

        }
    }

    @Override
    public void hxResultErrorAlarm(String title, String message) {
    }

    @Override
    public void zipFileErrorAlarm(String fileName, String title) {
    }

    @Override
    public void closeDateAlarm() {
    }

    @Override
    public void monitoringExpirationAlarm() {
    }

    public void fileUpload(String apiCode, String message){
        log.info("apiCode:{}, message:{}", apiCode, message);
        String compShortName = "";
        String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
        log.info("companyMsg:{}", companyMsg);
        if(StringUtils.isNotEmpty(companyMsg)){
            JSONObject companyJSONObj = JSON.parseObject(companyMsg);
            compShortName = companyJSONObj.getString("COMP_SHORT_NAME");
        }
        boolean flag = false;
        MarketingTask marketingTask = marketingTaskMapper.queryBlt(message);

        String alarmDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Map<String,String> param = new HashMap<>();
        param.put("apiCode", apiCode);
        param.put("batchNumber", message);
        param.put("status", "0");
        List<LoadResult> list = loadResultMapper.queryLoadResult(param);
        log.info("flag: {}, size:{}", flag, list);
        if(!flag&&list != null && !list.isEmpty()){
                StringBuilder content = new StringBuilder();
                content.append("<br/>您好：【")
                        .append(compShortName)
                        .append("-")
                        .append(apiCode)
                        .append("】文件校验失败，请及时跟进<br/>")
                       .append("&nbsp;&nbsp;<br/>");
                for (LoadResult lr: list){
                    content.append("&nbsp;&nbsp;&nbsp;文件名称：")
                            .append(lr.getFileName())
                            .append(" &nbsp;校验时间：")
                            .append(alarmDate)
                            .append(" &nbsp;失败原因：")
                            .append(lr.getMessage())
                            .append("<br/>");
                }
                String title="【紧急报警】【" + compShortName + "-" + apiCode + "】智能营销平台-文件校验失败";
                alarmClient.sendAlarm(content.toString(), title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
        }
        param.put("status", "1");
        List<LoadResult> successList = loadResultMapper.queryLoadResult(param);
        if(successList != null && !successList.isEmpty()){
            StringBuilder content = new StringBuilder();
            content.append("<br/>您好：【")
                    .append(compShortName)
                    .append("-")
                    .append(apiCode)
                    .append("】上传文件成功<br/>")
                   .append("&nbsp;&nbsp;<br/>");
            for (LoadResult lr:successList){
                if(lr.getFileName().endsWith(".txt")) {
                    content.append("&nbsp;&nbsp;&nbsp;文件名称：")
                            .append(lr.getFileName())
                            .append(" &nbsp;上传时间：")
                            .append(alarmDate)
                            .append(" &nbsp;上传数据量：")
                            .append(lr.getTaskNumber())
                            .append("&nbsp;入库数据量：")
                            .append(lr.getActualNumber())
                            .append("&nbsp;异常数据量：")
                            .append((lr.getTaskNumber()-lr.getActualNumber()))
                            .append("<br/>");
                } else if(lr.getFileName().endsWith(".config")){
                    content.append("&nbsp;&nbsp;&nbsp;文件名称：")
                            .append(lr.getFileName())
                            .append(" &nbsp;上传时间：")
                            .append(alarmDate)
                            .append("<br/>");
                }
            }

            String monitorType = "";
            switch (marketingTask.getMonitorType()){
                case 1:
                    monitorType = "一次性";
                    break;
                case 2:
                    monitorType = "首次全量,再定期变动";
                    break;
                case 3:
                    monitorType = "定期变动";
                    break;
                case 4:
                    monitorType = "全量定期查询";
                    break;
                default:
                    break;

            }
            String monitorStatus = "";
            switch (marketingTask.getMonitorStatus()){
                case 1:
                    monitorStatus = "监控中";
                    break;
                case 2:
                    monitorStatus = "停止监控";
                    break;
                case 3:
                    monitorStatus = "监控任务配置异常";
                    break;
                case 4:
                    monitorStatus = "监控到期";
                    break;
                case 0:
                    monitorStatus = "监控任务待配置";
                    break;
                default:
                    break;
            }
            StringBuilder sb1 = new StringBuilder();
            sb1.append(content);
            sb1.append("&nbsp;&nbsp;&nbsp;任务信息：")
                    .append(" &nbsp;批次号：")
                    .append(marketingTask.getBatchNumber() == null ? "": marketingTask.getBatchNumber())
                    .append(" &nbsp;策略编号：")
                    .append(marketingTask.getStrategyId() == null ? "": marketingTask.getStrategyId())
                    .append("&nbsp;监控模式：")
                    .append(monitorType)
                    .append("&nbsp;监控开始时间：")
                    .append(marketingTask.getStartDate() == null ? "": marketingTask.getStartDate())
                    .append("&nbsp;监控截止时间：")
                    .append(marketingTask.getCloseDate() == null ? "": marketingTask.getCloseDate())
                    .append("&nbsp;监控状态：")
                    .append(monitorStatus);
            sb1.append("<br/>");
            log.warn("任务信息：{}", sb1);
            content.append("<br/>");
            String title = "【上传通知】【" + compShortName + "-" + apiCode+"】智能营销平台-文件上传结果通知";
            alarmClient.sendAlarm(content.toString(), title, AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        }
    }

    @Override
    public void sendAlarm(String context, String type) {
    }

    @Override
    public void sendReport(String apiCode) {
    }

    @Override
    public void resultVolumeCheck(String apiCode) {
    }

    @Override
    public void resultVolumeCheck(StraHisFile file) {
    }

    @Override
    public void ftpToSftpCheck(String apiCode) {
    }

    @Override
    public void fileSizeException(String apiCode, String message) {
    }

    @Override
    public void fileUploadFtpException(String apiCode, String message) {
    }

    @Override
    public void report() {
    }

    public void dataFileVolumn(String apiCode, String message){
        log.warn("apiCode {}, message {}", apiCode, message);
        String compShortName = "";
        String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
        if(StringUtils.isNotEmpty(companyMsg)){
            JSONObject companyJSONObj = JSON.parseObject(companyMsg);
            compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
        }
        if(StringUtils.isNotEmpty(message) && message.split(",").length == 3) {
            String fileName = message.split(",")[0];
            String dataVolume = message.split(",")[1];
            String taskNumber = message.split(",")[2];
            StringBuilder content = new StringBuilder();
            content.append("<br/>您好：【")
                   .append(compShortName)
                   .append("-")
                   .append(apiCode)
                   .append("】上传数据文件数据量与配置文件中的数据量有差异<br/>")
                   .append("&nbsp;&nbsp;<br/>")
                   .append("&nbsp;&nbsp;&nbsp;文件名称：")
                   .append(fileName)
                   .append(" <br/>")
                   .append("&nbsp;&nbsp;&nbsp;上传数据文件数据量：")
                   .append(taskNumber)
                   .append(" <br/>")
                   .append("&nbsp;&nbsp;&nbsp;配置文件中的数据量：")
                   .append(dataVolume)
                   .append(" <br/>");
            String title = "【紧急预警】【" + compShortName + "-" + apiCode + "】智能营销平台-上传数据文件数据量异常";
            alarmClient.sendAlarm(content.toString(), title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
        }else{
            log.error("dataFileVolumn参数错误:{}",message);
        }
    }

    @Override
    public void progressReport() {

    }
}