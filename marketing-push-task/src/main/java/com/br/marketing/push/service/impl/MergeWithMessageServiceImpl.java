package com.br.marketing.push.service.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.br.marketing.client.FtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.client.bi.BiApiClient;
import com.br.marketing.client.bi.input.OffLineScoreDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TaskExtendExtendFieldDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IProductResultSimpleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
@Slf4j
public class MergeWithMessageServiceImpl {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    LoanFileMapper loanFileMapper;

    @Autowired
    MergeServiceImpl mergeService;

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Resource
    CustomerMapper customerMapper;

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @Autowired
    BiApiClient biApiClient;

    @Value("${otherConfig.warning.ftpBasePath:00}")
    private String ftpBasePath;
    @Value("${otherConfig.warning.ftpHost:00}")
    private String ftpHost;
    @Value("${otherConfig.warning.ftpPort:00}")
    private Integer ftpPort;
    @Value("${otherConfig.warning.ftpUser:00}")
    private String ftpUsername;
    @Value("${otherConfig.warning.ftpPwd:00}")
    private String ftpPwd;
    /**
     * 消费文件合并信息
     * @param fileId
     * @return
     */
    public Result<Boolean> consumerFileMsg(Long fileId){
        Boolean res = Boolean.FALSE;
        StraHisFile straHisFile = straHisFileMapper.selectByPrimaryKey(fileId);
        LoanFile loanFile = new LoanFile();
        if(straHisFile != null&& straHisFile.getStatus() ==4){
            try {
                Customer customer = customerMapper.getCustomerByApiCode(straHisFile.getApiCode());

                BeanUtils.copyProperties(straHisFile, loanFile);
                loanFile.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(straHisFile.getCreateTime()));
                String s = mergeService.mergeResultFile(loanFile, customer);
                if(StringUtils.isNotBlank(s)){
                    BeanUtils.copyProperties(loanFile, straHisFile);
                    straHisFile.setStatus(5);
                    straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
                }

            }catch (Exception ex){
                log.error(ex.getMessage(),ex);
            }
        }
        if(straHisFile != null&& straHisFile.getStatus() ==5){
            straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
            pushToFtp(loanFile);
            BeanUtils.copyProperties(loanFile, straHisFile);
            straHisFile.setStatus(6);
            straHisFileMapper.updateByPrimaryKeySelective(straHisFile);
            reqOffLine(straHisFile);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(res);
    }


    public void pushToFtp(LoanFile blf){
        String apiCode=blf.getApiCode();
        FtpClient ftpClient = new FtpClient(ftpHost,ftpPort,ftpUsername,ftpPwd,ftpBasePath);
        try {
            ftpClient.connect();
            String remotePath=ftpBasePath.concat(apiCode).concat("/output/").concat(DateHelper.getDateAddYyMmDd(0));
            String filePathAndName=blf.getFilePath().concat("/").concat(blf.getFileName());
            File file = new File(filePathAndName);
            if(file.exists()){
                log.warn("push txt to sftp :{}",blf.getFileName());
                ftpClient.uploadFile(Files.newInputStream(Paths.get(filePathAndName)), remotePath, blf.getFileName());
                String sueccessFilePath = filePathAndName.concat(".success");
                File successFile = new File(sueccessFilePath);
                if(!successFile.exists()){
                    successFile.createNewFile();
                }
                ftpClient.uploadFile(Files.newInputStream(Paths.get(sueccessFilePath)), remotePath, blf.getFileName().concat(".success"));
            }
            blf.setInnerFtpPath(remotePath);
        } catch (Exception e) {
            log.error("Exception",e);
        }finally {
            try {
                ftpClient.disconnect();
            } catch (Exception e) {
                log.error("Exception",e);
            }
        }
    }

    private void reqOffLine(StraHisFile file){
        String batchNumber = file.getBatchNumber();
        MarketingTask task = marketingTaskMapper.getByBatchNumber(batchNumber);
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andTaskIdEqualTo(task.getId());
        List<MarketingTaskExtend> marketingTaskExtends = marketingTaskExtendMapper.selectByExample(extendExample);
        MarketingTaskExtend taskExtend = marketingTaskExtends.get(0);
        Integer encodeType = 1;
        if(StringUtils.isNotBlank(taskExtend.getExtendConfigInfo())){
            TaskExtendExtendFieldDTO taskExtendExtendFieldDTO = JSON.parseObject(taskExtend.getExtendConfigInfo(), TaskExtendExtendFieldDTO.class);
            encodeType = taskExtendExtendFieldDTO.getThreekEncryptType()!=null?taskExtendExtendFieldDTO.getThreekEncryptType():1;
        }

        StringBuilder head = new StringBuilder();
        task.setIsOnline(1);
        iProductResultSimpleService.initHead(head,",",task);
        OffLineScoreDTO scoreDTO = new OffLineScoreDTO();
        scoreDTO.setRequestId(file.getId().toString());
        scoreDTO.setProductInfo(JSONArray.parseArray(task.getProductInfo()));
        scoreDTO.setHeadInfo(head.toString());
        scoreDTO.setFilePath(file.getInnerFtpPath());
        scoreDTO.setFileName(file.getFileName());
        scoreDTO.setEncodeType(encodeType.toString());
        biApiClient.reqOffLineJob(scoreDTO);
    }
}
