package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.*;
import com.br.marketing.service.TransferToFileByTongChengServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class TransferFileTaskJob extends AbstractSimpleElasticJob {


    @Autowired
    MarketingCustomerMapper customerMapper;

    @Resource
    TransferFileTaskMapper transferFileTaskMapper;

    /*萨摩耶的实现*/
    @Resource
    ITransferToFileService transferToFileBySamoyeServiveImpl;

    /*哈罗的实现*/
    @Resource
    ITransferToFileService transferToFileByHaluoServiceImpl;

    @Autowired
    SftpInnerServiceImpl sftpInnerService;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    RetryMainLogMapper retryMainLogMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferToFileByShuHeServiceImpl transferToFileByShuHeService;

    @Resource
    private TransferToFileByYiXinRealTimeServiceImpl transferToFileByYiXinRealTimeService;

    @Resource
    private TransferToFileByJiuFuServiceImpl transferToFileByJiuFuService;

    @Resource
    private TransferToFileByTongChengServiceImpl transferToFileByTongChengService;

    @Resource
    private SyncLogMapper loanSyncLogMapper;
    @Resource
    private TransferToFileByXiaoYingRealTimeServiceImpl xiaoYingRealTimeService;
    @Resource
    private TransferToFileByPPDServiceImpl transferToFileByPPDService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"));
        List<MarketingCustomer> marketingCustomers = customerMapper.selectByExample(customerExample);
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            try {
                ITransferToFileService serviceImpl = getServiceImpl(marketingCustomer);
                if (serviceImpl == null) {
                    continue;
                }
                Result<List<TransferFileTask>> listResult = serviceImpl.buildTransferTask(marketingCustomer.getApiCode());
                if (ResultCode.SUCCESS.getValue().equals(listResult.getCode()) && listResult.getData().size() > 0) {
                    List<TransferFileTask> data = listResult.getData();
                    for (TransferFileTask datum : data) {
                        //自定义参数传入格式举例 7410785#20220711,true;7412003#123;.....
                        String myParam = serviceImpl.isMyParam(datum.getApiCode(), jobParameter);
                        log.warn("apicode={}获取的自定义参数为{}",datum.getApiCode(),myParam);
                        Result result = serviceImpl.actionTransferToFile(datum, myParam);
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
                            } else {
                                //第一次执行，查询为空，不会进行删除，直接返回
                                //第二次执行，删除b_sync_log的记录
                                List<SyncLog> syncLogList = loanSyncLogMapper.querySyncLog(ImmutableMap.of("apiCode", marketingCustomer.getApiCode(), "fileName", datum.getFileName()));
                                if (!CollectionUtils.isEmpty(syncLogList)) {
                                    if (syncLogList.size() != 1) {
                                        log.warn("重新执行数据提取异常，apiCode={},fileName={},syncLogSize={}", marketingCustomer.getApiCode(), datum.getFileName(), syncLogList.size());
                                        return;
                                    }
                                    SyncLogExample syncLogExample = new SyncLogExample();
                                    syncLogExample.createCriteria().andApiCodeEqualTo(marketingCustomer.getApiCode())
                                            .andFileNameIn(Lists.newArrayList(datum.getFileName(), datum.getFileName() + ".success"));
                                    loanSyncLogMapper.deleteByExample(syncLogExample);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                log.error(String.format("客户转化文件提取报错：%s,报错信息：%s", marketingCustomer.getApiCode(), ex.getMessage()), ex);
            }
        }
    }


    ITransferToFileService getServiceImpl(MarketingCustomer customer) {
        if (marketingCommonConfig.getSaMoYeTransferFileApiCodes().contains(customer.getApiCode())) {
            return transferToFileBySamoyeServiveImpl;
        } else if (marketingCommonConfig.getHaLuoTransferFileApiCodes().contains(customer.getApiCode())) {
            return transferToFileByHaluoServiceImpl;
        } else if (marketingCommonConfig.getShuHeTransferExtractApiCodes().containsKey(customer.getApiCode())) {
            return transferToFileByShuHeService;
        } else if (marketingCommonConfig.getYinXinTransferRealTimeApiCodes().contains(customer.getApiCode())) {
            return transferToFileByYiXinRealTimeService;
        }
        if (marketingCommonConfig.getJiuFuTransferApiCodes().contains(customer.getApiCode())) {
            return transferToFileByJiuFuService;
        }
        if (marketingCommonConfig.getPPDTransferFileApiCodes().contains(customer.getApiCode())) {
            return transferToFileByPPDService;
        }
        if (marketingCommonConfig.getTongChengTransferFileApiCodes().contains(customer.getApiCode())) {
            return transferToFileByTongChengService;
        }
        if (marketingCommonConfig.getXiaoYingTransferExtractApiCodes().contains(customer.getApiCode())) {
            return xiaoYingRealTimeService;
        } else {
            return null;
        }
    }


}
