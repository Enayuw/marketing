package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.ShuHeTransferService;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.ShuheBlackPhoneTransferDataDTO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleTransferInfo;
import com.br.marketing.enums.PhoneSaleTransferDataTypeEnum;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IShuheBlackPhoneRecordService;
import com.br.marketing.service.PhoneSaleTransferInfoService;
import com.br.marketing.strategy.ArtificialShuHeBlackPushTransferHandler;
import com.br.marketing.strategy.ArtificialTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @Author: lizhen
 * @Time: 2022/05/29 10:06
 * @Description: 数禾转化service
 */
@Service
@Slf4j
public class ShuHeTransferServiceImpl implements ShuHeTransferService {

    @Resource
    private CaseShuheUserMapper caseShuheUserMapper;
    @Resource
    private IShuheBlackPhoneRecordService iShuheBlackPhoneRecordService;
    @Autowired
    private ArtificialShuHeBlackPushTransferHandler artificialShuHeBlackPushTransferHandler;
    @Resource
    private ArtificialTransferHandler artificialTransferHandler;
    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private DataLoadingHandlerService handlerService;
    @Resource
    private PhoneSaleTransferInfoService phoneSaleTransferInfoService;

    final static DateTimeFormatter yyyyMMddDF = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final DateTimeFormatter isoDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static ThreadPoolExecutor POOL = BrExecutors.getThreadPool(2, 3);

