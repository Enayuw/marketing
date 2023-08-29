package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.SyncUserValidityPeriodBOCondition;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.*;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserDataSoleHandler;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * 描述：： 中原接口实现
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYuanServiceImpl
 * @author: it-yml
 * @create: 2023-08-25 19:41
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ZhongYuanServiceImpl implements ZhongYuanService {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;


    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Resource
    private ArtificialRealTimeUserDataSoleHandler artificialRealTimeUserDataSoleHandler;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    RedisChgService redisChgService;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Autowired
    DassServiceClient dassServiceClient;

    @Resource
    RetryMainLogMapper retryMainLogMapper;

    @Resource
    private AlarmApiClient alarmClient;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListWithValidityPeriod(String tcId, String apiCode, Long indexId,
                                                                                              String requestStartDate, String requestEndDate) {

        return marketingTransferSyncUserMapper.getZhongYuanTransferByRequestDate(tcId, apiCode, requestStartDate, requestEndDate, indexId);

    }

    @Override
    public void zhongYuanTransferDataToDaas(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        // 获取ifLogin 的数据集合
        List<MarketingTransferSyncUser> ifLoginCollectTransferSyncUserList = marketingTransferSyncUserList.stream().
                filter(m -> "1".equals(m.getIfLogin())).collect(Collectors.toList());

        // 剔除并返回有效期内最新一条的转化数据
        Map<String, SyncUserValidityPeriodBOCondition> periodBOMap = eliminateAndValidity(apiCode, ifLoginCollectTransferSyncUserList);

        // 推 Daas
        pushTransferDataToDaas(periodBOMap, ifLoginCollectTransferSyncUserList, apiCode);

    }

    private void pushTransferDataToDaas(Map<String, SyncUserValidityPeriodBOCondition> periodBOMap,
                                        List<MarketingTransferSyncUser> ifLoginCollectTransferSyncUserList,
                                        String apiCode) {
        List<RealTimeUserDataSoleDTO> realTimeUserDataSoleDTOS =
                packageRealTimeUserDataSoleDTO(periodBOMap, ifLoginCollectTransferSyncUserList);
        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        artificialRealTimeUserDataSoleHandler.call(realTimeUserDataSoleDTOS, context);
    }

    /**
     * 组装推Daas数据逻辑
     * @param periodBOMap
     * @param ifLoginCollectTransferSyncUserList
     * @return
     */
    private List<RealTimeUserDataSoleDTO> packageRealTimeUserDataSoleDTO(Map<String, SyncUserValidityPeriodBOCondition> periodBOMap,
                                                                         List<MarketingTransferSyncUser> ifLoginCollectTransferSyncUserList) {
        List<RealTimeUserDataSoleDTO> transferData = new ArrayList<>();
        if (!ObjectUtil.isEmpty(periodBOMap)) {
            for (MarketingTransferSyncUser marketingTransferSyncUser : ifLoginCollectTransferSyncUserList) {
                String custNum = marketingTransferSyncUser.getCustNum();
                SyncUserValidityPeriodBOCondition syncUserValidityPeriodBOCondition = periodBOMap.get(custNum);
                if (!ObjectUtil.isEmpty(syncUserValidityPeriodBOCondition)) {

                    MarketingSyncUser marketingSyncUser = syncUserValidityPeriodBOCondition.getSyncUser();


                    // 组装Daas 接口数据单条
                    DassSingleImportDataDTO dassSingleImportDataDTO =
                            packageDassSingleImportData(marketingTransferSyncUser, marketingSyncUser, syncUserValidityPeriodBOCondition.getDxUserType());
                    if(ObjectUtil.isEmpty(dassSingleImportDataDTO)){
                        continue;
                    }
                    DassSingleImportAdapSoleDTO dassSingleImportAdapSoleDTO = new DassSingleImportAdapSoleDTO();
                    dassSingleImportAdapSoleDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);

                    // 组装b_phone_sale_extend_ino 表信息
                    PhoneSaleExtendInfo phoneSaleExtendInfo = packagePhoneSaleExtendInfo(marketingTransferSyncUser, marketingSyncUser,
                            syncUserValidityPeriodBOCondition.getCondition());

                    // 接口数据封装
                    RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = new RealTimeUserDataSoleDTO();
                    realTimeUserDataSoleDTO.setDassSingleImportAdapDTO(dassSingleImportAdapSoleDTO);
                    realTimeUserDataSoleDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
                    realTimeUserDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);

                    // 设置去重逻辑 单一cell 7 天内只推送一次
                    realTimeUserDataSoleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
                    realTimeUserDataSoleDTO.setSoleType(7);
                    transferData.add(realTimeUserDataSoleDTO);
                }

            }
        }
        return transferData;
    }
    private DassSingleImportDataDTO packageDassSingleImportData(MarketingTransferSyncUser transfer, MarketingSyncUser marketingSyncUser,
                                                    String dxUserType) {
        // 根据custNum 找到转化数据里最新的一条转化数据  获取里面的 loginTime 和 registerTime。
        String tcId = tableCreateService.getTcId(transfer.getApiCode());
        MarketingTransferSyncUser registerTimeAndLoginTimeByCreateTimeOrderDesc =
                marketingTransferSyncUserMapper.getRegisterTimeAndLoginTimeByCreateTimeOrderDesc(tcId, transfer.getCustNum());
        String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        //解密失败报警,当前数据不推送
        if (StringUtils.isEmpty(cell)) {
            log.error("数据推电销业务：电话解密失败 cell：{}",marketingSyncUser.getCell());
            return new DassSingleImportDataDTO();
        }
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();
        dassSingleImportDataDTO.setName("1");
        dassSingleImportDataDTO.setOrgname("zhongyuanxj");
        dassSingleImportDataDTO.setPhone(phone);
        dassSingleImportDataDTO.setUserType(dxUserType);
        dassSingleImportDataDTO.setSource("30");
        dassSingleImportDataDTO.setId(transfer.getId());
        dassSingleImportDataDTO.setUid(transfer.getCustNum());
        dassSingleImportDataDTO.setRegisterTime(registerTimeAndLoginTimeByCreateTimeOrderDesc.getRegisterTime());
        dassSingleImportDataDTO.setLoginTime(registerTimeAndLoginTimeByCreateTimeOrderDesc.getLoginTime());

        return dassSingleImportDataDTO;
    }

    /**
     * 电销记录表数据组装
     * @param transfer
     * @param marketingSyncUser
     * @param condition
     * @return
     */
    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transfer,
                                                           MarketingSyncUser marketingSyncUser,
                                                           String condition) {
        LocalDateTime localDateTime = transfer.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
        phoneSaleExtendInfo.setUserType(marketingSyncUser.getUserType());
        phoneSaleExtendInfo.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        phoneSaleExtendInfo.setAppletTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setType(transfer.getType());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        phoneSaleExtendInfo.setStatus(condition);
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
        return phoneSaleExtendInfo;
    }

    /**
     * 剔除数据
     * @param apiCode
     * @param marketingTransferSyncUserList
     * @return
     */
    private Map<String, SyncUserValidityPeriodBOCondition> eliminateAndValidity(String apiCode,
                                                                                List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        // 获取custNum 集合
        Set<String> custNumCollect = marketingTransferSyncUserList.stream().map(m -> m.getCustNum()).collect(Collectors.toSet());

        Map<String, SyncUserValidityPeriodBOCondition> filterSyncUserValidityPeriodBOCondition = new HashMap<>();
        // 判断有效期
        Map<String, SyncUserValidityPeriodBO> periodBOMap =
                transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumCollect, apiCode, new Date());
        if (!ObjectUtil.isEmpty(periodBOMap)) {
            marketingTransferSyncUserList.forEach(transferSyncUser -> {
                String custNum = transferSyncUser.getCustNum();
                SyncUserValidityPeriodBO bo = periodBOMap.get(custNum);
                if (ObjectUtil.isEmpty(bo)) {
                    log.warn("{}:中原转化数据推Daas不满足案件编号“有效期内”条件", custNum);
                } else {
                    Boolean ifApplyOrIsBlack = validityPeriodDataService.judgmentMarketingTransferDataInvalidWithValidityPeriod(apiCode,
                            transferSyncUser.getCustNum());
                    // ifApply =1 and isBlack =1 剔除
                    if (!ifApplyOrIsBlack) {
                        MarketingSyncUser syncUser = bo.getSyncUser();
                        SyncUserValidityPeriodBOCondition sbo = new SyncUserValidityPeriodBOCondition();
                        BeanUtils.copyProperties(bo, sbo);
                        // 电销userType = 1
                        sbo.setDxUserType("1");
                        Map<String, Boolean> zhongYuanConditionMap = marketingCommonConfig.getZhongYuanConditionMap();
                        switch (syncUser.getUserType()) {
                            case "1":
                                // 判断开关是否推送
                                Boolean condition1 = zhongYuanConditionMap.get("condition_1");
                                if (condition1) {
                                    sbo.setCondition("1");
                                    filterSyncUserValidityPeriodBOCondition.put(custNum, sbo);
                                }
                                break;
                            case "2":
                                Boolean condition2 = zhongYuanConditionMap.get("condition_2");
                                if (condition2) {
                                    sbo.setCondition("2");
                                    filterSyncUserValidityPeriodBOCondition.put(custNum, sbo);
                                }
                                break;
                        }
                    } else {
                        log.warn("{}:中原转化数据推Daas满足【isBlack=1 or ifApply=1】条件", transferSyncUser.getCustNum());
                    }
                }
            });
        }
        return filterSyncUserValidityPeriodBOCondition;
    }

    @Override
    public void zhongYuanTransferDataToCustomerFilter(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {

    }

    @Override
    public Result pushOutBoundData(Long id) {

        Boolean isContiue = false;
        Boolean actionMark = true;
        Long minId = null;
        Integer threadNum = 5;

        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }

        localFile.setPushStartTime(new Date());
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);

        Integer number = 0;
        while (actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            number += phoneSales.size();
            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                minId = phoneSale.getId();
                threadPool.submit(() -> {


                });
            } else {
                actionMark = false;
            }

        }

        threadPool.shutdown();
        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }

        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(number);
        localFileMapper.updateByPrimaryKeySelective(localFile);
        if (SftpFileTypeEnum.DX.getValue().equals(localFile.getFileType())) {
            StringBuilder content = new StringBuilder();
            content.append("apiCode：".concat(localFile.getApiCode()).concat("\r\n"))
                    .append("fileName：".concat(localFile.getFileName()).concat("\r\n"))
                    .append("数量：".concat(number.toString()).concat("\r\n"))
                    .append("文件推送dass结束".concat("\r\n"));
            alarmClient.sendAlarm(content.toString(), "Dass结果文件推送", AlarmSendCodeEnum.SUCCESS_UPLOAD.getCode());
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }

}
