package com.br.marketing.check.job.dataclean;

import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.mapper.MarketingCleanDataFileMapperBase;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.IFileActionService;
import com.br.marketing.service.SyncConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
/**
 * @author zhen.Li1
 * @Classname DataCleanFileSyncJob
 * @Description 数据清洗文件同步
 * @Date 2024/05/22
 */
public class DataCleanFileSyncJob extends AbstractSimpleElasticJob {

    @Resource
    SyncConfigMapper syncConfigMapper;

    @Autowired
    SyncConfigService syncConfigService;

    @Autowired
    IFileActionService iFileActionService;

    @Autowired
    MarketingCleanDataFileMapperBase marketingCleanDataFileMapperBase;

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String jobParameter = context.getJobParameter();
        String apiCode = StringUtils.isBlank(jobParameter) ? "" : jobParameter;
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        SyncConfigExample.Criteria criteria = syncConfigExample.createCriteria();
        if (StringUtils.isNotBlank(apiCode)) {
            criteria.andApiCodeEqualTo(apiCode);
        }
        criteria.andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.MARKETINGUPLOADDATA.getValue())
                .andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        for (SyncConfig syncConfig : syncConfigs) {
            // 组装本地下载路径
            String targetPath = syncConfigService.getPath().concat("initPath/upload/").concat(syncConfig.getApiCode()).concat("/");
            SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
            Result<List<String>> res = iFileActionService.downSyncFileBySftp(sftpClient, syncConfig, targetPath);
            if (ResultCode.SUCCESS.getValue().equals(res.getCode())) {
                List<String> fileNames = res.getData();
                for (String fileName : fileNames) {
                    fileSyncTable(syncConfig.getApiCode(), targetPath, fileName);

                }
            }
        }

    }

    private void fileSyncTable(String apiCode, String path, String fileName) {
        MarketingCleanDataFile dataFile = new MarketingCleanDataFile();
        String fileStr = path.concat(fileName);
        File file = new File(fileStr);
        Integer line = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String row = "";
            while (line < 2) {
                row = br.readLine();
                if (line == 0) {
                    dataFile.setFileHeader(row);
                } else {
                    dataFile.setFileData(row);
                }
                line++;
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        dataFile.setApiCode(apiCode);
        dataFile.setFileName(fileName);
        dataFile.setCleanType(0);
        dataFile.setCreateTime(new Date());
        dataFile.setUpdateTime(new Date());
        marketingCleanDataFileMapperBase.insertSelective(dataFile);

    }
}
