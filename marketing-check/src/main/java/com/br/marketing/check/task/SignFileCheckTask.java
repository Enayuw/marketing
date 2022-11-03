package com.br.marketing.check.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.file.FtpUtil2;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
/**
 * 如果出现以下几种异常，当日的标识文件暂时不会上传，并提交标识文件校验任务，每30分钟校验一次标识是否已上传，校验到标识文件已上传后自动结束定时任务
 * 1.服务器本地磁盘上文件的大小和ftp上的文件大小不一致。
 * 2.ftp上的结果文件大小为0k，或者360客户的结果文件大小超过2M。
 * 3.应返回条数与实际上传到ftp上的条数差大于20条。
 * 4.应返回数据文件个数与实际上传到ftp的数据文件个数不一致。
 */
public class SignFileCheckTask implements Runnable {

    private String apiCode;
    private EmailService systemExceptionServiceImpl;
    private EmailService businessAlarmServiceImpl;
    private String ftpHost;
    private Integer ftpPort;
    private String ftpUsername;
    private String ftpPwd;
    private ScheduledExecutorService executorService;

    public SignFileCheckTask(String apiCode, EmailService systemExceptionServiceImpl, EmailService businessAlarmServiceImpl,
                             String ftpHost, Integer ftpPort, String ftpUsername, String ftpPwd, ScheduledExecutorService executorService) {
        this.apiCode = apiCode;
        this.systemExceptionServiceImpl = systemExceptionServiceImpl;
        this.businessAlarmServiceImpl = businessAlarmServiceImpl;
        this.ftpHost = ftpHost;
        this.ftpPort = ftpPort;
        this.ftpUsername = ftpUsername;
        this.ftpPwd = ftpPwd;
        this.executorService=executorService;
    }

    @Override
    public void run() {
        String today= DateHelper.getDateAddYyMmDd(0);
        FtpUtil2 ftp=new FtpUtil2();
        try {
            boolean connect = ftp.connect( "/loanwarn/" + apiCode + "/output/"+today+"/", ftpHost, ftpPort, ftpUsername, ftpPwd);
            if(!connect){
                log.error("获取ftp链接出错");
                return;
            }
            String signFileName=apiCode+"_ReturnCompleted_"+today+".finish";
            boolean exsits = ftp.isExsits(signFileName);
            String compShortName="";
            String companyMsg = RpcClientProxy.getCompanyMsg(apiCode);
            if(StringUtils.isNotEmpty(companyMsg)){
                JSONObject companyJSONObj = JSON.parseObject(companyMsg);
                compShortName=companyJSONObj.getString("COMP_SHORT_NAME");
            }
            if(!exsits){
                StringBuilder content = new StringBuilder();
                content.append(apiCode).append("-").append(compShortName).append("今日标识文件：").append(signFileName)
                        .append("未上传。30分钟后再次校验");
                systemExceptionServiceImpl.sendAlarm(content.toString(),"Sign file not uploaded");
            }else{
                log.warn("{} 标识文件已上传",signFileName);
                systemExceptionServiceImpl.sendAlarm(apiCode+"-"+compShortName+"标识文件："+signFileName+"已上传","Sign file  uploaded");

                final ScheduledExecutorService executorService1 = new ScheduledThreadPoolExecutor(1,
                        new BasicThreadFactory.
                                Builder().namingPattern("ftpToSftpCheck-inner-schedule-pool-%d").daemon(true).build());
                FtpToSftpCheckTask ftpToSftpCheckTask = new FtpToSftpCheckTask(apiCode, businessAlarmServiceImpl,executorService1);
                executorService1.schedule(ftpToSftpCheckTask, 3600000,  TimeUnit.MILLISECONDS);
               /* Timer t1=new Timer();
                FtpToSftpCheckTask ftpToSftpCheckTask = new FtpToSftpCheckTask(apiCode, businessAlarmServiceImpl,t1);
                t1.schedule(ftpToSftpCheckTask,3600000);*/
                executorService.shutdown();
            }

        }catch (Exception e){
            log.error("SignFileCheckTask error {}",e);
        }
    }
}
