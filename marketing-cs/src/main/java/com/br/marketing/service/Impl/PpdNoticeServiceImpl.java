package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.IceClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LoanFile;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.mapper.LoanMailMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ppd定制通知
 * Created by Bairong on 2020/7/11.
 */
@Service
@Slf4j
public  class PpdNoticeServiceImpl implements EmailService {
    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    LoanMailMapper loanMailMapper;
    @Resource
    private AlarmApiClient alarmClient;


    @Override
    public void sendAlarm(String context, String type) {

    }

    public void sendReport(String apiCode){
        List<MarketingTask> marketingTasks = marketingTaskMapper.queryBatchNumByapiCode(apiCode);
        List<LoanFile> blrList = loanFileMapper.queryResultByApiCode(apiCode);
        int expectedFileNumT1=0;
        int expectedFileNumT5=0;
        int actualFileNumT1=0;
        int actualFileNumT5=0;
        if(Constants.APICODE_PPD.equals(apiCode)||Constants.APICODE_PPD_QA.equals(apiCode)){
            for(MarketingTask blt: marketingTasks){
                int days = 0;
                try {
                    days = DateHelper.daysBetween(blt.getStartDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                expectedFileNumT1++;
                if(days % Constants.PPDFREQUENCY == 0){
                    expectedFileNumT5++;
                }
            }
            String  returnStatus="";
            if(blrList!=null&&blrList.size()>0){
                returnStatus="已回传";
                for(LoanFile blf:blrList){
                    log.warn("BLoanFile:{}",blf);
                    if(blf.getStatus()==2){
                        if(0==blf.getIsSec()){
                            actualFileNumT5++;
                        }else if(1==blf.getIsSec()){
                            actualFileNumT1++;
                        }
                    }else {
                        returnStatus="未回传完毕";
                    }
                }
            }else{
                returnStatus="未回传完毕";
            }


            String alarmDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String compShortName="";
            String companyMsg = IceClient.getCompanyMsg(apiCode);
            if(StringUtils.isNotEmpty(companyMsg)){
                JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
            }
            StringBuilder content = new StringBuilder();
            content.append("尊敬的客户：<br/>")
                   .append("&nbsp;&nbsp;您好！<br/><br/>")
                   .append("<html>\n" +
                    "<head></head>\n" +
                    "<table border=\"1\" cellspacing=\"0\" width=\"850\" height=\"150\" style=\"text-align: center\">")
                   .append("<tr style=\"background-color: #B3B3B3; color:#ffffff text-align: center \"><td colspan=\"5\">百融存量客户监控【")
                    .append(alarmDate).append("】当前回传文件结果统计</td></tr>")
                   .append("<tr><td colspan=\"1\" style=\"background-color: #E6E6E6;\">回传状态</td><td colspan=\"4\">")
                    .append(returnStatus).append("</td></tr>")
                   .append("<tr><td style=\"background-color: #E6E6E6;\">全量字段回传结果</td><td >应回传结果文件数量</td><td width=\"50\">")
                    .append(expectedFileNumT5).append("</td><td >已回传结果文件数量</td><td width=\"50\">")
                    .append(actualFileNumT5)
                    .append("</td></tr>")
                   .append("<tr><td style=\"background-color: #E6E6E6;\">重点字段回传结果</td><td >应回传结果文件数量</td><td >")
                    .append(expectedFileNumT1).append("</td><td >已回传结果文件数量</td><td >")
                    .append(actualFileNumT1).append("</td></tr>")
                   .append("</table>")
                   .append("</body></html>");
            log.info(content.toString());
            String title="百融存量客户监控-文件回传结果通知【"+compShortName+"-"+apiCode+"】";
            String mails=loanMailMapper.queryMails(apiCode);
            alarmClient.send(title,content.toString(),mails);
        }
    }

    @Override
    public void resultVolumeCheck(String apiCode) {

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
    public void zipFileErrorAlarm(String fileName, String title) {

    }

    @Override
    public void closeDateAlarm() {

    }

    @Override
    public void monitoringExpirationAlarm() {

    }


}
