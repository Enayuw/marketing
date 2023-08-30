package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
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
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.*;
import com.br.marketing.common.utils.AESUtil;
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
import com.br.marketing.strategy.CustomerTransferSoleHandler;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.runtime.directive.Foreach;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        CONDITION_LIST.add("1");
        CONDITION_LIST.add("2");
    }


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
    private AlarmApiClient alarmClient;


    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private CustomerTransferSoleHandler customerTransferSoleHandler;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListWithValidityPeriod(String tcId, String apiCode, Long indexId,
                                                                                              String requestStartDate, String requestEndDate) {

        return marketingTransferSyncUserMapper.getZhongYuanTransferByRequestDate(tcId, apiCode, requestStartDate, requestEndDate, indexId);

    }

    @Override
    public void zhongYuanTransferDataToDaas(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        // 获取ifLogin 的数据集合
        List<MarketingTransferSyncUser> ifLoginCollectTransferSyncUserList = marketingTransferSyncUserList.stream().
                filter(m -> "1".equals(m.getIfLogin())).collect(Collectors.toList());

        // 剔除并返回有效期内最新一条的转化数据
        Map<String, SyncUserValidityPeriodBOCondition> periodBOMap = eliminateAndValidity(ifLoginCollectTransferSyncUserList);

        // 推 Daas
        pushTransferDataToDaas(periodBOMap, ifLoginCollectTransferSyncUserList);

    }

    private void pushTransferDataToDaas(Map<String, SyncUserValidityPeriodBOCondition> periodBOMap,
                                        List<MarketingTransferSyncUser> ifLoginCollectTransferSyncUserList) {
        List<RealTimeUserDataSoleDTO> realTimeUserDataSoleDTOS =
                packageRealTimeUserDataSoleDTO(periodBOMap, ifLoginCollectTransferSyncUserList);
        if (!ObjectUtil.isEmpty(realTimeUserDataSoleDTOS)) {
            ProcessHandlerContext context = new ProcessHandlerContext();
            context.setApiCode(ifLoginCollectTransferSyncUserList.get(0).getApiCode());
            artificialRealTimeUserDataSoleHandler.call(realTimeUserDataSoleDTOS, context);
        }
    }

    /**
     * 组装推Daas数据逻辑
     *
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
                    if (ObjectUtil.isEmpty(dassSingleImportDataDTO)) {
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
//        String tcId = tableCreateService.getTcId(transfer.getApiCode());
//        MarketingTransferSyncUser registerTimeAndLoginTimeByCreateTimeOrderDesc =
//                marketingTransferSyncUserMapper.getRegisterTimeAndLoginTimeByCreateTimeOrderDesc(tcId, transfer.getCustNum());
        String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        //解密失败报警,当前数据不推送
        if (StringUtils.isEmpty(cell)) {
            log.error("数据推电销业务：电话解密失败 cell：{}", marketingSyncUser.getCell());
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
        dassSingleImportDataDTO.setRegisterTime(transfer.getRegisterTime());
        dassSingleImportDataDTO.setLoginTime(transfer.getLoginTime());

        return dassSingleImportDataDTO;
    }

    /**
     * 电销记录表数据组装
     *
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
     *
     * @param marketingTransferSyncUserList
     * @return
     */
    private Map<String, SyncUserValidityPeriodBOCondition> eliminateAndValidity(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        Map<String, SyncUserValidityPeriodBOCondition> filterSyncUserValidityPeriodBOCondition = new HashMap<>();
        for (int i = 0; i < CONDITION_LIST.size(); i++) {
            String userType = CONDITION_LIST.get(i);
            marketingTransferSyncUserList.forEach(item -> item.setUserType(userType));
            // 判断有效期
            Map<String, SyncUserValidityPeriodBO> periodBOMap =
                    transferDataValidityPeriodService.getValidityPeriodUserTypeBatchFirstVersion(marketingTransferSyncUserList, apiCode, new Date());
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
        }


        return filterSyncUserValidityPeriodBOCondition;
    }

    @Override
    public void zhongYuanTransferDataToCustomerFilter(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        Set<String> collectCustNumSet = marketingTransferSyncUserList.stream().map(item -> item.getCustNum()).collect(toSet());
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        Map<String, SyncUserValidityPeriodBO> periodBOMap =
                transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(collectCustNumSet, apiCode, new Date());
        if (!ObjectUtil.isEmpty(periodBOMap)) {
            List<ConversionData> conversionDataList = new ArrayList<>();
            marketingTransferSyncUserList.forEach(transferSyncUser -> {
                String custNum = transferSyncUser.getCustNum();
                SyncUserValidityPeriodBO bo = periodBOMap.get(custNum);
                if (ObjectUtil.isEmpty(bo)) {
                    log.warn("{}:中原转化数据推Daas不满足案件编号“有效期内”条件", custNum);
                } else {
                    ConversionData conversionData = packageConversionDataWithTransferData(transferSyncUser, bo);
                    conversionDataList.add(conversionData);
                }
            });
            ProcessHandlerContext context = new ProcessHandlerContext();
            context.setApiCode(apiCode);
            customerTransferSoleHandler.call(conversionDataList, context);
        }

    }

    @Override
    public Result pushOutBoundData(Long id) {

        Boolean isContiue = false;
        Boolean actionMark = true;
        LocalDate now = LocalDate.now();
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        String apiCode = localFile.getApiCode();
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.settCid(tcId);
        example.createCriteria().andApiCodeEqualTo(apiCode).andRequestDataEqualTo(now.toString());

        Long minId = null;
        Integer threadNum = marketingCommonConfig.getZhongYuanTransferPushOutBoundThreadPoolSize();

        if (localFile == null) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("文件不存在").setDate(isContiue);
        }
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum);
        Integer number = 0;
        example.setOrderByClause(" create_time,id limit 1000");
        while (actionMark) {
            List<DassImportDataDTO> phoneSales = phoneSaleMapper.getPushDassData(id, minId);
            Set<String> cellSet = new HashSet<>();
            phoneSales.forEach(list -> cellSet.add(list.getPhone()));
            if (org.springframework.util.CollectionUtils.isEmpty(phoneSales)) {
                break;
            }
            updatePoolSize(threadPool);
            localFile.setPushStartTime(new Date());

            number += phoneSales.size();
            if (phoneSales.size() > 0) {
                DassImportDataDTO phoneSale = phoneSales.get(phoneSales.size() - 1);

                minId = phoneSale.getId();
                threadPool.execute(() -> {
                    Map<String, SyncUserValidityPeriodBO> validityPeriodMap =
                            transferDataValidityPeriodService.getValidityPeriodCellBatchFirstVersion(
                                    cellSet, apiCode, new Date());

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
                    if (LocalTime.now().isAfter(LocalTime.parse("10:00:00"))) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_COMMON.getCode()
                                , "众邦转化数据推送到daas(单条)与外呼，推送时间已过“10点”,但任务会继续...,apiCode:" + apiCode
                                , "众邦转化数据推送daas(单条)与外呼告警"));
                    }
                });
            } else {
                actionMark = false;
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

            List<MarketingTransferSyncUserExample.Criteria> oredCriteria = example.getOredCriteria();
            for (MarketingTransferSyncUserExample.Criteria criteria1 : oredCriteria) {
                List<MarketingTransferSyncUserExample.Criterion> criteria2 = criteria1.getCriteria();
                criteria2.removeIf(criterion -> "id >".equals(criterion.getCondition()));
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

    /**
     * 2023-08-29 14:08
     * 动态调整线程大小
     */
    private void updatePoolSize(ThreadPoolExecutor threadPool) {
        int poolSize = marketingCommonConfig.getZhongYuanTransferPushOutBoundThreadPoolSize();
        int corePoolSize = threadPool.getCorePoolSize();
        if (corePoolSize != poolSize || threadPool.getMaximumPoolSize() != poolSize) {
            threadPool.setMaximumPoolSize(poolSize);
            threadPool.setCorePoolSize(poolSize);
        }
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
        conversionData.setGroupType(transferSyncUser.getUserType());
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
     * 组装推送daas信息
     */
    private ConversionData packageConversionData(DassImportDataDTO dto
            , SyncUserValidityPeriodBO bo, String tcid) {
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(dto.getId().toString());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        conversionData.setCid(tcid);
        conversionData.setCaseNum(dto.getUid());
        conversionData.setGroupType(dto.getUserType());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(dto.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(dto.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
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


}
