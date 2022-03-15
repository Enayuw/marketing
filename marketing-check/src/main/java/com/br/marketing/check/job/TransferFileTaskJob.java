package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.RetryMainLog;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.SftpInnerServiceImpl;
import com.br.marketing.service.Impl.TransferToFileByShuHeServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class TransferFileTaskJob extends AbstractSimpleElasticJob {


    @Autowired
    MarketingCustomerMapper customerMapper;

    @Autowired
    TransferFileTaskMapper transferFileTaskMapper;

    /*萨摩耶的实现*/
    @Autowired
    ITransferToFileService transferToFileBySamoyeServiveImpl;

    @Autowired
    SftpInnerServiceImpl sftpInnerService;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    RetryMainLogMapper retryMainLogMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferToFileByShuHeServiceImpl transferToFileByShuHeService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = customerMapper.selectByExample(customerExample);
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            ITransferToFileService serviceImpl = getServiceImpl(marketingCustomer);
            if (serviceImpl == null) {
                continue;
            }
            Result<List<TransferFileTask>> listResult = serviceImpl.buildTransferTask(marketingCustomer.getApiCode());
            if (ResultCode.SUCCESS.getValue().equals(listResult.getCode()) && listResult.getData().size() > 0) {
                List<TransferFileTask> data = listResult.getData();
                for (TransferFileTask datum : data) {
                    Result result = serviceImpl.actionTransferToFile(datum);
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        Result res = sftpInnerService.pushInnerSftp(datum);
                        if (!ResultCode.SUCCESS.getValue().equals(res.getCode())) {
                            RetryMainLog retryMainLog = new RetryMainLog();
                            retryMainLog.setRetryType(1);
                            retryMainLog.setRetryParam(JSON.toJSONString(datum));
                            retryMainLog.setRetryParamType(datum.getClass().getName());
                            retryMainLog.setRetryService("sftpInnerServiceImpl");
                            retryMainLog.setRetryMethod("pushInnerSftp");
                            retryMainLog.setRetryNum(0);
                            retryMainLog.setRetryMaxNum(3);
                            retryMainLog.setRetryStatus(1);
                            retryMainLog.setCreateTime(new Date());
                            retryMainLog.setIncrId(redisChgService.incr(RedisKeyConstant.retryid));
                            retryMainLogMapper.insertSelective(retryMainLog);
                        }
                    }
                }
            }
        }
    }

    ITransferToFileService getServiceImpl(MarketingCustomer customer) {
        if (customer.getShortName().contains("萨摩耶")) {
            return transferToFileBySamoyeServiveImpl;
        } else if (marketingCommonConfig.getShuHeTransferExtractApiCodes().contains(customer.getApiCode())) {
            return transferToFileByShuHeService;
        } else {
            return null;
        }
    }


}
