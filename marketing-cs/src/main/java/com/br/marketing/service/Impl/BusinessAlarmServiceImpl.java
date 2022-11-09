package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ApiCodeTask;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 业务流程异常预警
 * Created by Bairong on 2020/7/11.
 */
@Service
@Slf4j
public  class BusinessAlarmServiceImpl implements EmailService {
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    StraHisFileMapper straHisFileMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    private final static Integer SIZE=2000;


    @Override
    public void sendAlarm(String context, String type) {

    }

    @Override
    public void sendReport(String apiCode) {

    }

    public void resultVolumeCheck(String apiCode){
        log.warn("resultVolumeCheck  apiCode--{}",apiCode);
        List<LoanFile> blrList = loanFileMapper.queryResultByApiCode(apiCode);
        int expectedFileNum=blrList.size();
        int actualFileNum=0;
        Map<String,BigDecimal> map;
        for(LoanFile blf:blrList){
            if( blf.getFileNum()!=null){
                actualFileNum=actualFileNum+blf.getFileNum();
            }
        }
        map=loanFileMapper.queryTotalDataNum(apiCode);
        if(map!=null){
            BigDecimal expecteData=map.get("expecteDataNum");
            BigDecimal actualData=map.get("actualDataNum");
            int expecteDataNum =0;
            int actualDataNum=0;
            if(expecteData!=null){
                expecteDataNum=Integer.parseInt(expecteData.toString());
            }
            if(expecteData!=null){
                actualDataNum=Integer.parseInt(actualData.toString());
            }
            StringBuilder content = new StringBuilder();
            if(expectedFileNum!=actualFileNum||expecteDataNum!=actualDataNum){
                String alarmDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String compShortName="";
                String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
                if(StringUtils.isNotEmpty(companyMsg)){
                    JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                    compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
                }

                content.append("&nbsp;&nbsp;&nbsp;您好:  【").append(compShortName).append("】结果文件数据量级异常，触发报警，请及时跟进：<br/>");
                content.append("&nbsp;&nbsp;&nbsp;校验时间：").append(alarmDate).append("<br/>");
                if(expectedFileNum!=actualFileNum){
                    /**
                     * 如果应返回的结果文件个数和实际返回的结果文件个数不一致
                     * 修改标识文件上传状态为2，表示暂时不能上传finish文件
                     */
                    int i = expectedFileNum - actualFileNum;
                    loanFileMapper.updateSignFileStatus(apiCode);
                    content.append("&nbsp;&nbsp;&nbsp;应返回结果文件数量：")
                            .append(expectedFileNum)
                            .append(",实际返回结果文件数量：")
                            .append(actualFileNum)
                            .append(",结果文件差异量：<font color=\"red\">").
                            append(i)
                            .append("</font><br/>");
                }else{
                    content.append("&nbsp;&nbsp;&nbsp;应返回结果文件数量：")
                            .append(expectedFileNum)
                            .append(",实际返回结果文件数量：")
                            .append(actualFileNum)
                            .append(",结果文件差异量：0 <br/>");
                }
                if(expecteData!=null&&actualData!=null){
                    if(expecteDataNum!=actualDataNum){
                        /**
                         * 如果应返回的总数据量与实际返回的总数据量差值大于20，
                         * 修改标识文件上传状态为2，表示暂时不能上传finish文件
                         */
                        if(expecteDataNum-actualDataNum>20){
                            loanFileMapper.updateSignFileStatus(apiCode);
                        }
                        int i = expecteDataNum - actualDataNum;
                        content.append("&nbsp;&nbsp;&nbsp;应返回数据总量：")
                                .append(expecteDataNum)
                                .append(",实际返回数据量：")
                                .append(actualDataNum)
                                .append(",数据量条数差异：<font color=\"red\">")
                                .append(i)
                                .append("</font><br/>");
                    }
                }else {
                    log.error("数据量比较出错：expecteDataNum:{}，actualDataNum:{}",expecteDataNum,actualDataNum);
                }

                String title="【紧急报警】【"+compShortName+"-"+apiCode+"】智能营销平台-数据差异报警";
                alarmClient.sendAlarm(content.toString(),title, AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());

            }
        }
    }

