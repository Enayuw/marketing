package com.br.marketing.check.job;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.enums.ErrorFileTypeEnum;
import com.br.marketing.check.service.Impl.DeleteService;
import com.br.marketing.check.service.Impl.FileCheckServiceImpl;
import com.br.marketing.check.service.Impl.SftpToDbService;
import com.br.marketing.check.utils.SftpToDbUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IApiToDbService;
import com.br.marketing.service.Impl.ValidDataAlarmServiceImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.JSchException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
 * @Date 2021/4/27 15:46
 * @Description:
 **/
@Component
@Slf4j
public class SftpToDbByResultDataJob extends AbstractSimpleElasticJob {
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
    @Resource
    SftpToDbService sftpToDbService;
    @Resource
    MarketingTaskMapper marketingTaskMapper;
    @Autowired
    MarketingTaskExtendMapper marketingTaskExtendMapper;
    @Resource
    RedisChgService redisChgService;
    @Resource
    MarketingUserMapper marketingUserMapper;
    @Resource
    ValidDataAlarmServiceImpl validDataAlarmService;
    @Resource
    FileCheckServiceImpl fileCheckService;
    @Resource
    DeleteService deleteService;
    @Autowired
    IApiToDbService iApiToDbService;
    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;
    
    @Autowired
    SyncConfigMapper syncConfigMapper;

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
        List<String> apiCodes = marketingCustomers.stream().map(t -> t.getApiCode()).collect(Collectors.toList());
        SyncConfigExample syncConfigExample = new SyncConfigExample();
        syncConfigExample.createCriteria().andApiCodeIn(apiCodes).andStatusEqualTo(1).andDataTypeEqualTo(3).andTypeEqualTo(1);
        List<SyncConfig> syncConfigs = syncConfigMapper.selectByExample(syncConfigExample);
        for (int i = 0; i < syncConfigs.size(); i++) {
            
        }
        Map<String, Set<String>> map = new HashMap<>();
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        try {
            sftpClient.connect();
            SftpToDbUtils.listStpFile("/UploadFiles/marketing/", map, sftpClient);
            if (!map.isEmpty()) {
                log.info("----------SftpToDb开始处理新上传的数据文件-------------");
                long start = System.currentTimeMillis();
                dealDataFile(map, sftpClient);
                long end = System.currentTimeMillis();
                if (log.isWarnEnabled()) {
                    log.warn(String.format("数据入库时间:%d", end - start));
                }
            }
        } catch (JSchException e) {
            log.error("SftpToDbJob,sftp连接失败", e);
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

    /**
     * 开始处理新上传的文件
     *
     * @param map        key ftp上的路径loanwarn/4200333/input
     *                   value 对应目录下新上传的文件
     * @param sftpClient
     */
    private void dealDataFile(Map<String, Set<String>> map, SftpClient sftpClient) {
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            String sftpzipFilePash = entry.getKey();
            Set<String> zipFileNameSet = entry.getValue();
            MerchantParam merchantParam = SftpToDbUtils.vaildApicode(sftpzipFilePash);
            if (merchantParam == null) {
                log.error("vaildApicode error {}", sftpzipFilePash);
                continue;
            }
            int monitorType = Integer.parseInt(merchantParam.getCallMethod());
            String apiCode = merchantParam.getApiCode();
            String tableName = "b_marketing_user_" + apiCode;
            marketingUserMapper.createUserTable(tableName);
            //初始化参数对象
            FileContext context = new FileContext();
            context.setBaseFtpClient(sftpClient);
            context.setMerchantParam(merchantParam);
            context.setSftpZipFilePath(sftpzipFilePash);
            context.setApiCode(apiCode);
            for (String zipFileName : zipFileNameSet) {
                if (zipFileName.endsWith(".zip")) {
                    //设置zip文件名
                    context.setZipFileName(zipFileName);
                    String successFile = zipFileName + ".success";
                    if (zipFileNameSet.contains(successFile)) {
                        StringBuilder errorMessage = new StringBuilder("压缩文件异常,");
                        if (zipFileName.contains("DeleteMonitor")) {
                            context.setLocalZipFilePath(path.concat("delete/").concat(apiCode).concat("/"));
                            context.setType("delete");
                            context.setCusBatch(Constants.MYREGEX.split(zipFileName)[0]);
                            context.init();
                            if (SftpToDbUtils.vaildFileName(zipFileName, apiCode, errorMessage)) {
                                deleteService.execute(context);
                            } else {
                                fileCheckService.errorDetail(context, errorMessage.toString(), ErrorFileTypeEnum.ERROR_FILE);
                            }
                            validDataAlarmService.deleteMonitorFileUpload(apiCode, Constants.MYREGEX.split(context.getZipFileName())[0]);
                        } else {
                            context.setLocalZipFilePath(path.concat("sftp_data/").concat(apiCode).concat("/"));
                            String batchNumber = SftpToDbUtils.getBatchNumber(apiCode);
                            context.setBatchNumber(batchNumber);
                            context.setType("data");
                            MarketingTask task = new MarketingTask();
                            task.setApiCode(apiCode);
                            task.setBatchNumber(batchNumber);
                            task.setMonitorType(monitorType);
                            task.setMonitorStatus(0);
                            task.setStatus(2);
                            task.setFileName(Constants.MYREGEX.split(zipFileName)[0]);
                            task.setCusBatch(task.getFileName());
                            task.setContextId(iApiToDbService.getTaskContextId());
                            context.setCusBatch(task.getFileName());
                            marketingTaskMapper.insertTask(task);
                            MarketingTaskExtend taskExtend = new MarketingTaskExtend();
                            taskExtend.setApiCode(apiCode);
                            taskExtend.setTaskId(task.getId());
                            taskExtend.setCusTaskId(task.getFileName());
                            taskExtend.setCreateTime(new Date());
                            taskExtend.setUploadTime(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                            marketingTaskExtendMapper.insertSelective(taskExtend);
                            context.setTask(task);
                            context.init();
                            if (SftpToDbUtils.vaildFileName(zipFileName, apiCode, errorMessage)) {
                                sftpToDbService.execute(context);
                            } else {
                                fileCheckService.errorDetail(context, errorMessage.toString(), ErrorFileTypeEnum.ERROR_FILE);
                            }

                            String taskNumber = redisChgService.get(Constants.UPLOAD_DATA_NUM + batchNumber);
                            redisChgService.expire(Constants.UPLOAD_DATA_NUM + batchNumber, 60);
                            String failNumber = redisChgService.get(Constants.UPLOAD_FAILDATA_NUM + batchNumber);
                            task.setTableName("b_marketing_user_" + apiCode);
                            Integer actualNumber = marketingUserMapper.queryCount(task);
                            log.info("taskNumber:{},FailNumber:{}, actualNumber:{}", taskNumber, failNumber, actualNumber);
                            task.setTaskNumber(StringUtils.isNotEmpty(taskNumber) ? Integer.parseInt(taskNumber) : 0);
                            task.setActualNumber(actualNumber);
                            log.info("LoanTask:{}", task);
                            marketingTaskMapper.modifyTask(task);
                            fileCheckService.volidatorDataVolume(task.getDataVolume(), task.getTaskNumber(),
                                    context.getApiCode(), context.getTxtFileName());
                            validDataAlarmService.fileUpload(apiCode, batchNumber);
                        }

                        String inputPath = Constants.SFTP_IN_INPUT_PATH.replace("apiCode", apiCode);
                        try {
                            sftpClient.rename(inputPath + successFile, inputPath + successFile + ".bak");
                            sftpClient.rename(inputPath + zipFileName, inputPath + zipFileName + ".bak");
                        } catch (Exception e) {
                            log.warn("rename file error ", e);
                            try {
                                sftpClient.disconnect();
                                sftpClient.connect();
                                sftpClient.rename(inputPath + successFile, inputPath + successFile + ".bak");
                                sftpClient.rename(inputPath + zipFileName, inputPath + zipFileName + ".bak");
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
