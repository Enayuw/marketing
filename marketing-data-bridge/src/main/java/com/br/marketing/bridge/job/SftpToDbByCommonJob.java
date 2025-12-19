package com.br.marketing.bridge.job;

import com.br.common.log.AlertLog;
import com.br.marketing.bridge.DataBridgeApplication;
import com.br.marketing.bridge.common.enums.FileTypeToAssemblerEnum;
import com.br.marketing.bridge.common.utils.SftpToDbUtils;
import com.br.marketing.bridge.model.dto.FileContext;
import com.br.marketing.bridge.service.todb.impl.SftpToDbByCommonService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.ITxtToDbService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.file.filetodb.AbstractFileToDbAssembler;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Function;
import com.jcraft.jsch.JSchException;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SftpToDbByCommonJob extends AbstractSimpleElasticJob {
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
    MarketingTaskExtendMapper marketingTaskExtendMapper;
    @Resource
    IApiToDbService iApiToDbService;
    @Resource
    MarketingCustomerMapper marketingCustomerMapper;
    @Resource
    SyncConfigMapper syncConfigMapper;
    @Resource
    SftpToDbByCommonService sftpToDbByCommonService;
    @Resource
    LocalFileMapper localFileMapper;
    @Resource
    FileDbConfigMapper fileDbConfigMapper;
    @Resource
    ITxtToDbService iTxtToDbService;
    @Resource
    ICompatibleService iCompatibleService;


    /**
     *  1、先从customer读取客户
     *  2、再从sftp配置表读取路径
     *  3、查找该路径下的success文件
     *  4、把该文件同名的txt文件进行读取操作
     *      4.1、从标题读取到扩展字段标志位的位置
     *      4.2、标志位以前是表的基础字段，标志位以后是表的扩展字段
     *      4.3、存入读取记录表，存入数据表
     * @param jobExecutionMultipleShardingContext
     */
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        List<String> apiCodes = marketingCustomers.stream().filter(t->iCompatibleService.isAction(t.getExtendConfigInfo(),jobExecutionMultipleShardingContext.getJobName()))
                .map(t -> t.getApiCode()).collect(Collectors.toList());
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        syncConfigExample.createCriteria().andApiCodeIn(apiCodes).andStatusEqualTo(1).andDataTypeEqualTo(DataTypeEnum.FILETODB.getValue()).andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        syncConfigs.forEach(t->{
            if(StringUtils.isNotBlank(t.getTargetPath())){
                FileDbConfigExample fileDbConfigExample = new FileDbConfigExample();
                fileDbConfigExample.createCriteria().andSftpConfigIdEqualTo(t.getId()).andDelEqualTo(1);
                List<FileDbConfig> fileDbConfigs = fileDbConfigMapper.selectByExample(fileDbConfigExample);
                if(fileDbConfigs.size()<=0){
                    return;
                }
                FileDbConfig fileDbConfig = fileDbConfigs.get(0);
                Result<String> fileConfigRes = sftpToDbByCommonService.checkFileDbconfig(fileDbConfig);
                if(!ResultCode.SUCCESS.getValue().equals(fileConfigRes.getCode())){
                    log.error(String.format("apicode:%s的 %s",t.getApiCode(),fileConfigRes.getMessage()));
                }
                Map<String, Set<String>> map = new HashMap<>();
                SftpClient sftpClient = new SftpClient(sftpHost, sftpPort,sftpUsername,sftpPwd);
                try {
                    sftpClient.connect();
                    SftpToDbUtils.listStpFile(t.getTargetPath(), map, sftpClient,t);
                    if (!map.isEmpty()) {
                        log.warn(String.format("----------获取apiCode:%s数据开始-------------",t.getApiCode()));
                        long start = System.currentTimeMillis();
                        dealDataFile(map, sftpClient,t,fileDbConfig);
                        long end = System.currentTimeMillis();
                        if (log.isWarnEnabled()) {
                            log.warn(String.format("数据入库时间:%d", end - start));
                        }
                    }
                } catch (JSchException e) {
                    log.error("SftpToDbByCommonJob,sftp连接失败", e);
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
    private void dealDataFile(Map<String, Set<String>> map, SftpClient sftpClient, SyncConfig syncConfig,FileDbConfig fileDbConfig) {
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            String srcPath = entry.getKey();
            Set<String> fileNames = entry.getValue();
            //初始化参数对象
            String apiCode = syncConfig.getApiCode();

            for (String fileName : fileNames) {
                if(fileName.endsWith(".txt") || fileName.endsWith(".csv")){
                    log.warn(String.format("获取到%s的文件:%s",apiCode,fileName));
                    FileContext context = new FileContext();
                    context.setBaseFtpClient(sftpClient);
                    context.setSftpZipFilePath(srcPath);
                    context.setApiCode(apiCode);
                    context.setTxtFileName(fileName);
                    String successFile = fileName + ".success";
                    if (fileNames.contains(successFile)) {
                        context.setLocalTxtFilePath(syncConfigService.getPath().concat(fileDbConfig.getInnerPath()).concat("/")
                                .concat(apiCode).concat("/"));
                        if(!sftpToDbByCommonService.dowloadFile(context)){
                            continue;
                        }
                        LocalFile localFile = new LocalFile();
                        localFile.setApiCode(syncConfig.getApiCode());
                        localFile.setSrcPath(context.getSftpZipFilePath());
                        localFile.setFileName(context.getTxtFileName());
                        localFile.setLocalPath(context.getLocalTxtFilePath());
                        localFile.setStatus("1");
                        localFile.setCreateTime(new Date());
                        localFile.setFileType(fileDbConfig.getFileType());
                        // 只有指定的fileType，pushStatus才置为0
                        String fileType = String.valueOf(fileDbConfig.getFileType());
                        String pushStatusMark = FileTypeToAssemblerEnum.getPushStatusMarkByFileType(fileType);
                        if("1".equals(pushStatusMark)){
                            localFile.setPushStatus("0");
                        }
                        localFileMapper.insertSelective(localFile);

                        try {
                            String yyyyMMddHHmmss = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                            sftpClient.rename(srcPath + successFile, srcPath + successFile+"_"+yyyyMMddHHmmss+".bak");
                            sftpClient.rename(srcPath + fileName, srcPath + fileName+"_"+yyyyMMddHHmmss+ ".bak");
                            ArrayList<String> baseHeads = new ArrayList<String>();

                            Function<TxtToDbDTO, Result> function = getFunctionByFileType(fileType);
                            sftpToDbByCommonService.actionTxtFile(context, localFile, fileDbConfig, function);
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

    private Function<TxtToDbDTO, Result> getFunctionByFileType(String fileType){
        log.info("fileType: " + fileType);
        if(StringUtils.isEmpty(fileType)){
            return iTxtToDbService::toDbByCommon;
        }
        String assemblerName = FileTypeToAssemblerEnum.getAssemblerByFileType(fileType);
        log.info("assemblerName: " + assemblerName);
        if(!StringUtils.isEmpty(assemblerName)){
            AbstractFileToDbAssembler csvToDbAssembler = (AbstractFileToDbAssembler) DataBridgeApplication.ac.getBean(assemblerName);
            return csvToDbAssembler::operateDateToDb;
        }
        return iTxtToDbService::toDbByCommon;
    }
}