    @Override
    public void resultVolumeCheck(StraHisFile file){
        String apiCode = file.getApiCode();
        log.warn("resultVolumeCheck  batchnumber--{}",file.getBatchNumber());
        Integer expecteDataNum=file.getExpectedNum();
        Integer actualDataNum=file.getActualNum();
        StringBuilder content = new StringBuilder();
        if(!expecteDataNum.equals(actualDataNum)){
            String alarmDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String compShortName="";
            String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
            if(StringUtils.isNotEmpty(companyMsg)){
                JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
            }

            content.append("&nbsp;&nbsp;&nbsp;您好:  【").append(compShortName)
                    .append("】结果文件【").append(file.getZipfileName())
                    .append("】数据量级异常，触发报警，请及时跟进：<br/>");
            content.append("&nbsp;&nbsp;&nbsp;校验时间：").append(alarmDate).append("<br/>");
            /**
             * 如果应返回的总数据量与实际返回的总数据量差值大于20，
             * 修改标识文件上传状态为2，表示暂时不能上传finish文件
             */
            if(expecteDataNum-actualDataNum>20){
                StraHisFile upFile = new StraHisFile();
                upFile.setId(file.getId());
                upFile.setSignFileStatus(2);
                file.setSignFileStatus(2);
                straHisFileMapper.updateByPrimaryKeySelective(upFile);
            }
            int i = expecteDataNum - actualDataNum;
            content.append("&nbsp;&nbsp;&nbsp;应返回数据总量：")
                    .append(expecteDataNum)
                    .append(",实际返回数据量：")
                    .append(actualDataNum)
                    .append(",数据量条数差异：<font color=\"red\">")
                    .append(i)
                    .append("</font><br/>");
            String title="【紧急报警】【"+compShortName+"-"+apiCode+"】智能营销平台-数据差异报警";
            alarmClient.sendAlarm(content.toString(),title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            }
    }

    public void fileUploadFtpException(String apiCode,String message){
        String compShortName="";
        String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
        if(StringUtils.isNotEmpty(companyMsg)){
            JSONObject companyJSONObj = JSON.parseObject(companyMsg);
            compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
        }
        if(StringUtils.isNotEmpty(message)&&message.split(",").length==3){
            String fileName = message.split(",")[0];
            String localSize = message.split(",")[1];
            String ftpSize = message.split(",")[2];
            StringBuilder content = new StringBuilder();
            content.append("<br/>您好：【")
                    .append(compShortName)
                    .append("-")
                    .append(apiCode)
                    .append("】结果文件上传到内部FTP出现异常，触发报警，请及时跟进<br/>")
                    .append("&nbsp;&nbsp;<br/>")
                    .append("&nbsp;&nbsp;&nbsp;文件名称：")
                    .append(fileName).append(" <br/>")
                    .append("&nbsp;&nbsp;&nbsp;本地文件大小：")
                    .append(localSize).append("B <br/>").append("&nbsp;&nbsp;&nbsp;ftp上文件大小：").append(ftpSize).append("B <br/>");
            String title="【紧急报警】【"+compShortName+"-"+apiCode+"】智能营销平台-文件上传FTP异常报警";
            alarmClient.sendAlarm(content.toString(),title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
        }else{
            log.error("参数错误:{}",message);
        }
    }

    @Override
    public void fileUpload(String apiCode, String message) {

    }

    @Override
    public void report() {

    }


    @Override
    public void dataFileVolumn(String apiCode, String message) {

    }

    @Override
    public void progressReport() {

    }

    @Override
    public void deleteMonitorFileUpload(String apiCode, String message) {

    }

    @Override
    public void hxResultErrorAlarm(String title, String message) {

    }

    @Override
    public void zipFileErrorAlarm(String fileName,String apiCode) {
        String compShortName="";
        String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
        if(StringUtils.isNotEmpty(companyMsg)){
            JSONObject companyJSONObj = JSON.parseObject(companyMsg);
            compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
        }
        String alarmDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        StringBuilder content = new StringBuilder();
        content.append("&nbsp;&nbsp;&nbsp;您好:  【")
                .append(compShortName).append("】在")
                .append(alarmDate)
                .append("智能营销平台-源文件与压缩文件的大小不一致，触发报警，请及时跟进处理<br/><br/>")
                .append(fileName);
        String title="【紧急报警】【"+compShortName+"-"+apiCode+"】智能营销平台-源文件与压缩文件大小不一致";
        alarmClient.sendAlarm(content.toString(),title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
    }

    @Override
    public void closeDateAlarm() {
        String dateAdd = DateHelper.getDateAdd(0);
        List<ApiCodeTask> list = marketingTaskMapper.queryCloseBlt(dateAdd);
        log.info("list:{}", list);
        for (ApiCodeTask alt : list) {
            List<MarketingTask> marketingTaskList = alt.getMarketingTaskList();
            String apiCode = alt.getApiCode();
            String compShortName = "";
            if (marketingTaskList.size() > 0) {
                String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
                if (StringUtils.isNotEmpty(companyMsg)) {
                    JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                    compShortName = companyJSONObj.getString("COMP_SHORT_NAME");
                }
                StringBuilder content = new StringBuilder()
                        .append("&nbsp;&nbsp;&nbsp;您好:  【")
                        .append(compShortName)
                        .append("】智能营销平台-监控时间今日到期，请及时跟进：<br/><br/>")
                        .append("&nbsp;&nbsp;&nbsp;监控时间今日到期批次数：")
                        .append(marketingTaskList.size())
                        .append("<br/>")
                        .append("&nbsp;&nbsp;&nbsp;监控截止日期：[")
                        .append(dateAdd)
                        .append("]")
                        .append("<br/><br/>")
                        .append("备注：具体的今日到期的文件名称与批次编号，请联系后台研发或者产品同事进行查询获取明细");
                String title = "【紧急报警】【" + compShortName + "-" + apiCode + "】智能营销平台-监控时间今日到期";
                alarmClient.sendAlarm(content.toString(), title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());

            }
        }
    }

    @Override
    public void monitoringExpirationAlarm() {
        List<ApiCodeTask> list = marketingTaskMapper.queryCloseBltSoon(DateHelper.getDateAdd(14));
        log.info("list:{}", list);
        Set<String> set = new HashSet<>();
        for (ApiCodeTask alt : list) {
            List<MarketingTask> marketingTaskList = alt.getMarketingTaskList();
            String apiCode = alt.getApiCode();
            String compShortName = "";
            if (marketingTaskList.size() > 0) {
                for (MarketingTask lt : marketingTaskList) {
                    set.add(lt.getCloseDate());
                }
                String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
                if (StringUtils.isNotEmpty(companyMsg)) {
                    JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                    compShortName = companyJSONObj.getString("COMP_SHORT_NAME");
                }
                StringBuilder content = new StringBuilder()
                        .append("&nbsp;&nbsp;&nbsp;您好:  【")
                        .append(compShortName)
                        .append("】智能营销平台-监控时间即将到期，请及时跟进：<br/><br/>")
                        .append("&nbsp;&nbsp;&nbsp;监控时间即将到期批次数:")
                        .append(marketingTaskList.size()).append("<br/>")
                        .append("&nbsp;&nbsp;&nbsp;监控截止日期：")
                        .append(set)
                        .append("<br/><br/>")
                        .append("备注：具体的即将到期的文件名称与批次编号，请联系后台研发或者产品同事进行查询获取明细");
                String title = "【紧急报警】【" + compShortName + "-" + apiCode + "】智能营销平台-监控时间即将到期";
                alarmClient.sendAlarm(content.toString(), title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            }
            set.clear();
        }
    }


    public void ftpToSftpCheck(String apiCode){
        List<LoanFile> loanFiles = loanFileMapper.queryTodayFile(apiCode);
        if(loanFiles !=null&& loanFiles.size()>0){
            String alarmDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String compShortName="";
            String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
            if(StringUtils.isNotEmpty(companyMsg)){
                JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
            }
            StringBuilder content = new StringBuilder("<html><head></head><body><h2>文件回传同步失败通知</h2>");
            content.append("&nbsp;&nbsp;&nbsp;您好:  【")
                    .append(compShortName).append("】在")
                    .append(alarmDate)
                    .append("结果文件同步SFTP失败，触发报警，请及时跟进。<br/><br/>");
            String title="【紧急报警】【"+compShortName+"-"+apiCode+"】智能营销平台-结果文件回传失败预警";
            alarmClient.sendAlarm(content.toString(),title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
        }
    }


    public void fileSizeException(String apiCode,String message){
            String compShortName="";
            String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
            if(StringUtils.isNotEmpty(companyMsg)){
                JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
            }
            if(StringUtils.isNotEmpty(message)&&message.split(",").length==2){
                String fileName = message.split(",")[0];
                String s = message.split(",")[1];
                int size=Integer.parseInt(s)/1024;
                StringBuilder content = new StringBuilder();
                content.append("<br/>您好：【")
                        .append(compShortName)
                        .append("-")
                        .append(apiCode)
                        .append("】以下结果文件大小异常，触发报警，请及时跟进<br/>")
                  .append("&nbsp;&nbsp;<br/>")
                  .append("&nbsp;&nbsp;&nbsp;文件名称：").append(fileName).append(" <br/>")
                  .append("&nbsp;&nbsp;&nbsp;文件大小：").append(size).append("KB <br/>");
                String title="【紧急报警】【"+compShortName+"-"+apiCode+"】智能营销平台-文件大小异常报警";
                alarmClient.sendAlarm(content.toString(),title,AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            }else{
                log.error("参数错误:{}",message);
            }
    }
}
