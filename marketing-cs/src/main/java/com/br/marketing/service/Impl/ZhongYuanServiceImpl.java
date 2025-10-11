package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.SyncUserValidityPeriodBOCondition;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataSoleDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.*;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserDataSoleHandler;
import com.br.marketing.strategy.ArtificialTransferSoleHandler;
import com.br.marketing.strategy.CustomerTransferSoleHandler;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import com.google.common.collect.Lists;
import cn.hutool.core.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.ParseException;
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


    /**
     * 转化数据推Daas 情况集合 1，2
     */
    private static final LinkedList<String> CONDITION_LIST = new LinkedList<>();

    static {
        CONDITION_LIST.add("2");
        CONDITION_LIST.add("1");
    }

    private static final List<String> dxUserTypeList = Lists.newArrayList("1","2","3");

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

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    PhoneSaleMapper phoneSaleMapper;

    @Resource
    private ArtificialTransferSoleHandler artificialTransferSoleHandler;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private CustomerTransferSoleHandler customerTransferSoleHandler;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListWithValidityPeriod(String tcId, String apiCode, Long indexId,
                                                                                              String requestStartDate, String requestEndDate) {

        return marketingTransferSyncUserMapper.getZhongYuanTransferByRequestDate(tcId, apiCode, requestStartDate, requestEndDate, indexId);

    }
    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListWithValidityPeriodNoRegisterTime(String tcId, String apiCode, Long indexId,
                                                                                              String requestStartDate, String requestEndDate) {

        return marketingTransferSyncUserMapper.getZhongYuanTransferByRequestDateNoRegisterTime(tcId, apiCode, requestStartDate, requestEndDate, indexId);

    }

    @Override
    public void zhongYuanTransferDataToDaas(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        try {
            // 获取ifLogin 的数据集合
            List<MarketingTransferSyncUser> ifLoginCollectTransferSyncUserList = marketingTransferSyncUserList.stream().
                    filter(m -> "1".equals(m.getIfLogin())).collect(Collectors.toList());
            if(!ifLoginCollectTransferSyncUserList.isEmpty()){
                // 剔除并返回有效期内最新一条的转化数据
                Map<String, SyncUserValidityPeriodBOCondition> periodBOMap = eliminateAndValidity(ifLoginCollectTransferSyncUserList);

                // 推 Daas
                pushTransferDataToDaas(periodBOMap, ifLoginCollectTransferSyncUserList);
            }

        }catch (Exception e){
            log.error("中原转化数据推Daas异常：",e);
        }

    }

    private void pushTransferDataToDaas(Map<String, SyncUserValidityPeriodBOCondition> periodBOMap,
                                        List<MarketingTransferSyncUser> ifLoginCollectTransferSyncUserList) {
        List<RealTimeUserDataSoleDTO> realTimeUserDataSoleDTOS =
                packageRealTimeUserDataSoleDTO(periodBOMap, ifLoginCollectTransferSyncUserList);
        if(!ObjectUtil.isEmpty(realTimeUserDataSoleDTOS)){
            ProcessHandlerContext context = new ProcessHandlerContext();
            context.setApiCode(ifLoginCollectTransferSyncUserList.get(0).getApiCode());
            artificialRealTimeUserDataSoleHandler.call(realTimeUserDataSoleDTOS, context);
        }
    }

    /**
     * 组装推Daas数据逻辑
     * @param periodBOMap 有效期数据封装
     * @param ifLoginCollectTransferSyncUserList iflogin 数据集合
     * @return 返回
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
                            syncUserValidityPeriodBOCondition.getCondition(),syncUserValidityPeriodBOCondition.getDxUserType());

                    // 接口数据封装
                    RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = getRealTimeUserDataSoleDTO(dassSingleImportAdapSoleDTO, phoneSaleExtendInfo);
                    transferData.add(realTimeUserDataSoleDTO);
                }

            }
        }
        return transferData;
    }

    private  RealTimeUserDataSoleDTO getRealTimeUserDataSoleDTO(DassSingleImportAdapSoleDTO dassSingleImportAdapSoleDTO, PhoneSaleExtendInfo phoneSaleExtendInfo) {
        RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = new RealTimeUserDataSoleDTO();
        realTimeUserDataSoleDTO.setDassSingleImportAdapDTO(dassSingleImportAdapSoleDTO);
        realTimeUserDataSoleDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
        realTimeUserDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);

        // 设置去重逻辑 单一cell 7 天内只推送一次
        realTimeUserDataSoleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        realTimeUserDataSoleDTO.setSoleType(marketingCommonConfig.getZhongYuanDaysToSend());
        return realTimeUserDataSoleDTO;
    }

    private DassSingleImportDataDTO packageDassSingleImportData(MarketingTransferSyncUser transfer, MarketingSyncUser marketingSyncUser,
                                                    String dxUserType) {

        String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        //解密失败报警,当前数据不推送
        if (StringUtils.isEmpty(cell)) {
            log.error("数据推电销业务：电话解密失败 cell：{}",marketingSyncUser.getCell());
            return new DassSingleImportDataDTO();
        }

        return getDassSingleImportDataDTO(transfer, dxUserType, cell);
    }

    private  DassSingleImportDataDTO getDassSingleImportDataDTO(MarketingTransferSyncUser transfer, String dxUserType, String phone) {
        // 根据custNum 找到转化数据里最新的一条转化数据  获取里面的 loginTime 和 registerTime。
        String tcId = tableCreateService.getTcId(transfer.getApiCode());
        MarketingTransferSyncUser registerTimeAndLoginTimeByCreateTimeOrderDesc =
                marketingTransferSyncUserMapper.getRegisterTimeAndLoginTimeByCreateTimeOrderDesc(tcId, transfer.getCustNum());

        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();
        dassSingleImportDataDTO.setName("1");
        dassSingleImportDataDTO.setOrgname("zhongyuanxj");
        dassSingleImportDataDTO.setPhone(phone);
        dassSingleImportDataDTO.setUserType(dxUserType);
        dassSingleImportDataDTO.setSource("30");
        dassSingleImportDataDTO.setId(transfer.getId());
        dassSingleImportDataDTO.setUid(transfer.getCustNum());
        dassSingleImportDataDTO.setRegisterTime(formatDate(registerTimeAndLoginTimeByCreateTimeOrderDesc.getRegisterTime()));
        dassSingleImportDataDTO.setLoginTime(formatDate(registerTimeAndLoginTimeByCreateTimeOrderDesc.getLoginTime()));
        return dassSingleImportDataDTO;
    }
    private static String formatDate(String str) {
        return org.springframework.util.StringUtils.isEmpty(str) ? str : str.replace(":000", "");
    }
    /**
     * 电销记录表数据组装
     * @param transfer 转化
     * @param marketingSyncUser 上传
     * @param condition 情况
     * @return 电销数据集
     */
    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transfer,
                                                           MarketingSyncUser marketingSyncUser,
                                                           String condition,String dxUserType) {
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
        phoneSaleExtendInfo.setDxUserType(dxUserType);
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
        return phoneSaleExtendInfo;
    }

    /**
     * 剔除数据
     * @param marketingTransferSyncUserList 转化数据集
     * @return 返回需要推送bo集
     */
    private Map<String, SyncUserValidityPeriodBOCondition> eliminateAndValidity(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        Map<String, SyncUserValidityPeriodBOCondition> filterSyncUserValidityPeriodBOCondition = new HashMap<>();
        for (String userType : CONDITION_LIST) {
            marketingTransferSyncUserList.forEach(item -> item.setUserType(userType));
            // 判断有效期
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodUserTypeBatchFirstVersion(marketingTransferSyncUserList, apiCode, new Date());
            if (!ObjectUtil.isEmpty(periodBOMap)) {
                marketingTransferSyncUserList.forEach(transferSyncUser -> {
                    try{
                        String custNum = transferSyncUser.getCustNum();
                        SyncUserValidityPeriodBO bo = periodBOMap.get(custNum+userType);
                        if (ObjectUtil.isEmpty(bo)) {
                            log.warn("{}:中原转化数据推Daas,type:【{}】,不满足案件编号“有效期内”条件", custNum,userType);
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
                                        // registerTime非空
                                        boolean hasRegisterTime = StringUtils.isNotEmpty(transferSyncUser.getRegisterTime());
                                        if (condition1 && hasRegisterTime) {
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
                    }catch (Exception e) {
                        log.error("中原转化数据推Daas剔除报错:",e);
                    }

                });
            }
        }
        return filterSyncUserValidityPeriodBOCondition;
    }


    private List<MarketingTransferSyncUser> eliminateAndValidityTwo(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        List<MarketingTransferSyncUser> filterSyncUserValidityPeriodBOCondition = new ArrayList<>();
        List<String> userTypes = new ArrayList<>();
        userTypes.add("2");
        for (String userType : userTypes) {
            marketingTransferSyncUserList.forEach(item -> item.setUserType(userType));
            // 判断有效期
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodUserTypeBatchFirstVersion(marketingTransferSyncUserList, apiCode, new Date());
            if (!ObjectUtil.isEmpty(periodBOMap)) {
                marketingTransferSyncUserList.forEach(transferSyncUser -> {
                    try{
                        if("1".equals(transferSyncUser.getIfLogin())) {
                            String custNum = transferSyncUser.getCustNum();
                            SyncUserValidityPeriodBO bo = periodBOMap.get(custNum + userType);
                            if (ObjectUtil.isEmpty(bo)) {
                                log.warn("{}:中原转化数据推Daas,type:【{}】,不满足案件编号“有效期内”条件", custNum, userType);
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
                                        case "2":
                                            Boolean condition2 = zhongYuanConditionMap.get("condition_2");
                                            if (condition2) {
                                                sbo.setCondition("2");
                                                filterSyncUserValidityPeriodBOCondition.add(transferSyncUser);
                                            }
                                            break;
                                    }
                                } else {
                                    log.warn("{}:中原转化数据推Daas满足【isBlack=1 or ifApply=1】条件", transferSyncUser.getCustNum());
                                }
                            }
                        }
                    }catch (Exception e) {
                        log.error("中原转化数据推Daas剔除报错:",e);
                    }

                });
            }
        }
        return filterSyncUserValidityPeriodBOCondition;
    }

    @Override
    public void zhongYuanTransferDataToCustomerFilter(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        try {
            Set<String> collectCustNumSet = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(toSet());
            String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(collectCustNumSet, apiCode, new Date());
            if (!ObjectUtil.isEmpty(periodBOMap)) {
                List<ConversionData> conversionDataList = new ArrayList<>();
                marketingTransferSyncUserList.forEach(transferSyncUser -> {
                    if(StringUtils.isNotBlank(transferSyncUser.getRegisterTime())) {
                        String custNum = transferSyncUser.getCustNum();
                        SyncUserValidityPeriodBO bo = periodBOMap.get(custNum);
                        if (ObjectUtil.isEmpty(bo)) {
                            log.warn("{}:中原转化数据推客服转化不满足案件编号“有效期内”条件", custNum);
                        } else {
                            ConversionData conversionData = packageConversionDataWithTransferData(transferSyncUser, bo);
                            conversionDataList.add(conversionData);
                        }
                    }
                });
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                customerTransferSoleHandler.call(conversionDataList, context);
            }
        }catch (Exception e){
            log.error("中原转化数据推客服转化异常：",e);
        }
    }


    @Override
    public void zhongYuanTransferDataToCustomerFilterRuleFirst(List<MarketingTransferSyncUser> marketingTransferSyncUserList){
        try {
            Set<String> collectCustNumSet = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(toSet());
            String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(collectCustNumSet, apiCode, new Date());
            if (!ObjectUtil.isEmpty(periodBOMap)) {
                List<ConversionData> conversionDataList = new ArrayList<>();
                marketingTransferSyncUserList.forEach(transferSyncUser -> {
                    if(StringUtils.isNotBlank(transferSyncUser.getRegisterTime())) {
                        String custNum = transferSyncUser.getCustNum();
                        SyncUserValidityPeriodBO bo = periodBOMap.get(custNum);
                        if (ObjectUtil.isEmpty(bo)) {
                            log.warn("{}:中原转化数据推客服转化不满足案件编号“有效期内”条件", custNum);
                        } else {
                            ConversionData conversionData = packageConversionDataWithTransferData(transferSyncUser, bo);
                            conversionDataList.add(conversionData);
                        }
                    }
                });
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                customerTransferSoleHandler.call(conversionDataList, context);
            }
        }catch (Exception e){
            log.error("中原转化数据推客服转化异常：",e);
        }
    }
    @Override
    public void zhongYuanTransferDataToCustomerFilterByDaasTwo(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {
        try {

            List<MarketingTransferSyncUser> marketingTransferSyncUserList = eliminateAndValidityTwo(marketingTransferSyncUsers);

            if(marketingTransferSyncUserList == null ||marketingTransferSyncUserList.size()<=0){
                return;
            }

            Set<String> collectCustNumSet = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(toSet());
            String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(collectCustNumSet, apiCode, new Date());
            if (!ObjectUtil.isEmpty(periodBOMap)) {
                List<ConversionData> conversionDataList = new ArrayList<>();
                marketingTransferSyncUserList.forEach(transferSyncUser -> {
                    String custNum = transferSyncUser.getCustNum();
                    SyncUserValidityPeriodBO bo = periodBOMap.get(custNum);
                    if (ObjectUtil.isEmpty(bo)) {
                        log.warn("{}:中原转化数据推客服转化不满足案件编号“有效期内”条件", custNum);
                    } else {
                        ConversionData conversionData = packageConversionDataWithTransferData(transferSyncUser, bo);
                        conversionDataList.add(conversionData);
                    }
                });
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                customerTransferSoleHandler.call(conversionDataList, context);
            }
        }catch (Exception e){
            log.error("中原转化数据推客服转化异常：",e);
        }
    }

    @Override
    public Result pushOutBoundData(Long id) {

        Boolean isContiue = false;
        Boolean actionMark = true;
        Long minId = null;
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }
        String apiCode = localFile.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);

        Integer threadNum = marketingCommonConfig.getZhongYuanTransferPushOutBoundThreadPoolSize();

        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        while (actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            Set<String> custNumSet = new HashSet<>();
            phoneSales.forEach(list -> custNumSet.add(list.getUid()));

            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);
                minId = phoneSale.getId();
                threadPool.execute(() -> {
                    try {
                        Map<String, SyncUserValidityPeriodBO> validityPeriodMap =
                                transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(
                                        custNumSet, apiCode, new Date());

                        List<ConversionData> list = new ArrayList<>();
                        for (DassImportDataDTO transferSyncUser : phoneSales) {
                            SyncUserValidityPeriodBO bo = validityPeriodMap.get(transferSyncUser.getUid());
                            // 有效期判断
                            if (bo == null) {
                                continue;
                            }
                            // 外呼
                            ConversionData conversionData = packageConversionData(transferSyncUser, bo, tcId);
                            list.add(conversionData);
                        }
                        ProcessHandlerContext context = new ProcessHandlerContext();
                        context.setApiCode(apiCode);
                        customerTransferSoleHandler.call(list, context);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
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
        log.warn("中原sftp文件推送外呼流程结束,apiCode: {} ,fileName：{}",localFile.getApiCode(),localFile.getFileName());
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(isContiue);
    }


    /**
     * 组装推送客服转化数据
     */
    private ConversionData packageConversionDataWithTransferData(MarketingTransferSyncUser transferSyncUser
            , SyncUserValidityPeriodBO bo) {
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transferSyncUser.getId().toString());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        conversionData.setCid(transferSyncUser.getCid());
        conversionData.setCaseNum(transferSyncUser.getCustNum());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transferSyncUser.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transferSyncUser.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        conversionData.setInversionStatus("0");
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transferSyncUser, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置
        conversionData.setInitId(transferSyncUser.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        // 有效期设置
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }
    /**
     * 2023-08-28 9:52
     * 组装推送外呼信息
     */
    private ConversionData packageConversionData(DassImportDataDTO dto
            , SyncUserValidityPeriodBO bo, String tcid) {
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(dto.getId().toString());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        conversionData.setCid(tcid);
        conversionData.setCaseNum(dto.getUid());
//        conversionData.setGroupType(dto.getUserType());
        conversionData.setPartnerProcessDate(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        conversionData.setInversionStatus("0");
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(dto, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置
        conversionData.setInitId(dto.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        // 有效期设置
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }



    @Override
    public void zhongYuanPushDaasTransferFirst(String apiCode) {
        log.warn("中原首次推送Daas转化接口任务开始执行");
        Pair<String, String> validityRange =
                validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
        String startDate = validityRange.getKey();
        String endDate = validityRange.getValue();
        if(StringUtils.isEmpty(startDate)||StringUtils.isEmpty(endDate)){
            log.warn("中原获取有效期时间区间为空,请检查");
        }
        String tcId = tableCreateService.getTcId(apiCode);
        Long indexId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getZhongYuanTransferByPage(tcId,apiCode,startDate,endDate,indexId,"if_apply=1");
            if (marketingTransferSyncUserList.isEmpty()){
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            // 判断有效期
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSets, apiCode, new Date());
            List<MarketingSyncUser> marketingSyncUserList = periodBOMap.values().stream().map(SyncUserValidityPeriodBO::getSyncUser).collect(Collectors.toList());
            // 推人工转化
            if(!CollectionUtils.isEmpty(marketingSyncUserList)) {
                pushDaasTransferFirstProcess(marketingSyncUserList);
            }
        }
        indexId = null;
        while (true) {
            List<MarketingTransferSyncUser> marketingTransferSyncUserList = marketingTransferSyncUserMapper.getZhongYuanTransferByPage(tcId,apiCode,null,null,indexId,"reserve_field1->'$.isBlack'=\"1\"");
            if (marketingTransferSyncUserList.isEmpty()){
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            Set<String> custNumSets = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            // 判断有效期
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumSets, apiCode, new Date());
            List<MarketingSyncUser> marketingSyncUserList = periodBOMap.values().stream().map(SyncUserValidityPeriodBO::getSyncUser).collect(Collectors.toList());
            // 推人工转化
            if(!CollectionUtils.isEmpty(marketingSyncUserList)) {
                pushDaasTransferFirstProcess(marketingSyncUserList);
            }
        }
    }


    private void pushDaasTransferFirstProcess(List<MarketingSyncUser> marketingSyncUserList) {
        List<DassAssembleTransferDataSoleDTO> transferData = new ArrayList<>();
        dxUserTypeList.forEach(dxUserType -> {
                    marketingSyncUserList.forEach(marketingSyncUser -> {
                        DassAssembleTransferDataSoleDTO dassAssembleTransferDataSoleDTO = new DassAssembleTransferDataSoleDTO();
                        DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
                        dassTransferDataDTO.setUid(marketingSyncUser.getCustNum());
                        dassTransferDataDTO.setSource("30");
                        dassTransferDataDTO.setUserType(dxUserType);
                        dassTransferDataDTO.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
                        dassTransferDataDTO.setOrgName("zhongyuanxj");
                        dassTransferDataDTO.setIfTransform("1");
                        dassTransferDataDTO.setTransformStatus("4");
                        PhoneSaleTransferInfo phoneSaleTransferInfo = new PhoneSaleTransferInfo();
                        phoneSaleTransferInfo.setApiCode(marketingSyncUser.getApiCode());
                        phoneSaleTransferInfo.setCreateTime(new Date());
                        phoneSaleTransferInfo.setTransformStatus("4");
                        phoneSaleTransferInfo.setCustNum(marketingSyncUser.getCustNum());
                        phoneSaleTransferInfo.setOrgName("zhongyuanxj");
                        phoneSaleTransferInfo.setUserType(dxUserType);
                        phoneSaleTransferInfo.setSourceId(marketingSyncUser.getId());
                        phoneSaleTransferInfo.setAppletDate(LocalDate.now().toString());
                        phoneSaleTransferInfo.setDataType(1);

                        dassAssembleTransferDataSoleDTO.setDassTransferDataDTO(dassTransferDataDTO);
                        dassAssembleTransferDataSoleDTO.setPhoneSaleTransferInfo(phoneSaleTransferInfo);
                        dassAssembleTransferDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);
                        dassAssembleTransferDataSoleDTO.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
                        dassAssembleTransferDataSoleDTO.setStatus(dxUserType);
                        transferData.add(dassAssembleTransferDataSoleDTO);
                    });
                });

        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(marketingSyncUserList.get(0).getApiCode());
        artificialTransferSoleHandler.call(transferData, context);
        log.warn("中原首次推送Daas转化流程结束,推送量num = {}",transferData.size());
    }

    @Override
    public void zhongYuanPushDaasTransfer(String apiCode) {
        log.warn("中原非首次推送Daas转化接口任务开始执行");
        String tcId = tableCreateService.getTcId(apiCode);
        Pair<String, String> validityRange =
                validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
        if(StringUtils.isEmpty(validityRange.getKey())||StringUtils.isEmpty(validityRange.getValue())){
            log.warn("中原获取有效期时间区间为空,请检查");
        }
        String startDate = validityRange.getKey()+" 00:00:00";
        String endDate = validityRange.getValue()+" 23:59:59";
        Date startDateFormat=new Date();
        Date endDateFormat = new Date();
        try {
            startDateFormat = DateUtils.parse(startDate, "yyyy-MM-dd HH:mm:ss");
            endDateFormat = DateUtils.parse(endDate, "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            log.error("格式化日期错误", e);
        }
        int pageSize = 2000;
        int pageNum = 1;
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andDxUserTypeIn(dxUserTypeList)
                .andCreateTimeBetween(startDateFormat, endDateFormat);
        example.setOrderByClause("id asc");
        while (true) {
            List<PhoneSaleExtendInfo> list = phoneSaleExtendInfoMapper.findListPageByExample(
                    example, pageNum, pageSize);
            if(CollectionUtils.isEmpty(list)){
                break;
            }
            pageNum++;
            Map<String,Set<String>> custNumAndDxUserTypeSet =  list.stream().collect(Collectors.groupingBy(PhoneSaleExtendInfo::getCustNum, Collectors.mapping(PhoneSaleExtendInfo::getDxUserType, Collectors.toSet())));

            //剔除custNum
            Map<String,String> filterCustNumMap = filterCustNum(apiCode,tcId,startDate,endDate,custNumAndDxUserTypeSet.keySet());
            //推Daas转化
            if(!CollectionUtils.isEmpty(filterCustNumMap)) {
                pushDaasTransferNoFirstProcess(apiCode, filterCustNumMap, custNumAndDxUserTypeSet);
            }
        }
        pageNum = 1;
        while (true) {
            List<PhoneSale> list = phoneSaleMapper.getZhongYuanSaleByPage(
                    apiCode,dxUserTypeList,startDateFormat,endDateFormat, pageNum, pageSize);
            if(CollectionUtils.isEmpty(list)){
                break;
            }
            pageNum++;
            Map<String,Set<String>> custNumAndDxUserTypeSet =  list.stream().collect(Collectors.groupingBy(PhoneSale::getUid, Collectors.mapping(PhoneSale::getUserType,Collectors.toSet())));
            //剔除custNum
            Map<String,String> filterCustNumMap = filterCustNum(apiCode,tcId,startDate,endDate,custNumAndDxUserTypeSet.keySet());
            //推Daas转化
            if(!CollectionUtils.isEmpty(filterCustNumMap)) {
                pushDaasTransferNoFirstProcess(apiCode, filterCustNumMap, custNumAndDxUserTypeSet);
            }
        }

    }

    private Map<String,String> filterCustNum(String apiCode,String tcId,String startDate,String endDate,Set<String>  custNums) {
        List<MarketingTransferSyncUser> IfApplyTransferData = marketingTransferSyncUserMapper.getZhongYuanTransferBySql(tcId,apiCode,startDate,endDate,"if_apply=1",custNums);

        List<MarketingTransferSyncUser> IsBlackTrasnferData = marketingTransferSyncUserMapper.getZhongYuanTransferBySql(tcId,apiCode,null,null,"reserve_field1->'$.isBlack'=\"1\"",custNums);

        Set<String> IfApplyCustNums= IfApplyTransferData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());

        Set<String> IsBlackCustNums= IsBlackTrasnferData.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        //并集
        IfApplyCustNums.addAll(IsBlackCustNums);
        //交集
        custNums.retainAll(IfApplyCustNums);
        // 判断有效期
        Map<String, SyncUserValidityPeriodBO> periodBOMap =
                transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNums, apiCode, new Date());


        Map<String,String>custNumAndCellMap = periodBOMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,entry->{
                    return entry.getValue().getSyncUser().getCell();
                }
        ));

        return custNumAndCellMap;

    }


    private void pushDaasTransferNoFirstProcess(String apiCode,Map<String,String>filterCustNumMap,Map<String,Set<String>> custNumAndDxUserTypeMap) {
        List<DassAssembleTransferDataSoleDTO> transferData = new ArrayList<>();
        filterCustNumMap.forEach((custNum,cell) -> {
                custNumAndDxUserTypeMap.get(custNum).forEach(dxUserType->{
                DassAssembleTransferDataSoleDTO dassAssembleTransferDataSoleDTO = new DassAssembleTransferDataSoleDTO();
                DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
                dassTransferDataDTO.setUid(custNum);
                dassTransferDataDTO.setSource("30");
                dassTransferDataDTO.setUserType(dxUserType);
                dassTransferDataDTO.setPhone(BrCipherMaker.getInstance().decode(cell));
                dassTransferDataDTO.setOrgName("zhongyuanxj");
                dassTransferDataDTO.setIfTransform("1");
                dassTransferDataDTO.setTransformStatus("4");
                PhoneSaleTransferInfo phoneSaleTransferInfo = new PhoneSaleTransferInfo();
                phoneSaleTransferInfo.setApiCode(apiCode);
                phoneSaleTransferInfo.setCreateTime(new Date());
                phoneSaleTransferInfo.setTransformStatus("4");
                phoneSaleTransferInfo.setCustNum(custNum);
                phoneSaleTransferInfo.setOrgName("zhongyuanxj");
                phoneSaleTransferInfo.setUserType(dxUserType);
                phoneSaleTransferInfo.setAppletDate(LocalDate.now().toString());
                phoneSaleTransferInfo.setDataType(1);
                dassAssembleTransferDataSoleDTO.setDassTransferDataDTO(dassTransferDataDTO);
                dassAssembleTransferDataSoleDTO.setPhoneSaleTransferInfo(phoneSaleTransferInfo);
                dassAssembleTransferDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);
                dassAssembleTransferDataSoleDTO.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
                dassAssembleTransferDataSoleDTO.setStatus(dxUserType);
                transferData.add(dassAssembleTransferDataSoleDTO);
            });
        });

        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        artificialTransferSoleHandler.call(transferData, context);
        log.warn("中原非首次推送Daas转化流程结束,推送量num = {}",transferData.size());
    }

}
