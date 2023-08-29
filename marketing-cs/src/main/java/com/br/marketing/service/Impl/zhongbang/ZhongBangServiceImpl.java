package com.br.marketing.service.Impl.zhongbang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.DaasAndConversionData;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserAndCustomerTransferSoleFacade;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 业务逻辑
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-25 18:37
 */
@Service
@Slf4j
public class ZhongBangServiceImpl implements ZhongBangService {


    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private PhoneSaleExtendServiceImpl phoneSaleExtendService;

    @Resource
    private ArtificialRealTimeUserAndCustomerTransferSoleFacade artificialRealTimeUserAndCustomerTransferSoleFacade;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");


    @Override
    public void pushTransferToDaasRealTimeUserOneAndCustomer(String apiCode, ThreadPoolExecutor threadPool, String... dateTimeStr) {
        boolean bool = dateTimeStr.length > 1;
        int day = marketingCommonConfig.getZhongbangCellDistributeDay() - 1;
        String tcId = tableCreateService.getTcId(apiCode);
        LinkedHashMap<String, JSONObject> statusTypeMap = marketingCommonConfig.getZhongbangStatusTypeMap();
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.settCid(tcId);
        LocalDate now = LocalDate.now();
        LocalDate yesterdayDate = now.minusDays(1);
        String yesterdayStartTime = yesterdayDate.atStartOfDay().format(DATE_TIME_FORMATTER);
        String yesterdayEndTime = yesterdayDate.atTime(23, 59, 59, 999999999)
                .format(DATE_TIME_FORMATTER);
        String switchKey = "switch";
        String groupNoKey = "groupNo";
        String dxUserTypeKey = "dxUserType";
        statusTypeMap.forEach((k, v) -> {
            if (v.getBooleanValue(switchKey)) {
                switch (k) {
                    case "d":
                        example.clear();
                        MarketingTransferSyncUserExample.Criteria criteriaD = example.createCriteria();
                        if (bool) {
                            criteriaD.andRequestTimeBetween(dateTimeStr[0], dateTimeStr[1]);
                        } else {
                            criteriaD.andRequestDataEqualTo(dateTimeStr[0]);
                        }
                        example.setOrderByClause(" create_time,id limit 2000");
                        criteriaD.andApplyResultEqualTo("1")
                                .andApplyTimeBetween(yesterdayStartTime, yesterdayEndTime)
                                .andIfLentNotEqualTo("1").andApiCodeEqualTo(apiCode);
                        markPackagePushDaas(example, threadPool, apiCode, k, v, groupNoKey, dxUserTypeKey, day);
                        break;
                    case "c":
                        example.clear();
                        example.setOrderByClause(" create_time,id limit 2000");
                        MarketingTransferSyncUserExample.Criteria criteriaC = example.createCriteria();
                        if (bool) {
                            criteriaC.andRequestTimeBetween(dateTimeStr[0], dateTimeStr[1]);
                        } else {
                            criteriaC.andRequestDataEqualTo(dateTimeStr[0]);
                        }
                        criteriaC.andIfLoginEqualTo("1")
                                .andIfApplyNotEqualTo("1")
                                .andLoginTimeBetween(yesterdayStartTime, yesterdayEndTime)
                                .andApiCodeEqualTo(apiCode);
                        markPackagePushDaas(example, threadPool, apiCode, k, v, groupNoKey, dxUserTypeKey, day);
                        break;
                    default:
                }
            }
        });
    }

