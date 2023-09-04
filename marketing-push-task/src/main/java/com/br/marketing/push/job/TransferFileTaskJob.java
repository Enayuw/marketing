package com.br.marketing.push.job;

import cn.hutool.core.util.ObjectUtil;
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
import com.br.marketing.service.Impl.SftpInnerServiceImpl;
import com.br.marketing.service.Impl.transfertofile.*;
import com.br.marketing.service.TransferToFileByTongChengServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class TransferFileTaskJob extends AbstractSimpleElasticJob {
    private static int initCollectionSize = 64;

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

    /**
     * 数禾
     */
    @Resource
    private TransferToFileByShuHeServiceImpl transferToFileByShuHeService;

    /**
     * 宜信
     */
    @Resource
    private TransferToFileByYiXinRealTimeServiceImpl transferToFileByYiXinRealTimeService;
    /**
     * 玖富
     */
    @Resource
    private TransferToFileByJiuFuServiceImpl transferToFileByJiuFuService;
    /**
     * 同城
     */
    @Resource
    private TransferToFileByTongChengServiceImpl transferToFileByTongChengService;

    @Resource
    private SyncLogMapper loanSyncLogMapper;
    /**
     * 小赢
     */
    @Resource
    private TransferToFileByXiaoYingRealTimeServiceImpl xiaoYingRealTimeService;
    /**
     * 拍拍贷
     */
    @Resource
    private TransferToFileByPPDServiceImpl transferToFileByPPDService;
    /**
     * 众安
     */
    @Resource
    private TransferToFileByZhongAnServiceImpl transferToFileByZhongAnService;
    /**
     * 携程
     */
    @Resource
    private TransferToFileByXieChengServiceImpl transferToFileByXieChengService;
    /**
     * 拍拍贷老客
     */
    @Resource
    private TransferToFileByPPDOldServiceImpl transferToFileByPPDOldService;

    @Autowired
    private TransferToFileByYouMeDServiceImpl transferToFileByYouMeDService;
    @Autowired
    private TransferToFileByGomeServiceImpl transferToFileByGomeService;

    @Autowired
    private TransferToFileByDiDiServiceImpl transferToFileByDiDiService;

    /**
     * 桔子
     */
    @Resource
    private TransferToFileByOrangeServiceImpl orangeService;

    /**
     * 海尔
     */
    @Resource
    private TransferToFileByHaierServiceImpl transferToFileByHaierService;


    /**
     * 永辉
     */
    @Resource
    private TransferToFileByYonghuiServiceImpl transferToFileByYonghuiService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        BindApiCodeServiceImplBean bindApiCodeServiceImplBean = bindApiCode();
        Map<String, Set<ITransferToFileService>> bind = bindApiCodeServiceImplBean.bind;
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"))
                .andApiCodeIn(new ArrayList<>(bind.keySet()));
        List<MarketingCustomer> marketingCustomers = customerMapper.selectByExampleAndShard(customerExample
                , context.getShardingTotalCount()
                , context.getShardingItems());
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            Set<ITransferToFileService> serviceImplSet = bind.get(marketingCustomer.getApiCode());
            for (ITransferToFileService serviceImpl : serviceImplSet) {
                try {
                    //自定义参数传入格式举例 7410785#20220711,true;7412003#123;.....
                    String myParam = serviceImpl.isMyParam(marketingCustomer.getApiCode(), jobParameter);
                    if (StringUtils.isNotBlank(myParam)) {
                        log.warn("apicode={}获取的自定义参数为{}", marketingCustomer.getApiCode(), myParam);
                    }
                    Result<List<TransferFileTask>> listResult = serviceImpl.buildTransferTask(marketingCustomer.getApiCode(), myParam);
                    if (ResultCode.SUCCESS.getValue().equals(listResult.getCode()) && listResult.getData().size() > 0) {
                        List<TransferFileTask> data = listResult.getData();
                        for (TransferFileTask datum : data) {
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
        bind.clear();
    }

    /**
     * 2022-12-24 15:09
     * apiCode 绑定 业务实现
     */
    private BindApiCodeServiceImplBean bindApiCode() {
        return BindApiCodeServiceImplBean.BindApiCodeServiceImplBeanBuilder.create()
                // 萨摩耶转化数据提取
                .addBind(transferToFileBySamoyeServiveImpl, marketingCommonConfig.getSaMoYeTransferFileApiCodes())
                // 哈啰转化数据提取
                .addBind(transferToFileByHaluoServiceImpl, marketingCommonConfig.getHaLuoTransferFileApiCodes())
                // 数禾转化数据提取
                .addBind(transferToFileByShuHeService, ObjectUtil.isEmpty(
                        marketingCommonConfig.getShuHeTransferExtractApiCodes())
                        ? null : marketingCommonConfig.getShuHeTransferExtractApiCodes().keySet())
                // 宜信实时转化数据提取
                .addBind(transferToFileByYiXinRealTimeService, marketingCommonConfig.getYinXinTransferRealTimeApiCodes())
                // 玖富转化数据提取
                .addBind(transferToFileByJiuFuService, marketingCommonConfig.getJiuFuTransferApiCodes())
                // 拍拍贷新客实时转化数据提取
                .addBind(transferToFileByPPDService, marketingCommonConfig.getPPDTransferFileApiCodes())
                // 同程转化数据提取
                .addBind(transferToFileByTongChengService, marketingCommonConfig.getTongChengTransferFileApiCodes())
                // 小赢转化数据提取
                .addBind(xiaoYingRealTimeService, marketingCommonConfig.getXiaoYingTransferExtractApiCodes())
                // 众安异业撞库、转化数据提取
                .addBind(transferToFileByZhongAnService, marketingCommonConfig.getZhongAnTransferApiCodes())
                // 携程转化数据提取
                .addBind(transferToFileByXieChengService, marketingCommonConfig.getXieChengTransferApiCodes())
                // 拍拍贷老客转人工数据提取
                .addBind(transferToFileByPPDOldService, marketingCommonConfig.getPPDOldTransferFileApiCodes())
                // 桔子转化数据提取
                .addBind(orangeService, marketingCommonConfig.getOrangeTransferFileApiCodes())
                .addBind(transferToFileByYouMeDService, marketingCommonConfig.getYouMeDApiCodes())
                // 海尔转化数据提取
                .addBind(transferToFileByHaierService, marketingCommonConfig.getHaierApiCodes())
                // 国美转化数据提取
                .addBind(transferToFileByGomeService, marketingCommonConfig.getGomeApiCodes())
                // 永辉转化数据提取
                .addBind(transferToFileByYonghuiService, marketingCommonConfig.getYonghuiTransferExtractApiCodes())
                // 滴滴转化数据提取
                .addBind(transferToFileByDiDiService,marketingCommonConfig.getDidiApiCodes())
                .build();
    }

    /**
     * 2022-12-24 17:15
     * 已弃用，最好不要用，用了也不会起作用
     * ，如果非要用，需要修改主业务逻辑（👆{@link TransferFileTaskJob#process(JobExecutionMultipleShardingContext)}）的内容。
     * <p>
     * 新方法{@link TransferFileTaskJob#bindApiCode()}
     */
    @Deprecated
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
        }
        if (marketingCommonConfig.getZhongAnTransferApiCodes().contains(customer.getApiCode())) {
            return transferToFileByZhongAnService;
        }
        if (marketingCommonConfig.getYouMeDApiCodes().contains(customer.getApiCode())) {
            return transferToFileByYouMeDService;
        }
        if (marketingCommonConfig.getXieChengTransferApiCodes().contains(customer.getApiCode())) {
            return transferToFileByXieChengService;
        }
        else {
            return null;
        }
    }


    private static class BindApiCodeServiceImplBean {
        Map<String, Set<ITransferToFileService>> bind;

        private BindApiCodeServiceImplBean(Map<String, Set<ITransferToFileService>> bind) {
            this.bind = bind;
        }

        private static class BindApiCodeServiceImplBeanBuilder {
            private LinkedList<Collection<String>> apiCodesLinkedList;
            private LinkedList<ITransferToFileService> serviceLinkedList;

            private static BindApiCodeServiceImplBeanBuilder create() {
                return new BindApiCodeServiceImplBeanBuilder();
            }

            private BindApiCodeServiceImplBeanBuilder addBind(ITransferToFileService service
                    , Collection<String> apiCodes) {
                if (service != null && apiCodes != null) {
                    if (this.serviceLinkedList == null) {
                        this.serviceLinkedList = new LinkedList<>();
                    }
                    serviceLinkedList.add(service);
                    if (this.apiCodesLinkedList == null) {
                        this.apiCodesLinkedList = new LinkedList<>();
                    }
                    apiCodesLinkedList.add(apiCodes);
                }
                return this;
            }

            private BindApiCodeServiceImplBean build() {
                if (this.serviceLinkedList == null) {
                    this.serviceLinkedList = new LinkedList<>();
                }
                if (this.apiCodesLinkedList == null) {
                    this.apiCodesLinkedList = new LinkedList<>();
                }
                int size = apiCodesLinkedList.size();
                Map<String, Set<ITransferToFileService>> bind = new HashMap<>(initCollectionSize);
                for (int i = 0; i < size; i++) {
                    Collection<String> apiCodes = apiCodesLinkedList.get(i);
                    ITransferToFileService iTransferToFileService = serviceLinkedList.get(i);
                    for (String apiCode : apiCodes) {
                        if (bind.containsKey(apiCode)) {
                            Set<ITransferToFileService> serviceSet = bind.get(apiCode);
                            serviceSet.add(iTransferToFileService);
                        } else {
                            HashSet<ITransferToFileService> serviceSet = new HashSet<>(initCollectionSize);
                            serviceSet.add(iTransferToFileService);
                            bind.put(apiCode, serviceSet);
                        }
                    }
                }
                return new BindApiCodeServiceImplBean(bind);
            }
        }
    }


}
