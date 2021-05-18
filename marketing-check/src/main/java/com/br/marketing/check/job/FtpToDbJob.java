package com.br.marketing.check.job;

import com.br.marketing.check.service.FileUploadService;
import com.br.marketing.check.utils.UploadDataFileUtil;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.FtpUtil;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.Impl.ValidDataAlarmServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/4/27 15:47
 * @Description:
 **/
@Component
@Slf4j
public class FtpToDbJob extends AbstractSimpleElasticJob {

    @Value("${otherConfig.warning.ftpHost:00}")
    private String ftpHost;
    @Value("${otherConfig.warning.ftpPort:00}")
    private Integer ftpPort;
    @Value("${otherConfig.warning.ftpUsername:00}")
    private String ftpUsername;
    @Value("${otherConfig.warning.ftpPwd:00}")
    private String ftpPwd;
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Resource
    FileUploadService dataFileUploadServiceImpl;
    @Resource
    LoadResultMapper loadResultMapper;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Resource
    RedisChgService redisChgService;
    @Resource
    MarketingUserMapper marketingUserMapper;

    private static final Pattern MYREGEX = Pattern.compile("\\.");

    @Resource
    ValidDataAlarmServiceImpl validDataAlarmService;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Map<String, Set<String>> map=new HashMap<>();
        FtpUtil ftpUtil=FtpUtil.createFtpCli(ftpHost, ftpUsername, ftpPwd,"/loanwarn/");
        try {
            boolean connect = ftpUtil.connect();
            if(connect){
                log.warn("======登录成功===开始数据文件处理======");
            }else{
                log.warn("======登录失败=========");
                return;
            }
            UploadDataFileUtil.listFiles("/loanwarn/",map,false,ftpUtil);
            if(!map.isEmpty()){
                log.warn("----------FtpToDb开始处理新上传的数据文件-------------");
                dealDataFile(map,ftpUtil);
            }
        } catch (Exception e) {
            log.error("获取ftp上的剔除文件列表出错",e);
        }finally {
            ftpUtil.disconnect();
        }
    }

    /**
     * 开始处理新上传的文件
     * @param map key ftp上的路径loanwarn/4200333/input
     *            value 对应目录下新上传的文件
     * @param ftpUtil
     */
    private void dealDataFile(Map<String, Set<String>> map, FtpUtil ftpUtil) {
        for(Map.Entry<String,Set<String>> entry:map.entrySet()){
            String key = entry.getKey();
            Set<String> value = entry.getValue();
            MerchantParam merchantParam = UploadDataFileUtil.vaildApicode(key);
            if(merchantParam==null){
                log.error("vaildApicode error {}",key);
                continue;
            }
            String callMethod = merchantParam.getCallMethod();
            int monitorType;
            //交付系统和存量监控对一次性任务和严格增量任务的code正好相反
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
            StringBuilder localFile=new StringBuilder(path)
                    .append("ftp_data")
                    .append("/")
                    .append(apiCode)
                    .append("/");
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
                            dataFileUploadServiceImpl.parsingFile(key,fileName,localFile.toString(),
                                    apiCode,merchantParam,zipName,batchNumber,lt,ftpUtil);
                        }else{
                            UploadDataFileUtil.returnErrorFile(apiCode, localFile.toString(), fileName, errorMessage,ftpUtil);
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
                        try {
                            ftpUtil.changeWorkingDirectory("/loanwarn/"+apiCode+"/input/");
                            ftpUtil.rename(successFile,successFile+".bak");
                            ftpUtil.rename(fileName,fileName+".bak");
                        } catch (Exception e) {
                            log.warn("rename file error ",e);
                            try {
                                ftpUtil.disconnect();
                                ftpUtil.connect();
                                ftpUtil.changeWorkingDirectory("/loanwarn/"+apiCode+"/input/");
                                ftpUtil.rename(successFile,successFile+".bak");
                                ftpUtil.rename(fileName,fileName+".bak");
                            } catch (Exception ex) {
                                log.error("rename file error ",e);
                            }
                        }
                    }
                }
            }
        }
    }
}