    @Override
    public void pushBlackDataToDaas() {
        String endDay = LocalDate.now().format(yyyyMMddDF);
        String StartDay = LocalDate.parse(endDay, yyyyMMddDF).minusDays(30L).format(yyyyMMddDF);
        List<ShuheBlackPhoneTransferDataDTO> shuheBlackPhoneTransferDataDTOList = new ArrayList<>();
        List<CaseShuheUser> blackPhoneDataList = new ArrayList<>();
        //is_black为Y
        List<CaseShuheUser> blackCaseUserList = caseShuheUserMapper.selectIsBlackData(StartDay, endDay);
        blackPhoneDataList.addAll(blackCaseUserList);
        Set<String> blackMap = blackCaseUserList.parallelStream().map(CaseShuheUser::getMobile).collect(Collectors.toSet());
        Set<String> rrtEndMap = new HashSet<>();
        //clc_usr_max_dx_rrt_end>当前日期
        Boolean mark = Boolean.TRUE;
        Integer page = 0;
        while (mark) {
            List<CaseShuheUser> rrtOrderCaseUserList = caseShuheUserMapper.selectOrderRrtEndData(page * 2000);
            if (CollectionUtils.isEmpty(rrtOrderCaseUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            page++;
            for (CaseShuheUser rrtOrderCaseUser : rrtOrderCaseUserList) {
                LocalDate rrtEndDate;
                try {
                    rrtEndDate = LocalDate.parse(rrtOrderCaseUser.getClcUsrMaxDxRrtEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e) {
                    rrtEndDate = LocalDateTime.parse(rrtOrderCaseUser.getClcUsrMaxDxRrtEnd(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate();
                }
                if (rrtEndDate.isBefore(LocalDate.now())) {
                    rrtEndMap.add(rrtOrderCaseUser.getMobile());
                    continue;
                }
                //最新的一条>=当前日期，并且不在isBlack=Y中，推送
                if (rrtEndMap.add(rrtOrderCaseUser.getMobile()) && blackMap.add(rrtOrderCaseUser.getMobile())) {
                    blackPhoneDataList.add(rrtOrderCaseUser);
                }
            }
        }
        LocalDate todayDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalDate();
        //根据数禾黑名单推电销记录表去重
        //封装调用Daas接口参数
        blackPhoneDataList.forEach(blackCaseUser -> {
            ShuheBlackPhoneTransferDataDTO shuheBlackPhoneTransferDataDTO = new ShuheBlackPhoneTransferDataDTO();
            if (!iShuheBlackPhoneRecordService.isRepeatPhone(blackCaseUser.getCell(), todayDate.toString())) {
                shuheBlackPhoneTransferDataDTO.setPhone(blackCaseUser.getMobile());
                shuheBlackPhoneTransferDataDTO.setApiCode(blackCaseUser.getApiCode());
                shuheBlackPhoneTransferDataDTO.setPushDate(todayDate.toString());
                shuheBlackPhoneTransferDataDTO.setCustNum(blackCaseUser.getCustNum());
                shuheBlackPhoneTransferDataDTOList.add(shuheBlackPhoneTransferDataDTO);
            }
            ;
        });
        log.warn("数禾黑名单数据推电销转化接口,pushNum ={}", shuheBlackPhoneTransferDataDTOList.size());
        //调用电销接口
        artificialShuHeBlackPushTransferHandler.call(shuheBlackPhoneTransferDataDTOList, null);

    }

    @Override
    public void invalidDataFilterToDaasTransfer(Map<String, Map<String, String>> typeMap) {
        Set<Map.Entry<String, Map<String, String>>> entries = typeMap.entrySet();
        int pageNum = 1;
        List<Callable<List<DassAssembleTransferDataDTO>>> list = new ArrayList<>();
        try {
            for (Map.Entry<String, Map<String, String>> entry : entries) {
                String userType = entry.getKey();
                Map<String, String> value = entry.getValue();
                Integer day = handlerService.getShuHePeriodOfValidityDay(userType);
                LocalDateTime endDateTimeT = LocalDateTime.of(LocalDate.now()
                        , LocalTime.parse(value.getOrDefault("time"
                                , "20:00:00"))).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime startDateTimeMinusOneT = endDateTimeT.minusDays(1);
                LocalDateTime endDateTimeMinusOneT = endDateTimeT.toLocalDate().atStartOfDay().atZone(
                        ZoneId.systemDefault()).toLocalDateTime();
                String endDateTimeStrT = endDateTimeT.format(isoDateTime);
                String startDateTimeMinusOneStrT = startDateTimeMinusOneT.format(isoDateTime);
                String endDateTimeMinusOneStrT = endDateTimeMinusOneT.format(isoDateTime);
                LocalDateTime startDateTimeT = endDateTimeT.minusDays(day);
                String startDateTimeStrT = startDateTimeT.format(isoDateTime);
                list.add(() -> invalidDataFilter(userType, value, startDateTimeStrT, endDateTimeStrT, pageNum, null));
                list.add(() -> invalidDataFilter(userType, value, startDateTimeMinusOneStrT, endDateTimeMinusOneStrT
                        , pageNum, endDateTimeMinusOneStrT));
                List<Future<List<DassAssembleTransferDataDTO>>> futures = POOL.invokeAll(list
                        , 3, TimeUnit.MINUTES);
                List<DassAssembleTransferDataDTO> dtoList = futures.get(0).get(5, TimeUnit.SECONDS);
                dtoList.addAll(futures.get(1).get(5, TimeUnit.SECONDS));
                artificialTransferHandler.call(dtoList, null);
                list.clear();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } finally {
            list.clear();
        }
    }

    /**
     * 2022/7/14 13:14
     * 失效数据过滤
     *
     * @param userType            userType
     * @param value               userType信息
     * @param startDateTimeStrT   开始时间
     * @param endDateTimeStrT     结束时间
     * @param pageNum             页号
     * @param syncUserDateTimeEnd 上传信息截止时间
     */
    private List<DassAssembleTransferDataDTO> invalidDataFilter(String userType
            , Map<String, String> value
            , String startDateTimeStrT
            , String endDateTimeStrT
            , int pageNum
            , String syncUserDateTimeEnd) {
        String apiCode = value.get("apiCode");
        String orgName = value.get("orgname");
        List<PhoneSaleExtendInfo> listPage = phoneSaleExtendInfoMapper.findPushPhoneSaleListPage(
                apiCode, userType, startDateTimeStrT, endDateTimeStrT, pageNum, 1000);
        List<DassAssembleTransferDataDTO> transferData = new ArrayList<>();
        if (CollectionUtils.isEmpty(listPage)) {
            return transferData;
        }
        Set<String> custNums = listPage.parallelStream().map(
                PhoneSaleExtendInfo::getCustNum).collect(Collectors.toSet());
        Map<String, Date> custNumMap = iMarketingSyncUserService.getSyncUserTimeMaxByCustNumsMap(apiCode
                , custNums, userType, syncUserDateTimeEnd);
        PhoneSaleTransferInfo phoneSale = new PhoneSaleTransferInfo();
        phoneSale.setTransformStatus("1");
        phoneSale.setDataType(PhoneSaleTransferDataTypeEnum.INVALID_DATA_FILTER.getValue());
        phoneSale.setApiCode(apiCode);
        phoneSale.setUserType(userType);
        List<String> cusaNumList = phoneSaleTransferInfoService.findCusaNumList(custNums, phoneSale);
        List<PhoneSaleTransferInfo> phoneSaleList = new ArrayList<>();
        for (PhoneSaleExtendInfo info : listPage) {
            String custNum = info.getCustNum();
            if (isLastDayValidity(userType, info.getCreateTime(), custNumMap.getOrDefault(custNum, null))) {
                if (cusaNumList.contains(custNum)) {
                    continue;
                }
                DassAssembleTransferDataDTO dto = new DassAssembleTransferDataDTO();
                transferData.add(dto);
                DassTransferDataDTO dataDTO = new DassTransferDataDTO();
                dataDTO.setUid(custNum);
                dataDTO.setOrgName(orgName);
                dataDTO.setTransformStatus("1");
                dto.setDassTransferDataDTO(dataDTO);
                PhoneSaleTransferInfo phoneSaleTransferInfo = new PhoneSaleTransferInfo();
                phoneSaleTransferInfo.setSourceId(info.getId());
                phoneSaleTransferInfo.setAppletDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                phoneSaleTransferInfo.setCreateTime(new Date());
                phoneSaleTransferInfo.setApiCode(apiCode);
                phoneSaleTransferInfo.setTransformStatus(dataDTO.getTransformStatus());
                phoneSaleTransferInfo.setDataType(PhoneSaleTransferDataTypeEnum.INVALID_DATA_FILTER.getValue());
                phoneSaleTransferInfo.setCusaNum(custNum);
                phoneSaleTransferInfo.setUserType(userType);
                phoneSaleList.add(phoneSaleTransferInfo);
            }
        }
        phoneSaleTransferInfoService.insertSelectiveBatch(phoneSaleList);
        transferData.addAll(invalidDataFilter(apiCode, value, startDateTimeStrT, endDateTimeStrT, ++pageNum
                , syncUserDateTimeEnd));
        return transferData;
    }

    /**
     * 2022/7/14 14:34
     * 是否是有效期最后一天
     *
     * @param userType     userType
     * @param pCreateTime  推送电销创建时间
     * @param validityDate 有效期时间
     * @return true or false
     */
    private boolean isLastDayValidity(String userType, Date pCreateTime, Date validityDate) {
        if (ObjectUtils.isEmpty(pCreateTime) || ObjectUtils.isEmpty(validityDate) || StringUtils.isBlank(userType)) {
            return false;
        }
        Integer day;
        try {
            day = handlerService.getShuHePeriodOfValidityDay(userType);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        LocalDate creatDate = validityDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        final LocalDate lastDate;
        if (day == null) {
            lastDate = creatDate.with(TemporalAdjusters.lastDayOfMonth());
        } else if (day <= 0) {
            lastDate = creatDate;
        } else {
            lastDate = creatDate.plusDays(day);
        }
        LocalDate pCreateDate = pCreateTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return lastDate.isEqual(pCreateDate);
    }
}


