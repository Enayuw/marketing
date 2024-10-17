package com.br.marketing.bridge.job;

import com.br.marketing.bridge.common.utils.SftpToDbUtils;
import com.br.marketing.bridge.model.dto.FileContext;
import com.br.marketing.bridge.service.todb.impl.SftpToDbByCommonService;
import com.br.marketing.bridge.service.todb.impl.SftpToDbByDXService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.SyncConfigExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.ITxtToDbService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Component
@Slf4j
public class SftpToDbByDxTransferDataJob extends AbstractSimpleElasticJob {
    @Resource
    SyncConfigService syncConfigService;
    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;
    @Value("${otherConfig.warning.sftpPort:00}")
    private Integer sftpPort;
    @Value("${otherConfig.warning.sftpUser:00}")
    private String sftpUsername;
    @Value("${otherConfig.warning.sftpPwd:00}")
    private String sftpPwd;
    @Resource
    IApiToDbService iApiToDbService;
    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    SyncConfigMapper syncConfigMapper;

    @Resource
    SftpToDbByDXService sftpToDbByDXService;

    @Resource
    SftpToDbByCommonService sftpToDbByCommonService;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    ITxtToDbService iTxtToDbService;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    ICompatibleService iCompatibleService;

    /**
     * 1、先从customer读取客户
     * 2、再从sftp配置表读取路径
     * 3、查找该路径下的success文件
     * 4、把该文件同名的txt文件进行读取操作
     * 4.1、从标题读取到扩展字段标志位的位置
     * 4.2、标志位以前是表的基础字段，标志位以后是表的扩展字段
     * 4.3、存入读取记录表，存入数据表
     *
     * @param jobExecutionMultipleShardingContext
     */
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        List<String> apiCodes = marketingCustomers.stream().filter(t->iCompatibleService.isAction(t.getExtendConfigInfo(),jobExecutionMultipleShardingContext.getJobName())).map(t -> t.getApiCode()).collect(Collectors.toList());
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        syncConfigExample.createCriteria().andApiCodeIn(apiCodes).andStatusEqualTo(1)
                .andDataTypeEqualTo(DataTypeEnum.DXTRANSFER.getValue()).andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        syncConfigs.forEach(t -> {
            if (StringUtils.isNotBlank(t.getTargetPath())) {
                Map<String, Set<String>> map = new HashMap<>();
                SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
                try {
                    sftpClient.connect();
                    SftpToDbUtils.listStpFile(t.getTargetPath(), map, sftpClient, t);
                    if (!map.isEmpty()) {
                        log.info("----------获取运营需要推送电销转化结果数据-------------");
                        long start = System.currentTimeMillis();
                        dealDataFile(map, sftpClient, t);
                        long end = System.currentTimeMillis();
                        if (log.isWarnEnabled()) {
                            log.warn(String.format("数据入库时间:%d", end - start));
                        }
                    }
                } catch (JSchException e) {
                    log.error("SftpToDbByDxTransferDataJob,sftp连接失败", e);
                } catch (Exception e) {
                    log.error("获取sftp上的数据文件列表出错", e);
                } finally {
                    try {
                        sftpClient.disconnect();
                    } catch (Exception e) {
                        log.error("断开sftp连接出错", e);
                    }
                }
            }
        });
    }

    /**
     * 开始处理新上传的文件
     *
     * @param map        key ftp上的路径loanwarn/4200333/input
     *                   value 对应目录下新上传的文件
     * @param sftpClient
     */
    private void dealDataFile(Map<String, Set<String>> map, SftpClient sftpClient, SyncConfig syncConfig) {
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            String srcPath = entry.getKey();
            Set<String> fileNames = entry.getValue();
            //初始化参数对象
            String apiCode = syncConfig.getApiCode();
            for (String fileName : fileNames) {
                if (fileName.endsWith(".txt")) {
                    FileContext context = new FileContext();
                    context.setBaseFtpClient(sftpClient);
                    context.setSftpZipFilePath(srcPath);
                    context.setApiCode(apiCode);
                    context.setTxtFileName(fileName);
                    String successFile = fileName + ".success";
                    if (fileNames.contains(successFile)) {
                        context.setLocalTxtFilePath(syncConfigService.getPath()
                                .concat("sftp_dxTransfer_data/")
                                .concat(apiCode).concat("/"));
                        if (!sftpToDbByDXService.dowloadFile(context)) {
                            continue;
                        }
                        LocalFile localFile = new LocalFile();
                        localFile.setApiCode(syncConfig.getApiCode());
                        localFile.setSrcPath(context.getSftpZipFilePath());
                        localFile.setFileName(context.getTxtFileName());
                        localFile.setLocalPath(context.getLocalTxtFilePath());
                        localFile.setStatus("1");
                        localFile.setCreateTime(new Date());
                        localFile.setFileType(SftpFileTypeEnum.DXTRANSFORM.getValue());
                        localFileMapper.insertSelective(localFile);

                        try {
                            String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                            sftpClient.rename(srcPath + successFile, srcPath + successFile + "_" + yyyyMMddHHmmss + ".bak");
                            sftpClient.rename(srcPath + fileName, srcPath + fileName + "_" + yyyyMMddHHmmss + ".bak");
                            ArrayList<String> baseHeads = new ArrayList<String>();
                            sftpToDbByCommonService.actionTxtFile(context
                                    , localFile
                                    , baseHeads
                                    , iTxtToDbService::phoneTodbByTransfer);

                            // 更新推送状态为待推送
                            LocalFile updateFile = new LocalFile();
                            updateFile.setId(localFile.getId());
                            updateFile.setPushStatus("0");
                            localFileMapper.updateByPrimaryKeySelective(updateFile);
                        } catch (Exception e) {
                            log.warn("rename file error ", e);
                            try {
                                sftpClient.rename(srcPath + successFile, srcPath + successFile + ".bak");
                                sftpClient.rename(srcPath + fileName, srcPath + fileName + ".bak");
                                sftpClient.disconnect();
                                sftpClient.connect();
                            } catch (Exception ex) {
                                log.error("rename file error ", ex);
                            }
                        }
                    }
                }
            }
        }
    }
}
