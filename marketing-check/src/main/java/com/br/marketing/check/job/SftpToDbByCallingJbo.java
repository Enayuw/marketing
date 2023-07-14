package com.br.marketing.check.job;

import com.br.marketing.check.dto.FileContext;
import com.br.marketing.check.service.Impl.SftpToDbCallingService;
import com.br.marketing.client.SftpClient;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerCallingMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.CallingToDbService;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.SyncConfigService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;


/**
 * @author guangchao.zhang
 * @Classname SftpToDbByCallingJbo
 * @Description 客服拨打明细落库
 * @Date 2022/2/11 2:25 PM
 */
@Component
@Slf4j
public class SftpToDbByCallingJbo extends AbstractSimpleElasticJob {
    @Autowired
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
     SftpToDbCallingService sftpToDbCallingService;

    @Resource
    LocalFileMapper localFileMapper;


    @Resource
    CallingToDbService callingToDbservice;

    @Resource
    CustomerCallingMapper customerCallingMapper;

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    ICompatibleService iCompatibleService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        this.process(getSftpClient(),context);
    }

    /**
     * 主程序处理
     */
    private void process(SftpClient sftpClient,JobExecutionMultipleShardingContext context) {

        List<CustomerCalling> customerCallings = getCustomerCallings();
        log.warn("1用户信息调用开始：{}", customerCallings);
        for (CustomerCalling customerCalling : customerCallings) {
            MarketingCustomerExample customerExample = new MarketingCustomerExample();
            customerExample.createCriteria().andApiCodeEqualTo(customerCalling.getApiCode()).andStatusEqualTo(Byte.valueOf("1"));
            List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
            if(marketingCustomers.size()<=0){
                continue;
            }
            MarketingCustomer marketingCustomer = marketingCustomers.get(0);
            Boolean action = iCompatibleService.isAction(marketingCustomer.getExtendConfigInfo(),context.getJobName());
            if(!action){
                continue;
            }
            Map<String, Set<String>> map = new HashMap<>(16);
            // 文件处理逻辑
            processFile(customerCalling.getSftpPath(), sftpClient, map,context);
            log.warn("分片内容map：{}",map);
            // 数据处理逻辑
            processData(sftpClient, map, customerCalling);

        }
        try {
            sftpClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<CustomerCalling> getCustomerCallings() {
        CustomerCallingExample customerCallingExample = new CustomerCallingExample();
        customerCallingExample.createCriteria().andStatusEqualTo((byte) 1);
        customerCallingExample.createCriteria().andPushTypeEqualTo(0);
        return customerCallingMapper.selectByExample(customerCallingExample);
    }

    /**
     * 获取sftp服务连接
     *
     * @return 返回sftp 对象
     */
    private SftpClient getSftpClient() {
        SftpClient sftpClient = new SftpClient(sftpHost, sftpPort, sftpUsername, sftpPwd);
        try {
            sftpClient.connect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sftpClient;
    }

    /**
     * 文件数据处理
     *
     * @param sftpClient      sftp客户端
     * @param map             文件名称容器
     * @param customerCalling 客户信息
     */
    private void processData(SftpClient sftpClient, Map<String, Set<String>> map, CustomerCalling customerCalling) {
        String columnsDetail = customerCalling.getColumnsDetail();
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            String sftpPathFromMap = entry.getKey();
            Set<String> fileNames = entry.getValue();
            for (String fileName : fileNames) {
                if (fileName.endsWith(".txt")) {
                    FileContext context = new FileContext();
                    LocalFile localFile = getLocalFile(sftpClient, sftpPathFromMap, fileName, context, customerCalling);
                    if (localFile != null) {
                        localFileMapper.insertSelective(localFile);
                        // 数据处理
                        sftpToDbCallingService.actionTxtFile(
                                context,
                                localFile,
                                Arrays.asList(columnsDetail.split(",")),
                                callingToDbservice::execute,
                                sftpClient);
                    }
                }
            }
        }
    }

    /**
     * @param sftpClient      sftp客户端
     * @param sftpPathFromMap sftp 路径
     * @param fileName        文件名称
     * @param context         file上下文
     * @param customerCalling 客户信息
     * @return 本地文件地址信息
     */
    private LocalFile getLocalFile(SftpClient sftpClient, String sftpPathFromMap, String fileName, FileContext context, CustomerCalling customerCalling) {
        context.setBaseFtpClient(sftpClient);
        context.setSftpZipFilePath(sftpPathFromMap);
        context.setApiCode(customerCalling.getApiCode());
        context.setTxtFileName(fileName);
        context.setLocalTxtFilePath(syncConfigService.getPath().concat(customerCalling.getApiCode()).concat("/").concat("marketing-calling").concat("/"));
        if (!sftpToDbCallingService.downLoadFile(context)) {
            return null;
        }
        return localFileInstance(context, customerCalling.getApiCode());
    }

    /**
     * 初始化localFile 对象
     *
     * @param context 文件上下文信息
     * @param apiCode 用户aoiCode
     * @return 本地文件对象
     */
    static LocalFile localFileInstance(FileContext context, String apiCode) {
        LocalFile localFile = new LocalFile();
        localFile.setApiCode(apiCode);
        localFile.setSrcPath(context.getSftpZipFilePath());
        localFile.setFileName(context.getTxtFileName());
        localFile.setLocalPath(context.getLocalTxtFilePath());
        localFile.setStatus("1");
        localFile.setCreateTime(new Date());
        localFile.setFileType("halo");
        return localFile;
    }

    /**
     * 处理文件类型
     *
     * @param sftpPath   sftp 文件地址
     * @param sftpClient sftp 客户端
     * @param map        文件名称容器
     */
    private void processFile(String sftpPath, SftpClient sftpClient, Map<String, Set<String>> map,JobExecutionMultipleShardingContext context) {
        List<Integer> shardingItems = context.getShardingItems();
        try {
            Map<String, SftpATTRS> attrsMap = sftpClient.listFiles(sftpPath);
            for (Map.Entry<String, SftpATTRS> entry : attrsMap.entrySet()) {
                String fileName = entry.getKey();
                if (fileName.endsWith(".success")) {
                    shardingItems.forEach((v)->{
                        String substringName = fileName.substring(fileName.length() - 14, fileName.length() - 12);
                        int i = (Integer.valueOf(substringName)) % 4;
                        if(i==Integer.valueOf(v)){
                            Set<String> set = map.computeIfAbsent(sftpPath, k -> new HashSet<>());
                            set.add(fileName.substring(0, fileName.length() - 8));
                        }
                    });
                }
            }
        } catch (
                Exception e) {
            log.error("遍历sftp文件出错", e);
        }
    }
}
