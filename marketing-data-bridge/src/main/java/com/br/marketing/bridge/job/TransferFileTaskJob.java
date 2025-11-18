package com.br.marketing.bridge.job;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DataTypeEnum;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import com.br.marketing.mapper.SyncLogMapper;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.SftpInnerServiceImpl;
import com.br.marketing.service.Impl.transfertofile.*;
import com.br.marketing.service.TransferToFileByTongChengServiceImpl;
import com.br.marketing.service.ftp.Impl.SftpUploadHandlerServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

@Component
@Slf4j
public class TransferFileTaskJob extends AbstractSimpleElasticJob {
    private static int initCollectionSize = 64;

    @Value("${otherConfig.warning.sftpHost:00}")
    private String sftpHost;

    @Value("${innerSftp.uploadpath:00}")
    private String upLoadPath;

    @Resource
    MarketingCustomerMapper marketingCustomerMapper;
    /*萨摩耶的实现*/
    @Resource
    ITransferToFileService transferToFileBySamoyeServiveImpl;

    /*哈罗的实现*/
    @Resource
    ITransferToFileService transferToFileByHaluoServiceImpl;

    @Resource
    SftpInnerServiceImpl sftpInnerService;

    @Resource
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
     * 数禾促复借转化数据提取
     */
    @Resource
    private TransferToFileByShuHeCuFuJieServiceImpl transferToFileByShuHeCuFuJieService;

    /**
     * 宜信
     */
    @Resource
    private TransferToFileByYiXinRealTimeServiceImpl transferToFileByYiXinRealTimeService;
    /**
     * 宜信转化数据提取V4.0
     */
    @Resource
    private TransferToFileByYiXinV4ServiceImpl transferToFileByYiXinV4Service;
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

    /**
     * 同城新系统
     */
    @Resource
    private TransferToFileByNewTongChengServiceImpl transferToFileByNewTongChengService;

    /**
     * 同城集团
     */
    @Resource
    private TransferToFileByTongChengGroupServiceImpl transferToFileByTongChengGroupService;

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
     * 携程新场景
     */
    @Resource
    private NewTransferToFileByXieChengServiceImpl newTransferToFileByXieChengService;

    /**
     * 携程V2
     */
    @Resource
    private TransferToFileByXieChengTwoServiceImpl transferToFileByXieChengTwoService;

    /**
     * 拍拍贷老客
     */
    @Resource
    private TransferToFileByPPDOldServiceImpl transferToFileByPPDOldService;

    @Resource
    private TransferToFileByYouMeDServiceImpl transferToFileByYouMeDService;

    @Resource
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
     * 海尔新系统
     */
    @Resource
    private TransferToFileByNewHaierServiceImpl transferToFileByNewHaierServicea;

    /**
     * 永辉
     */
    @Resource
    private TransferToFileByYonghuiServiceImpl transferToFileByYonghuiService;

    /**
     * 众邦财富
     */
    @Resource
    private TransferToFileByZhongBangServiceImpl transferToFileByZhongBangService;

    /**
     * 众邦
     */
    @Resource
    private TransferToFileByZhongBangTransferServiceImpl transferToFileByZhongBangTransferService;

    /**
     * 奇富360
     */
    @Resource
    private TransferToFileByQiFuServiceImpl transferToFileByQiFuService;

    /**
     * 奇富360VT
     */
    @Resource
    private TransferToFileByQiFuVtServiceImpl transferToFileByQiFuVTService;

    /**
     * 奇富360Full
     */
    @Resource
    private TransferToFileByQiFuFullServiceImpl transferToFileByQiFuFullService;

    /**
     * 榕树转化提取
     */
    @Resource
    private TransferToFileByRongShuServiceImpl transferToFileByRongShuService;

    /**
     * 奇富360促动支转化提取
     */
    @Resource
    private TransferToFileByCuDongZhiServiceImpl transferToFileByCuDongZhiService;

    /**
     * 苏商自动化回传-3710114
     */
    @Resource
    private TransferToFileBySuShangServiceImpl transferToFileBySuShangService;

    /**
     * 苏商自动化回传-3710114
     */
    @Resource
    private TransferToFileByWbxkServiceImpl transferToFileByWbxkService;


    /**
     * 医时
     */
    @Resource
    private TransferToFileByYiShiServiceImpl transferToFileByYiShiService;

    /**
     * 国美
     */
    @Resource
    private TransferToFileByGuoMeiServiceImpl transferToFileByGuoMeiService;

    /**
     * 58新客-营销名单上报-数据提取
     */
    @Resource
    private TransferToFileByWuBaSubmitDataServiceImpl transferToFileByWuBaSubmitDataService;

    @Resource
    ICompatibleService iCompatibleService;

