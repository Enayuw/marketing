package com.br.marketing.check.job;

import com.br.marketing.check.service.FileCheckService;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.check.utils.UploadDataFileUtil;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.LoadResult;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.mapper.LoadResultMapper;
import com.br.marketing.service.Impl.ValidDataAlarmServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.SftpException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
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
 * @Date 2021/4/27 15:43
 * @Description:
 **/
@Component
@Slf4j
public class SftpDeleteMonitorJob extends AbstractSimpleElasticJob {
    @Value("${otherConfig.warning.path:00}")
    private String path;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    private static final Pattern MYREGEX = Pattern.compile("\\.");
    private static final Pattern MYREGEX1 = Pattern.compile("_");

    @Resource
    FileCheckService fileCheckServiceImpl;
    @Resource
    LoadResultMapper loadResultMapper;
    @Resource
    ValidDataAlarmServiceImpl validDataAlarmService;
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Map<String, Set<String>> map=new HashMap<>();
        SftpClient sftpClient = new SftpClient(sftpHost,sftpPort,sftpUsername,sftpPwd);
        try {
            sftpClient.connect();
            SftpToDbUtils.listStpFile("/UploadFiles/marketing/",map,true,sftpClient);
            if(!map.isEmpty()){
                log.warn("----------SftpToDb开始处理新上传的剔除文件-------------");
                dealDeleteMonitorFile(map,sftpClient);
            }
        } catch (Exception e) {
            log.error("获取sftp上的剔除文件列表出错",e);
        }finally {
            try {
                sftpClient.disconnect();
            } catch (Exception e) {
                log.error("断开sftp连接出错",e);
            }
        }
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
            StringBuilder localFile=new StringBuilder(path)
                    .append("delete")
                    .append("/")
                    .append(apiCode)
                    .append("/")
                    .append(DateHelper.getDateAddYyMmDd(0)).append("/");

            for(String fileName:value){
                if(fileName.endsWith(".zip")){
                    String successFile=fileName+".success";
                    if(value.contains(successFile)){
                        String[] split = MYREGEX.split(fileName);
                        String zipName = split[0];
                        StringBuilder errorMessage=new StringBuilder("压缩文件异常,");
                        if(SftpToDbUtils.vaildFileName(fileName, apiCode,errorMessage)){
                            //fileCheckServiceImpl.parsingDeleteFile(key,fileName,localFile.toString(),apiCode,merchantParam,zipName,sftpClient);
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
