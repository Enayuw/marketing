package com.br.marketing.service.Impl;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.SendMailClint;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.EmailService;
import com.br.marketing.service.SyncConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Bairong on 2020/7/14.
 */
@Service
@Slf4j
public  class ReportServiceImpl implements EmailService {
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Autowired
    SyncConfigService syncConfigService;
    private final static Integer SIZE=2000;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;
    @Override
    public void progressReport() {
        List<MarketingTask> list= marketingTaskMapper.queryYesterdayUploadTask();
        StringBuilder content = new StringBuilder("<html><head></head><body>");
        String compShortName="";
        if(list!=null&&list.size()>0){
            content.append("<h2>智能营销平台昨日上传任务统计</h2>")
            .append( "<table border=\"5\"  width=\"650\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:12px;\">")
            .append("<tr style=\"background-color: #428BCA; color:#ffffff\"><th>客户名称</th><th>批次号</th><th>上传时间</th><th>上传数据量</th><th>入库数据量</th></tr>");
            for (MarketingTask blt:list){
                String companyMsg = RpcClientProxy.getCompanyMsg(blt.getApiCode());
                if(StringUtils.isNotEmpty(companyMsg)){
                    JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                    compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
                }
                content.append("<tr><td>")
                        .append(compShortName).append("-")
                        .append(blt.getApiCode())
                        .append("</td>")
                        .append("<td>")
                        .append(blt.getBatchNumber())
                        .append("</td><td>")
                        .append(blt.getCreateTime())
                        .append("</td><td>")
                        .append(blt.getTaskNumber())
                        .append("</td><td>")
                        .append(blt.getActualNumber())
                        .append("</td></tr>");
            }
            content.append("</table>");
        }

        content.append("<h2>智能营销平台今日任务进度</h2>")
        .append( "<table border=\"5\"  width=\"650\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:12px;\">")
        .append("<tr style=\"background-color: #428BCA; color:#ffffff\"><th>客户名称</th><th>任务状态</th><th>任务进度</th></tr>");
        List<String> strings = marketingTaskMapper.queryApiCode();
        for(String apiCode :strings){
            String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
            if(StringUtils.isNotEmpty(companyMsg)){
                JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
            }
            List<Integer> statusList=loanFileMapper.queryTadayTaskStatus(apiCode);
            String status="";
            String progress="";
            if(statusList==null ||statusList.isEmpty()){
                status="未开始";
                progress="0%";
            }else{
                if(statusList.size()==1&&statusList.get(0)==0){
                    status="已开始,正在生成结果文件";
                    progress="30%";
                }else if(statusList.size()==1&&statusList.get(0)==1){
                    status="进行中,文件已上传至内部sftp,等待同步到客户sftp";
                    progress="60%";
                }else if(statusList.size()==1&&statusList.get(0)==2){
                    status="已结束,文件已回传至客户sftp";
                    progress="100%";
                }else if(statusList.size()==2&&statusList.contains(1)&&statusList.contains(0)){
                    status="进行中,文件正在上传至内部sftp";
                    progress="50%";
                }else if(statusList.size()==2&&statusList.contains(1)&&statusList.contains(2)){
                    status="进行中,文件正在同步至客户sftp";
                    progress="80%";
                }else{
                    log.error("任务状态异常：{},{}",apiCode,statusList);
                }
            }
            content.append("<tr><td>")
                    .append(compShortName)
                    .append("_")
                    .append(apiCode)
                    .append("</td><td>")
                    .append(status)
                    .append("</td><td>")
                    .append(progress)
                    .append("</td></tr>");
        }

        content.append("</body></html>");
        String title="智能营销平台昨日上传任务统计&当日任务进度统计";
        alarmClient.sendAlarm(content.toString(),title, AlarmSendCodeEnum.SUCCESS_INTERNAL.getCode());
    }