    @Resource
    SftpUploadHandlerServiceImpl sftpUploadHandlerService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String jobParameter = context.getJobParameter();
        BindApiCodeServiceImplBean bindApiCodeServiceImplBean = bindApiCode();
        Map<String, Set<ITransferToFileService>> bind = bindApiCodeServiceImplBean.bind;
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(Byte.valueOf("1"))
                .andApiCodeIn(new ArrayList<>(bind.keySet()));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
        for (MarketingCustomer marketingCustomer : marketingCustomers) {
            String redisKey = RedisKeyConstant.TRANSFER_FILE_TASK_JOB_KEY.concat(":").concat(marketingCustomer.getApiCode());
            String value = RandomStringUtils.randomAlphabetic(16) + UUID.randomUUID();
            Boolean action = iCompatibleService.isAction(marketingCustomer.getExtendConfigInfo(), context.getJobName());
            if (!action) {
                continue;
            }
            Set<ITransferToFileService> serviceImplSet = bind.get(marketingCustomer.getApiCode());
            for (ITransferToFileService serviceImpl : serviceImplSet) {
                try {
                    //自定义参数传入格式举例 7410785#20220711,true;7412003#123;.....
                    String myParam = serviceImpl.isMyParam(marketingCustomer.getApiCode(), jobParameter);
                    if (StringUtils.isNotBlank(myParam)) {
                        log.warn("apicode={}获取的自定义参数为{}", marketingCustomer.getApiCode(), myParam);
                    }
                    Result<List<TransferFileTask>> listResult = new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Lists.newArrayList());
                    try {
                        boolean lock = redisChgService.lock(redisKey, value,
                                marketingCommonConfig.getTransferFileTaskJobLockExpireTime());
                        if (lock) {
                            listResult = serviceImpl.buildTransferTask(marketingCustomer.getApiCode(), myParam);
                        }
                    } catch (Exception e) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode()
                                , "该apiCode:" + marketingCustomer.getApiCode() + "执行数据提取任务获取锁:" + redisKey + "异常!"), e);
                    } finally {
                        redisChgService.unlock(redisKey, value);
                    }
                    if (ResultCode.SUCCESS.getValue().equals(listResult.getCode()) && listResult.getData().size() > 0) {
                        List<TransferFileTask> data = listResult.getData();
                        for (TransferFileTask datum : data) {
                            Result result = serviceImpl.actionTransferToFile(datum, myParam);
                            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                                String localPath = datum.getFilePath();
                                sftpUploadHandlerService.insertSftpUploadTask(datum.getApiCode(),localPath,datum.getFileName(),
                                        DataTypeEnum.TRANSFER.getValue(),
                                        "update b_transfer_file_task set status = 4 where id ="+datum.getId());
                                    //第一次执行，查询为空，不会进行删除，直接返回
                                    //第二次执行，删除b_sync_log的记录
                                    /*String fileChildDir = "";
                                    if (StringUtils.isNotEmpty(datum.getFileChildDir())) {
                                        fileChildDir =  datum.getFileChildDir().concat("/");
                                    }
                                    String childDir = StringUtils.isNotEmpty(datum.getFileChildDir()) ? fileChildDir : "";
                                    String srcPath = sftpHost.concat(":")
                                            .concat(upLoadPath)
                                            .concat(marketingCustomer.getApiCode())
                                            .concat("/transferOutPut/")
                                            .concat(childDir)
                                            .concat(datum.getStartDate())
                                            .concat("/");
                                    List<SyncLog> syncLogList = loanSyncLogMapper.querySyncLog(
                                            ImmutableMap.of("apiCode", marketingCustomer.getApiCode()
                                                    , "fileName", datum.getFileName()
                                                    , "srcPath", srcPath));
                                    if (!CollectionUtils.isEmpty(syncLogList)) {
                                        if (syncLogList.size() != 1) {
                                            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode()
                                                    , "重新执行数据提取异常，apiCode=" + marketingCustomer.getApiCode()
                                                            + ",fileName=" + datum.getFileName()
                                                            + ",syncLogSize=" + syncLogList.size()));
                                            return;
                                        }
                                        SyncLogExample syncLogExample = new SyncLogExample();
                                        syncLogExample.createCriteria().andApiCodeEqualTo(marketingCustomer.getApiCode())
                                                .andFileNameIn(Lists.newArrayList(datum.getFileName(), datum.getFileName() + ".success"))
                                                .andSrcPathEqualTo(srcPath);
                                        loanSyncLogMapper.deleteByExample(syncLogExample);
                                    }*/
                                }
                            }
                        }

                } catch (Exception ex) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode()
                            , String.format("客户转化文件提取报错：%s,报错信息：%s", marketingCustomer.getApiCode(), ex.getMessage())), ex);
                }
            }
        }
        bind.clear();
    }

    /**
     * 2022-12-24 15:09 apiCode 绑定 业务实现
     */
    private BindApiCodeServiceImplBean bindApiCode() {
        return BindApiCodeServiceImplBean.BindApiCodeServiceImplBeanBuilder.create()
                // 萨摩耶转化数据提取
                .addBind(transferToFileBySamoyeServiveImpl, marketingCommonConfig.getSaMoYeTransferFileApiCodes())
                // 哈啰转化数据提取
                .addBind(transferToFileByHaluoServiceImpl, marketingCommonConfig.getHaLuoTransferFileApiCodes())
                // 数禾转化数据提取
                .addBind(transferToFileByShuHeService,
                        ObjectUtil.isEmpty(marketingCommonConfig.getShuHeTransferExtractApiCodes()) ? null
                                : marketingCommonConfig.getShuHeTransferExtractApiCodes().keySet())
                // 数禾促复借转化数据提取
                .addBind(transferToFileByShuHeCuFuJieService, marketingCommonConfig.getShuHeCuFuJieTransferFileApiCodes())
                // 宜信实时转化数据提取
                .addBind(transferToFileByYiXinRealTimeService, marketingCommonConfig.getYinXinTransferRealTimeApiCodes())
                // 宜信转化数据提取V4.0
                .addBind(transferToFileByYiXinV4Service, marketingCommonConfig.getYinXinTransferV4ApiCodes())
                // 玖富转化数据提取
                .addBind(transferToFileByJiuFuService, marketingCommonConfig.getJiuFuTransferApiCodes())
                // 拍拍贷新客实时转化数据提取
                .addBind(transferToFileByPPDService, marketingCommonConfig.getPPDTransferFileApiCodes())
                // 同程转化数据提取
                .addBind(transferToFileByTongChengService, marketingCommonConfig.getTongChengTransferFileApiCodes())
                // 同程新系统转化数据提取
                .addBind(transferToFileByNewTongChengService, marketingCommonConfig.getNewTongChengTransferFileApiCodes())
                // 同程集团转化数据提取
                .addBind(transferToFileByTongChengGroupService, marketingCommonConfig.getTongChengGroupTransferFileApiCodes())
                // 小赢转化数据提取
                .addBind(xiaoYingRealTimeService, marketingCommonConfig.getXiaoYingTransferExtractApiCodes())
                // 众安异业撞库、转化数据提取
                .addBind(transferToFileByZhongAnService, marketingCommonConfig.getZhongAnTransferApiCodes())
                // 携程转化数据提取
                .addBind(transferToFileByXieChengService, marketingCommonConfig.getXieChengTransferApiCodes())
                // 携程新场景转化数据提取
                .addBind(newTransferToFileByXieChengService, marketingCommonConfig.getXieChengNewTransferApiCodes())
                // 携程V2转化数据提取
                .addBind(transferToFileByXieChengTwoService, marketingCommonConfig.getXieChengTwoTransferApiCodes())
                // 拍拍贷老客转人工数据提取
                .addBind(transferToFileByPPDOldService, marketingCommonConfig.getPPDOldTransferFileApiCodes())
                // 桔子转化数据提取
                .addBind(orangeService, marketingCommonConfig.getOrangeTransferFileApiCodes())
                .addBind(transferToFileByYouMeDService, marketingCommonConfig.getYouMeDApiCodes())
                // 海尔转化数据提取
                .addBind(transferToFileByHaierService, marketingCommonConfig.getHaierApiCodes())
                // 海尔新系统转换数据提取
                .addBind(transferToFileByNewHaierServicea, marketingCommonConfig.getNewHaierTransferApiCodes())
                // 国美转化数据提取
                .addBind(transferToFileByGuoMeiService, marketingCommonConfig.getGomeApiCodes())
                // 永辉转化数据提取
                .addBind(transferToFileByYonghuiService, marketingCommonConfig.getYonghuiTransferExtractApiCodes())
                // 众邦财富转换数据提取
                .addBind(transferToFileByZhongBangService, marketingCommonConfig.getZhongBangTransferApiCodes())
                // 众邦转换数据提取
                .addBind(transferToFileByZhongBangTransferService, marketingCommonConfig.getZhongBangApiCodes())
                // 奇富360转换数据提取
                .addBind(getQifuService(), ObjectUtil.isEmpty(marketingCommonConfig.getQiFuExtDataConfig()) ? null
                        : marketingCommonConfig.getQiFuExtDataConfig().keySet())
                // 滴滴转化数据提取
                .addBind(transferToFileByDiDiService, marketingCommonConfig.getDidiApiCodes())
                // 榕树转化数据提取
                .addBind(transferToFileByRongShuService, marketingCommonConfig.getRongShuTransferApiCodes())
                .addBind(transferToFileByCuDongZhiService, marketingCommonConfig.getCuDongZhiTransferExtractApiCodes())
                .addBind(transferToFileByQiFuFullService, ObjectUtil.isEmpty(marketingCommonConfig.getQiFuFullExtDataConfig()) ? null
                        : marketingCommonConfig.getQiFuFullExtDataConfig().keySet())
                .addBind(transferToFileBySuShangService, marketingCommonConfig.getSuShangTransferExtractApiCodes())
                //医时转换数据提取
                .addBind(transferToFileByYiShiService, marketingCommonConfig.getYiShiTransferApiCodes())
                .addBind(transferToFileByWuBaSubmitDataService, marketingCommonConfig.getWuBaSubmitDataTransferApiCodes())
                .build();

    }

    private ITransferToFileService getQifuService() {
        if (marketingCommonConfig.getQiFuExtDataVTConfig()) {
            return transferToFileByQiFuVTService;
        }
        return transferToFileByQiFuService;
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
