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
    private final static DateTimeFormatter yyyy_MM_ddDF = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter isoDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static ThreadPoolExecutor POOL = BrExecutors.getThreadPool(2, 3);

    @Override
    public void pushBlackDataToDaas() {
        String endDay = LocalDate.now().plusDays(1L).format(yyyyMMddDF);
        String endDay_ = LocalDate.now().format(yyyy_MM_ddDF);
        String StartDay = LocalDate.parse(endDay, yyyyMMddDF).minusDays(30L).format(yyyyMMddDF);
        List<CaseShuheUser> blackPhoneDataList = new ArrayList<>();
        //is_black为Y
        //获取时间范围 大于等于29天前 小于明天
        List<CaseShuheUser> blackCaseUserList = caseShuheUserMapper.selectIsBlackData(StartDay, endDay);
        blackPhoneDataList.addAll(blackCaseUserList);
        Set<String> blackMap = blackCaseUserList.parallelStream().map(CaseShuheUser::getMobile).collect(Collectors.toSet());
        Set<String> rrtEndMap = new HashSet<>();
        //clc_usr_max_dx_rrt_end>当前日期
        Boolean mark = Boolean.TRUE;
        Long minId = null;
        Integer pushNum = 0;
        while (mark) {
            List<CaseShuheUser> rrtOrderCaseUserList = caseShuheUserMapper.selectOrderRrtEndData(minId, endDay_);
            if (CollectionUtils.isEmpty(rrtOrderCaseUserList)) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = rrtOrderCaseUserList.get(rrtOrderCaseUserList.size() - 1).getId() + 1;
            List<ShuheBlackPhoneTransferDataDTO> shuheBlackPhoneTransferDataDTOList = new ArrayList<>();
            rrtOrderCaseUserList.forEach(t -> {
                if (rrtEndMap.add(t.getMobile()) && blackMap.add(t.getMobile())) {
                    //根据数禾黑名单推电销记录表去重
                    if (!iShuheBlackPhoneRecordService.isRepeatPhone(t.getCell(), endDay_)) {
                        //封装调用Daas接口参数
                        ShuheBlackPhoneTransferDataDTO shuheBlackPhoneTransferDataDTO = new ShuheBlackPhoneTransferDataDTO();
                        shuheBlackPhoneTransferDataDTO.setPhone(t.getMobile());
                        shuheBlackPhoneTransferDataDTO.setApiCode(t.getApiCode());
                        shuheBlackPhoneTransferDataDTO.setPushDate(endDay_);
                        shuheBlackPhoneTransferDataDTO.setCustNum(t.getCustNum());
                        shuheBlackPhoneTransferDataDTOList.add(shuheBlackPhoneTransferDataDTO);
                    }
                }
            });
            pushNum += shuheBlackPhoneTransferDataDTOList.size();
            //调用电销接口
            artificialShuHeBlackPushTransferHandler.call(shuheBlackPhoneTransferDataDTOList, null);
        }
        log.warn("数禾黑名单数据推电销转化接口,pushNum ={}", pushNum);
    }

    @Override
    public void invalidDataFilterToDaasTransfer(Map<String, Map<String, String>> typeMap) {
        Set<Map.Entry<String, Map<String, String>>> entries = typeMap.entrySet();
        int pageNum = 1;
        List<Callable<List<DassAssembleTransferDataDTO>>> list = new ArrayList<>();
        final List<String> deDuplicationList = new CopyOnWriteArrayList<>();
        try {
            // 遍历场景
            for (Map.Entry<String, Map<String, String>> entry : entries) {
                String userType = entry.getKey();
                Map<String, String> value = entry.getValue();
                Integer day = handlerService.getShuHePeriodOfValidityDay(userType);
                // T天数据截止时间 开区间
                LocalDateTime endDateTimeT = LocalDateTime.of(LocalDate.now()
                        , LocalTime.parse(value.getOrDefault("time"
                                , "20:00:00"))).atZone(ZoneId.systemDefault()).toLocalDateTime();
                // T天数据开始时间 闭区间
                LocalDateTime startDateTimeT = endDateTimeT.minusDays(day);
                // T-1天数据开始时间 闭区间
                LocalDateTime startDateTimeMinusOneT = endDateTimeT.minusDays(1);
                // T-1天数据结束时间 开区间
                LocalDateTime endDateTimeMinusOneT = endDateTimeT.toLocalDate().atStartOfDay().atZone(
                        ZoneId.systemDefault()).toLocalDateTime();
                // 添加T天查询任务
                String startDateTimeStrT = startDateTimeT.format(isoDateTime);
                String endDateTimeStrT = endDateTimeT.format(isoDateTime);
                list.add(() -> invalidDataFilter(userType, value, startDateTimeStrT, endDateTimeStrT, pageNum
                        , null, deDuplicationList));
                // 添加T-1天查询任务
                String startDateTimeMinusOneStrT = startDateTimeMinusOneT.format(isoDateTime);
                String endDateTimeMinusOneStrT = endDateTimeMinusOneT.format(isoDateTime);
                list.add(() -> invalidDataFilter(userType, value, startDateTimeMinusOneStrT, endDateTimeMinusOneStrT
                        , pageNum, endDateTimeMinusOneStrT, deDuplicationList));
                // 执行查询任务
                List<Future<List<DassAssembleTransferDataDTO>>> futures = POOL.invokeAll(list
                        , 3, TimeUnit.MINUTES);
                // 获取结果集
                List<DassAssembleTransferDataDTO> dtoList = futures.get(0).get(5, TimeUnit.SECONDS);
                dtoList.addAll(futures.get(1).get(5, TimeUnit.SECONDS));
                // 调用Dass转化接口
                artificialTransferHandler.call(dtoList, null);
                list.clear();
                deDuplicationList.clear();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
        } finally {
            list.clear();
            deDuplicationList.clear();
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
            , String syncUserDateTimeEnd
            , List<String> deDuplicationList) {
        String apiCode = value.get("apiCode");
        String orgName = value.get("orgname");
        // 分页获取电销数据
        List<PhoneSaleExtendInfo> listPage = phoneSaleExtendInfoMapper.findPushPhoneSaleListPage(
                apiCode, userType, startDateTimeStrT, endDateTimeStrT, pageNum, 1000);
        List<DassAssembleTransferDataDTO> transferData = new ArrayList<>();
        if (CollectionUtils.isEmpty(listPage)) {
            return transferData;
        }
        Set<String> custNums = listPage.parallelStream().map(
                PhoneSaleExtendInfo::getCustNum).collect(Collectors.toSet());
        // 查询有效期开始时间
        Map<String, Date> custNumMap = iMarketingSyncUserService.getSyncUserTimeMaxByCustNumsMap(apiCode
                , custNums, userType, syncUserDateTimeEnd);
        PhoneSaleTransferInfo phoneSale = new PhoneSaleTransferInfo();
        phoneSale.setTransformStatus("1");
        phoneSale.setDataType(PhoneSaleTransferDataTypeEnum.INVALID_DATA_FILTER.getValue());
        phoneSale.setApiCode(apiCode);
        phoneSale.setUserType(userType);
        phoneSale.setAppletDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        // 查询去重数据
        Set<String> custNumList = phoneSaleTransferInfoService.findCusaNumList(custNums, phoneSale);
        for (PhoneSaleExtendInfo info : listPage) {
            String custNum = info.getCustNum();
            // 判断有效期
            if (isLastDayValidity(userType, StringUtils.isBlank(syncUserDateTimeEnd) ? LocalDate.now()
                    : LocalDate.now().minusDays(1), custNumMap.getOrDefault(custNum, null))) {
                if (custNumList.contains(custNum) || deDuplicationList.contains(custNum)) {
                    continue;
                }
                // 构建数据
                DassAssembleTransferDataDTO dto = new DassAssembleTransferDataDTO();
                transferData.add(dto);
                DassTransferDataDTO dataDTO = new DassTransferDataDTO();
                dataDTO.setUid(custNum);
                dataDTO.setOrgName(orgName);
                dataDTO.setTransformStatus("1");
                dto.setDassTransferDataDTO(dataDTO);
                phoneSaleTransferInfoNew(dto, apiCode, orgName, dataDTO.getTransformStatus()
                        , info.getId(), custNum, userType);
                // 添加到去重集合
                custNumList.add(custNum);
                deDuplicationList.add(custNum);
            }
        }
        transferData.addAll(invalidDataFilter(userType, value, startDateTimeStrT, endDateTimeStrT, ++pageNum
                , syncUserDateTimeEnd, deDuplicationList));
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
    private boolean isLastDayValidity(String userType, LocalDate pCreateTime, Date validityDate) {
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
        return pCreateTime.isEqual(lastDate);
    }

    private void phoneSaleTransferInfoNew(DassAssembleTransferDataDTO dto, String apiCode
            , String orgName, String transformStatus, Long sourceId, String custNum, String userType) {
        PhoneSaleTransferInfo psti = new PhoneSaleTransferInfo();
        psti.setSourceId(sourceId);
        psti.setAppletDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        psti.setCreateTime(new Date());
        psti.setApiCode(apiCode);
        psti.setTransformStatus(transformStatus);
        psti.setDataType(PhoneSaleTransferDataTypeEnum.INVALID_DATA_FILTER.getValue());
        psti.setCustNum(custNum);
        psti.setUserType(userType);
        psti.setUpdateTime(psti.getCreateTime());
        psti.setOrgName(orgName);
        dto.setPhoneSaleTransferInfo(psti);
    }
}