    @Override
    public void deleteMonitorFileUpload(String apiCode, String message) {

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
    public void fileUpload(String apiCode, String message) {

    }

    public void report() {
        List<String> apiCodes=loanFileMapper.getApiCodes();
        Map<String,List<LoanFile>> map=new HashedMap();
        Map<String,List<LoanFile>> emptyFileMap=new HashedMap();
        Map<String,List<LoanFile>> dataNumDiffMap=new HashedMap();
        for(String apiCode:apiCodes) {
            String compShortName = "";
            String companyMsg = "";
            try {
                companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
            } catch (Exception e) {
                log.error("getCompanyMsg error",e);
            }
            if (StringUtils.isNotEmpty(companyMsg)) {
                JSONObject companyJsonObj = JSON.parseObject(companyMsg);
                compShortName = companyJsonObj.getString("COMP_SHORT_NAME");
            }

            List<LoanFile> blrList = loanFileMapper.queryResultByApiCode(apiCode);
            if (blrList.size() > 0) {
                map.put(apiCode + "-" + compShortName, blrList);
            }

            List<LoanFile> emptyFileList = loanFileMapper.queryEmptyFileListByApiCode(apiCode);
            if (emptyFileList.size() > 0) {
                emptyFileMap.put(apiCode + "-" + compShortName, emptyFileList);
            }


            List<LoanFile> dataNumDiffList = loanFileMapper.querydataNumDiffListByApiCode(apiCode);
            if (dataNumDiffList.size() > 0) {
                dataNumDiffMap.put(apiCode + "-" + compShortName, dataNumDiffList);
            }
        }
        String today=new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName="result_file_list"+today+".xlsx";
        String s = syncConfigService.getPath() + "result/";
        export(map,fileName,s);

        String receive="xiaoxin.pang@brgroup.com,song.wang@brgroup.com,penghui.cheng@brgroup.com,xiangru.meng@brgroup.com,yanping.fu@brgroup.com";
        String subject ="智能营销平台当日任务结果统计报告";
        String msg =mailContent(map,emptyFileMap,dataNumDiffMap,today);
        try {
            SendMailClint.sendMail(receive, subject, msg, s,fileName);
        } catch (Exception e) {
            log.error("发送统计文件失败",e);
        }
    }


    @Override
    public void dataFileVolumn(String apiCode, String message) {

    }




    private String mailContent(Map<String,List<LoanFile>> map, Map<String,List<LoanFile>> emptyFileMap,
                               Map<String,List<LoanFile>> dataNumDiffMap, String today){
        StringBuilder content = new StringBuilder("<html><head></head><body><h2>智能营销平台当日任务结果统计</h2>");
        content.append("&nbsp;&nbsp;&nbsp;&nbsp;附件内容是每个客户当日监控任务返回结果的详细信息，包括任务批次号、" +
                "对应批次应该返回给客户的数据量、对应批次实际返回给客户的数据量、\r\n 对应批次实际返回文件数、" +
                "结果文件总大小以及结果文件的上传时间。如果有异常情况，会在邮件内容中以表格的形式展示。")
        .append("<table border=\"5\" style=\"border:solid 1px #E8F2F9;font-size=14px;;font-size:12px;\">");
        for (Map.Entry<String,List<LoanFile>> entry : map.entrySet()) {
            String key = entry.getKey();
            String apiCode= key.split("-")[0];
            int i= expectedFileNum(apiCode);
            int size=map.get(key).size();
            if(i!=size||(dataNumDiffMap!=null&&dataNumDiffMap.size()>0)
                    ||(emptyFileMap!=null&&emptyFileMap.size()>0)){
                List<LoanFile> bLoanResults = dataNumDiffMap.get(key);
                List<LoanFile> emptybLoanResults = emptyFileMap.get(key);
                if((bLoanResults!=null&&bLoanResults.size()>0)||(emptybLoanResults!=null&&emptybLoanResults.size()>0)){
                    content.append("<tr  style=\"font-size:16px;\"><th style=\"border:none;float:left\">")
                            .append(key)
                            .append(":</th></tr>");
                }
            }
            if(i!=size){
                content.append("<tr  style=\"font-size:12px;\"><th style=\"border:none;float:left\">结果文件数量异常:</th></tr>")
                       .append("<tr style=\"background-color: #428BCA; color:#ffffff\"><th>客户编号</th><th>日期</th>" +
                               "<th>应返回结果文件数</th><th>实际返回结果文件数</th></tr>")
                       .append("<tr>")
                       .append("<td>")
                       .append(apiCode)
                       .append("</td>")
                       .append("<td>")
                       .append(today)
                       .append("</td>")
                       .append("<td>")
                       .append(i)
                       .append("</td>")
                       .append("<td>")
                       .append(size)
                       .append("</td>")
                       .append("</tr>");
            }
            if(dataNumDiffMap!=null&&dataNumDiffMap.size()>0){
                List<LoanFile> bLoanResults = dataNumDiffMap.get(key);
                if(bLoanResults!=null&&bLoanResults.size()>0){
                    content.append("<tr  style=\"font-size:12px;\"><th style=\"border:none;float:left\">结果文件数据量异常:</th></tr>")
                    .append("<tr style=\"background-color: #428BCA; color:#ffffff\"><th>客户编号</th>" +
                            "<th>结果文件名称or批次号</th><th>应返回数据量</th><th>实际返回数据量</th><th>差异量</th></tr>");
                    for(LoanFile blf:bLoanResults){
                        content.append("<tr>")
                               .append("<td>" )
                               .append(blf.getApiCode())
                               .append("</td>");
                        String str=blf.getZipFileName();
                        content.append("<td>" )
                               .append(str)
                               .append("</td>")
                               .append("<td>")
                               .append(blf.getExpectedNum())
                               .append("</td>")
                               .append("<td>")
                               .append(blf.getActualNum())
                               .append("</td>")
                               .append("<td>")
                               .append((blf.getExpectedNum()-blf.getActualNum()))
                               .append("</td>")
                               .append("</tr>");
                    }
                }
            }
            if(emptyFileMap!=null&&emptyFileMap.size()>0){
                List<LoanFile> bLoanResults = emptyFileMap.get(key);
                if(bLoanResults!=null&&bLoanResults.size()>0){
                    content.append("<tr  style=\"font-size:12px;\"><th style=\"border:none;float:left\">结果文件大小异常:</th></tr>");
                    content.append("<tr style=\"background-color: #428BCA; color:#ffffff\">" +
                            "<th>客户编号</th><th>结果文件名称</th><th>文件大小</th></tr>");
                    for(LoanFile blr:bLoanResults){
                        content.append("<tr>")
                        .append("<td>")
                        .append(blr.getApiCode())
                        .append("</td>")
                        .append("<td>")
                        .append(blr.getZipFileName())
                        .append("</td>")
                        .append("<td>")
                        .append(blr.getFileSize())
                        .append("</td>")
                        .append("</tr>");
                    }
                }
            }
            content.append("<tr ><th style=\"border:none;height:25px\"></th></tr>");
        }
        content.append("</table>").append("</body></html>");
        log.info("emptyFileMap:{}",content);
        return content.toString();
    }

    private int expectedFileNum(String apiCode){
        int count=0;
        List<MarketingTask> marketingTasks = marketingTaskMapper.queryBatchNumByapiCode(apiCode);
        for(MarketingTask blt: marketingTasks){
            log.info("BLoanTask:{}",blt);
            int num=0;
            int days = 0;
            try {
                days = DateHelper.daysBetween(blt.getStartDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(StringUtils.isNotEmpty(blt.getFrequency())&&(days%Constants.frequencyMap.get(blt.getFrequency())==0)){
                    num=1;
            }
            if(StringUtils.isEmpty(blt.getFrequency())&&blt.getMonitorType()==1){
                num=1;
            }
            count=count+num;
        }
        return count;
    }


    /**
     * 导出存量监控结果文件列表
     * @param fileName
     */
    private static <T> void export(Map<String,List<T>> map, String fileName,String pathStr) {
        File writeName = new File(pathStr);
        if (!writeName.exists()) {
            boolean mkdirs = writeName.mkdirs();
            if(!mkdirs){
                log.error("创建文件失败：{}",pathStr);
            }
        }
        try (
                OutputStream out =  java.nio.file.Files.newOutputStream(Paths.get(pathStr + fileName));){
            ExcelWriter writer = new ExcelWriter(out, ExcelTypeEnum.XLSX,true);
            if (!map.isEmpty()) {
                int i=1;
                for(Map.Entry<String,List<T>> entry:map.entrySet()){
                    String sheetName=entry.getKey();
                    List<T> list=entry.getValue();
                    Sheet sheet = new Sheet(i, 0, (Class<? extends BaseRowModel>) list.get(0).getClass());
                    sheet.setSheetName(sheetName);
                    writer.write((List<? extends BaseRowModel>) list, sheet);
                    i++;
                }
                writer.finish();
            }
        } catch (Exception e) {
            log.error("export error",e);
        }
    }
}
