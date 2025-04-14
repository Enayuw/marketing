package com.br.marketing.bridge.job.clean;

import com.br.common.validator.DateUtils;
import com.br.marketing.client.FtpClient;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.TimeUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @ClassName ShuHeFileCleanUploadDateJob
 * @Description 数禾洗库：https://c.100credit.cn/pages/viewpage.action?pageId=201089016
 * @Author kongbx
 * @Date 2025/4/14 10:36
 */
@Component
@Slf4j
public class ShuHeFileCleanUploadDateJob extends AbstractSimpleElasticJob {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;
    @Resource
    SyncConfigMapper syncConfigMapper;
    private static final String SOURCEPATH = "/DATASHARE/yingxiao/mmg/shuhe/";
    public static final String LOCALPATH = "/opt/data/inloan/download/marketingCommonApplet/";
    public static final int MAX_RETRY_COUNT = 3;
    private static final String TITLE = "【数禾首借数据自动化匹配洗库】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.info("{}开始执行", TITLE);
        long start = System.currentTimeMillis();

        try {
            // 1. 获取配置
            Map<String, Object> config = getConfig();

            // 2. 处理每个apiCode
            processApiCodes(config);

        } finally {
            long end = System.currentTimeMillis();
            log.info("{}执行完成，耗时{}ms", TITLE, end - start);
        }
    }
    private Map<String, Object> getConfig() {
        // {"apiCode":["3710028","3710048"],"appletDate":["2025-01-01","2025-01-02"],"fileName":"pdm_cusop_slp_dyy_br1_df_yyyy-MM-dd_5.csv"}
        Map<String, Object> map = marketingCommonConfig.getShuHeFileCleanUploadDateConfig();
        List<String> apiCodes = (List<String>) map.get("apiCode");
        List<String> appletDates = (List<String>) map.get("appletDate");
        String fileName = (String) map.get("fileName");
        if (CollectionUtils.isEmpty(apiCodes)) {
            apiCodes.add("3710028");
            apiCodes.add("3710048");
        }
        if (CollectionUtils.isEmpty(appletDates)) {
            appletDates.add(LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (fileName.contains("yyyy-MM-dd")) {
            fileName.replace("yyyy-MM-dd", TimeUtils.getNowDate(TimeUtils.DATE_FORMAT));
        }
        return map;
    }

    private void processApiCodes(Map<String, Object> map) {
        List<String> apiCodes = (List<String>) map.get("apiCode");
        List<String> appletDates = (List<String>) map.get("appletDate");
        String fileName = (String) map.get("fileName");

        for (String apiCode : apiCodes){
            //判断今日是否执行过
            TransferActionFrontExample frontExample = new TransferActionFrontExample();
            frontExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andActionDataEqualTo(LocalDate.now().toString())
                    .andActionTypeEqualTo(1)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

            if(!CollectionUtils.isEmpty(transferActionFronts)){
                log.warn(TITLE + "今日已执行！");
                return;
            }

            SyncConfigExample syncConfigExample = new SyncConfigExample();
            syncConfigExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andStatusEqualTo(1)
                    .andDataTypeEqualTo(DataTypeEnum.MARKETINGUPLOADDATA.getValue())
                    .andSrcSftpHostLike("/DATASHARE/yingxiao/mmg/shuhe")
                    .andTypeEqualTo(1);
            List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
            if(CollectionUtils.isEmpty(syncConfigs)){
                log.warn(TITLE + "SFTP配置不存在！");
                return;
            }
            SyncConfig syncConfig = syncConfigs.get(0);

            processFile(syncConfig,fileName);
        }

    }

    private void processFile(SyncConfig syncConfig, String fileName) {
        // 文件处理逻辑
        String srcPath = syncConfig.getSrcPath();
        FtpClient client = new FtpClient(syncConfig.getSrcSftpHost(), syncConfig.getSrcSftpPort(), syncConfig.getSrcSftpUser(),
                syncConfig.getSrcSftpPwd(), srcPath);
        for (int retry = 0; retry <= MAX_RETRY_COUNT; retry++) {
            try {
                client.connect();
                boolean fileExists = client.isExsits(syncConfig.getSrcPath().concat(fileName));
                if (!fileExists) {
                    log.warn(TITLE + "文件不存在:{}", fileName);
                    return;
                }
                // 判断文件创建时间是否大于5分钟
                FTPFile ftpFile = client.getFtpFile(srcPath + "/" , fileName);
                Calendar timestamp = ftpFile.getTimestamp();
                String createFileTime = DateUtils.parseDateTimeByDate( timestamp.getTime(), "yyyy-MM-dd HH:mm:ss");
                long minutes = DateHelper.getDistanceMinutes(createFileTime);
                if (minutes < 5) {
                    log.warn("文件上传时间距离当前时间小于5分钟，暂时不处理");
                    return;
                }
                //判断.success文件是否存在
                String successName = fileName + ".success";
                boolean successFileExists = client.isExsits(syncConfig.getSrcPath().concat(successName));
                if (!successFileExists) {
                    //创建.success文件
                    log.warn(TITLE + ".success文件不存在:{}", successName);
                    File successFile = new File(syncConfig.getSrcPath().concat(successName));
                    successFile.createNewFile();
                    return;
                }
                break;
            } catch (Exception e) {
                log.warn(TITLE + "第{}次任务异常", retry + 1, e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } finally {
                try {
                    client.disconnect();
                } catch (Exception e) {
                    log.error(TITLE + "文件检查任务关闭FTP客户端异常", e);
                }
            }
        }
    }

}