    /**
     * 2023-08-28 9:51
     * 组装daas记录信息
     */
    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transferSyncUser
            , MarketingSyncUser syncUser, String status, String dxUserType, int groupNo) {
        PhoneSaleExtendInfo info = new PhoneSaleExtendInfo();
        info.setApiCode(transferSyncUser.getApiCode());
        info.setCustNum(transferSyncUser.getCustNum());
        info.setAppletDate(transferSyncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        info.setAppletTime(transferSyncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(DATE_TIME_FORMATTER));
        info.setTaskId(syncUser.getCusBatch());
        info.setStatus(status);
        info.setPStatus(1);
        info.setCreateTime(new Date());
        info.setUpdateTime(info.getCreateTime());
        info.setType(transferSyncUser.getType());
        info.setPushDxTime(new Date());
        info.setSourceId(transferSyncUser.getId());
        info.setCell(syncUser.getCell());
        info.setDxUserType(dxUserType);
        info.setGroupNo(groupNo);
        info.setUserType(transferSyncUser.getUserType());
        return info;
    }

    /**
     * 2023-08-28 9:52
     * 组装推送daas信息
     */
    private DassSingleImportDataDTO packageDassSingleImportDataDTO(MarketingTransferSyncUser transferSyncUser
            , MarketingSyncUser syncUser, String dxUserType, Map<String, MarketingTransferSyncUser> newTransferSyncUserMap) {
        String phone = AESUtil.aesEncrypty(BrCipherMaker.getInstance().decode(
                syncUser.getCell()), aesKey);
        DassSingleImportDataDTO singleImportDataDTO = new DassSingleImportDataDTO();
        String reserveField1 = syncUser.getReserveField1();
        String firstName = null;
        if (StringUtils.isNotBlank(reserveField1)) {
            try {
                JSONObject jsonObject = JSONObject.parseObject(reserveField1);
                firstName = jsonObject.getString("firstName");
                if (StringUtils.isNotBlank(firstName)) {
                    firstName = firstName.replaceAll("\\*", "");
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        MarketingTransferSyncUser newSyncUser = newTransferSyncUserMap.get(transferSyncUser.getCustNum());
        if (newSyncUser == null) {
            newSyncUser = transferSyncUser;
        }
        singleImportDataDTO.setName(firstName);
        singleImportDataDTO.setOrgname("zhongbang");
        singleImportDataDTO.setPhone(phone);
        singleImportDataDTO.setUserType(dxUserType);
        singleImportDataDTO.setSource("33");
        singleImportDataDTO.setUid(transferSyncUser.getCustNum());
        singleImportDataDTO.setId(transferSyncUser.getId());
        singleImportDataDTO.setRegisterTime(replaceZero(newSyncUser.getRegisterTime(), transferSyncUser.getRegisterTime()));
        singleImportDataDTO.setLoginTime(replaceZero(newSyncUser.getLoginTime(), transferSyncUser.getLoginTime()));
        singleImportDataDTO.setAuditTime(replaceZero(newSyncUser.getAuditTime(), transferSyncUser.getAuditTime()));
        singleImportDataDTO.setAuditAmount(newSyncUser.getAuditAmount());
        String idCard = BrCipherMaker.getInstance().decode(syncUser.getIdCard());
        if (StringUtils.isNotBlank(idCard)) {
            int gender;
            int idCardLen = 18;
            int length = idCard.length();
            if (length == idCardLen) {
                gender = Integer.parseInt(idCard.substring(16, 17));
            } else {
                gender = Integer.parseInt(idCard.substring(length - 1, length - 1));
            }
            singleImportDataDTO.setGender((gender % 2 == 0) ? "女" : "男");
        }
        return singleImportDataDTO;
    }

    /**
     * 2023-08-29 9:31
     * 替换0
     */
    private String replaceZero(String s1, String s2) {
        return StringUtils.isBlank(s1) ? (StringUtils.isBlank(s2) ? s2 : s2.replace(":000", ""))
                : s1.replace(":000", "");
    }

    /**
     * 2023-08-28 9:52
     * 组装推送daas信息
     */
    private ConversionData packageConversionData(MarketingTransferSyncUser transferSyncUser
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
     * 2023-08-28 9:47
     * 过滤数据
     * 推送daas及外呼
     */
    private void markPackagePushDaas(MarketingTransferSyncUserExample example
            , ThreadPoolExecutor threadPool
            , String apiCode
            , String status
            , JSONObject v
            , String groupNoKey
            , String dxUserTypeKey
            , int day) {
        for (; ; ) {
            List<MarketingTransferSyncUser> dList = marketingTransferSyncUserMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(dList)) {
                break;
            }
            Set<String> custNumSet = dList.stream().map(MarketingTransferSyncUser::getCustNum)
                    .collect(Collectors.toSet());
            threadPool.execute(() -> {
                Map<String, SyncUserValidityPeriodBO> validityPeriodMap =
                        transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(
                                custNumSet, apiCode, new Date());
                Set<String> cellSet = validityPeriodMap.values().stream().map(
                        m -> m.getSyncUser().getCell()).collect(Collectors.toSet());
                int groupNo = v.getIntValue(groupNoKey);
                Set<String> newCellSet = phoneSaleExtendService.groupRule(apiCode, day, cellSet
                        , groupNo);
                List<MarketingTransferSyncUser> newTransferSyncUser = marketingTransferSyncUserMapper
                        .getTransferByCustNumOrderDatatikv_(dList.get(0).gettCid(), new ArrayList<>(custNumSet));
                Map<String, MarketingTransferSyncUser> newTransferSyncUserMap = newTransferSyncUser.stream().collect(Collectors.toMap(
                        MarketingTransferSyncUser::getCustNum, Function.identity(), (v1, v2) -> v2));
                List<DaasAndConversionData> list = new ArrayList<>();
                for (MarketingTransferSyncUser transferSyncUser : dList) {
                    SyncUserValidityPeriodBO bo = validityPeriodMap.get(transferSyncUser.getCustNum());
                    // 有效期判断
                    if (bo == null) {
                        continue;
                    }
                    // 最新的上传数据
                    MarketingSyncUser syncUser = bo.getSyncUser();
                    String cell = syncUser.getCell();
                    if (!newCellSet.contains(cell)) {
                        continue;
                    }
                    DassSingleImportAdapSoleDTO soleDTO = new DassSingleImportAdapSoleDTO();
                    String dxUserType = v.getString(dxUserTypeKey);
                    // 电销本地推送记录
                    PhoneSaleExtendInfo info = packagePhoneSaleExtendInfo(
                            transferSyncUser, syncUser, status, dxUserType, groupNo);
                    // 电销
                    DassSingleImportDataDTO dassImportDataDTO = packageDassSingleImportDataDTO(
                            transferSyncUser, syncUser, dxUserType, newTransferSyncUserMap);
                    soleDTO.setDassSingleImportDataDTO(dassImportDataDTO);
                    // 外呼
                    ConversionData conversionData = packageConversionData(transferSyncUser, bo);
                    RealTimeUserDataSoleDTO dto = new RealTimeUserDataSoleDTO();
                    dto.setPhoneSaleExtendInfo(info);
                    dto.setDassSingleImportAdapDTO(soleDTO);
                    dto.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.TRANSFER);
                    DaasAndConversionData data = new DaasAndConversionData();
                    data.setConversionData(conversionData);
                    data.setRealTimeUserDataSoleDTO(dto);
                    list.add(data);
                }
                ProcessHandlerContext context = new ProcessHandlerContext();
                context.setApiCode(apiCode);
                artificialRealTimeUserAndCustomerTransferSoleFacade.call(list, context);
            });
            if (dList.size() < 2000) {
                break;
            }
        }
        int activeCount = 0;
        do {
            try {
                activeCount = threadPool.getActiveCount();
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {
            }
        } while (activeCount != 0);
    }
}
