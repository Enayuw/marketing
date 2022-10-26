package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.OrangePushDassService;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialBatchRealTimeDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;

/**
 * 桔子推送电销 业务实现
 *
 * @author Guo Zeqiang
 * @dateTime 2022/10/19 14:32
 */
@Service
@Slf4j
public class OrangePushDassServiceImpl implements OrangePushDassService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    @Resource
    private ArtificialBatchRealTimeDataHandler artificialBatchRealTimeDataHandler;
    @Resource
    private DecodeClient decodeClient;
    @Value("${api.dass.aesKey:}")
    private String aesKey;

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[:SSS]");

    @Override
    public void transferCyclicalPushDass(String apiCode) {
        final LocalDate localDate = LocalDate.now();
        final String tcId = tableCreateService.getTcId(apiCode);
        if (StringUtils.isBlank(tcId)) {
            log.warn("该apicode未维护，{}", apiCode);
            return;
        }
        DayObj dayObj = new DayObj(localDate, 30);
        // 推送优先级为d1>c1>b1>a1,d1为最高优先级，a1为最低优先级
        // 情况d1
        pushPageData(tcId, apiCode, "d", dayObj, this::preRejectWhereD1, "B"
                , "d1", "a", "b", "c", "d");
        // 情况c1
        pushPageData(tcId, apiCode, "c", dayObj, this::preRejectWhereC1, "B"
                , "c1", "d1", "a", "b", "c", "d");
        // 情况b1
        pushPageData(tcId, apiCode, "b", dayObj, this::preRejectWhereA1OrB1, "A"
                , "b1", "c1", "d1", "a", "b", "c", "d");
        // 情况a1
        pushPageData(tcId, apiCode, "a", dayObj, this::preRejectWhereA1OrB1, "A"
                , "a1", "b1", "c1", "d1", "a", "b", "c", "d");
    }

    /**
     * 2022/10/20 15:53
     * c1情况前置剔除条件
     * 锁定期：applyLoan=1&applyLoanTime+30天
     */
    private Map<String, MarketingTransferSyncUser> preRejectWhereC1(List<MarketingTransferSyncUser> list
            , DayObj dayObj) {
        int day = dayObj.day;
        LocalDate localDate = dayObj.localDate;
        Map<String, MarketingTransferSyncUser> map = new HashMap<>(list.size());
        for (MarketingTransferSyncUser user : list) {
            String reserveField1 = user.getReserveField1();
            String custNum = user.getCustNum();
            if (StringUtils.isBlank(reserveField1)) {
                addMap(map, custNum, user);
                continue;
            }
            JSONObject jsonObject = JSON.parseObject(reserveField1);
            String applyLoan = jsonObject.getString("applyLoan");
            String applyLoanTimeStr = jsonObject.getString("applyLoanTime");
            boolean applyLoanBool = "1".equals(applyLoan);
            if (applyLoanBool && StringUtils.isNotBlank(applyLoanTimeStr)) {
                LocalDate applyLoanTimeLocalDate;
                try {
                    applyLoanTimeLocalDate = LocalDate.parse(applyLoanTimeStr, DateTimeFormatter.ISO_LOCAL_DATE)
                            .plusDays(day);
                    if (localDate.isBefore(applyLoanTimeLocalDate) || localDate.isEqual(applyLoanTimeLocalDate)) {
                        continue;
                    }
                } catch (Exception e) {
                    try {
                        applyLoanTimeLocalDate = LocalDateTime.parse(applyLoanTimeStr
                                , DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT))
                                .toLocalDate().plusDays(day);
                        if (localDate.isBefore(applyLoanTimeLocalDate) || localDate.isEqual(applyLoanTimeLocalDate)) {
                            continue;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            addMap(map, custNum, user);
        }
        list.clear();
        return map;
    }

    /**
     * 2022/10/25 15:53
     * d1情况前置剔除条件
     * 锁定期：unlentAmount=0&lentTime+30天
     */
    private Map<String, MarketingTransferSyncUser> preRejectWhereD1(List<MarketingTransferSyncUser> list
            , DayObj dayObj) {
        Map<String, MarketingTransferSyncUser> map = new HashMap<>(list.size());
        for (MarketingTransferSyncUser user : list) {
            String lentTimeStr = user.getLentTime();
            String unlentAmount = user.getUnlentAmount();
            String custNum = user.getCustNum();
            if (StringUtils.isBlank(lentTimeStr) || StringUtils.isBlank(unlentAmount)) {
                addMap(map, custNum, user);
                continue;
            }
            boolean unlentAmountBool = "0".equals(unlentAmount);
            if (unlentAmountBool && compareDate(lentTimeStr, dayObj)) {
                continue;
            }
            addMap(map, custNum, user);
        }
        list.clear();
        return map;
    }

    /**
     * 2022/10/20 15:53
     * a1、b1情况前置剔除条件
     * 锁定期：applyDt有值+30天
     */
    private Map<String, MarketingTransferSyncUser> preRejectWhereA1OrB1(List<MarketingTransferSyncUser> list
            , DayObj dayObj) {
        Map<String, MarketingTransferSyncUser> map = new HashMap<>(list.size());
        for (MarketingTransferSyncUser user : list) {
            String applyDtStr = user.getApplyDt();
            String custNum = user.getCustNum();
            if (StringUtils.isBlank(applyDtStr)) {
                addMap(map, custNum, user);
                continue;
            }
            if (compareDate(applyDtStr, dayObj)) {
                continue;
            }
            addMap(map, custNum, user);
        }
        list.clear();
        return map;
    }

    /**
     * 2022/10/25 15:47
     * 比较当前日期是否在给定日期加n天范围内（包括等于）
     *
     * @param dateTimeStr 给定时间字符串
     * @param dayObj      当前日期
     * @return true 在范围内，false 不在范围内
     */
    private boolean compareDate(String dateTimeStr, DayObj dayObj) {
        LocalDate localDateNew;
        int day = dayObj.day;
        LocalDate localDate = dayObj.localDate;
        try {
            localDateNew = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER).toLocalDate().plusDays(day);
            if (localDate.isBefore(localDateNew) || localDate.isEqual(localDateNew)) {
                return true;
            }
        } catch (Exception e) {
            try {
                localDateNew = LocalDate.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(day);
                if (localDate.isBefore(localDateNew) || localDate.isEqual(localDateNew)) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * 2022/10/20 18:10
     * 添加到map
     * key custNum
     * value {@link MarketingTransferSyncUser}
     */
    private void addMap(Map<String, MarketingTransferSyncUser> map, String custNum
            , MarketingTransferSyncUser user) {
        if (map.containsKey(custNum)) {
            MarketingTransferSyncUser syncUser = map.get(custNum);
            String insertTimeOld = syncUser.getInsertTime();
            String insertTime = user.getInsertTime();
            if (StringUtils.isBlank(insertTimeOld) || StringUtils.isBlank(insertTime)) {
                if (syncUser.getCreateTime().before(user.getCreateTime())) {
                    map.put(custNum, user);
                }
            } else {
                try {
                    LocalDateTime timeOld = LocalDateTime.parse(insertTimeOld, DATE_TIME_FORMATTER);
                    LocalDateTime time = LocalDateTime.parse(insertTime, DATE_TIME_FORMATTER);
                    if (timeOld.isBefore(time)) {
                        map.put(custNum, user);
                    }
                } catch (Exception ignored) {
                    if (syncUser.getCreateTime().before(user.getCreateTime())) {
                        map.put(custNum, user);
                    }
                }
            }
        } else {
            map.put(custNum, user);
        }
    }

    /**
     * 2022/10/20 15:13
     * 分页查询对应情况、推送日期的转化分页数据
     *
     * @param tcId    分表后缀
     * @param apiCode apiCode
     * @param status  情况
     */
    private void pushPageData(final String tcId
            , final String apiCode
            , final String status
            , final DayObj dayObj
            , BiFunction<List<MarketingTransferSyncUser>, DayObj
            , Map<String, MarketingTransferSyncUser>> biFunction, String userType, String... statusList) {
        Set<String> dateSet = getDateSet(status, dayObj.localDate);
        int page = 1;
        int pageSize = 2000;
        boolean nextBool = true;
        while (nextBool) {
            List<MarketingTransferSyncUser> pageList = marketingTransferSyncUserMapper
                    .findOrangeCyclicalTransferSyncPage(tcId, apiCode, dateSet, status, page, pageSize);
            if (CollectionUtils.isEmpty(pageList)) {
                break;
            } else if (pageList.size() < pageSize) {
                nextBool = false;
            }
            page++;
            List<MarketingTransferSyncUser> list = statusFilter(biFunction.apply(pageList, dayObj)
                    , dayObj.localDate, apiCode, statusList);
            sendDass(list, status + "1", userType);
        }
    }

    private void sendDass(List<MarketingTransferSyncUser> list, String status, String userType) {
        List<BatchRealTimeUserDataDTO> transferData = new ArrayList<>();
        for (MarketingTransferSyncUser u : list) {
            BatchRealTimeUserDataDTO dataDTO = new BatchRealTimeUserDataDTO();
            u.setUserType(userType);
            DassImportDataDTO dassImportData = getDassImportData(u);
            if (dassImportData == null) {
                continue;
            }
            dataDTO.setDassImportDataDTO(dassImportData);
            dataDTO.setPhoneSaleExtendInfo(getPhoneSaleExtendInfo(u, status));
            transferData.add(dataDTO);
        }
        artificialBatchRealTimeDataHandler.call(transferData, new ProcessHandlerContext());
    }

    /**
     * 2022/10/20 15:12
     * 获取日期集合
     *
     * @param status    情况
     * @param localDate 本地日期
     */
    private Set<String> getDateSet(String status, LocalDate localDate) {
        Map<String, Set<Integer>> map = marketingCommonConfig.getOrangeTransferCyclicalPushDassDay();
        if (map == null) {
            map = new HashMap<>(8);
            Set<Integer> a = new HashSet<>(Collections.singletonList(2));
            Set<Integer> c = new HashSet<>(Arrays.asList(2, 6, 13, 27));
            Set<Integer> d = new HashSet<>(Collections.singletonList(6));
            map.put("a", a);
            map.put("b", a);
            map.put("c", c);
            map.put("d", d);
        }
        Set<Integer> daySet = map.get(status);
        if (CollectionUtils.isEmpty(daySet)) {
            return null;
        }
        Set<String> dateSet = new HashSet<>();
        for (Integer day : daySet) {
            dateSet.add(localDate.minusDays(day).format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        return dateSet;
    }

    /**
     * 2022/10/21 15:01
     * 过滤其他情况的案件
     */
    private List<MarketingTransferSyncUser> statusFilter(Map<String, MarketingTransferSyncUser> map
            , LocalDate localDate, String apiCode, String... statusList) {
        if (map.size() < 1 || statusList.length < 1) {
            return new ArrayList<>(map.values());
        }
        String dateStr = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        Set<String> custNumSet = map.keySet();
        // a1+b1+c1+d1+a+b+c+d当天仅推送一次
        Set<String> set = phoneSaleExtendInfoMapper.getCustNumByCustNumAndStatusAndDateSet(
                apiCode, custNumSet, Arrays.asList(statusList), dateStr);
        custNumSet.removeAll(set);
        if (custNumSet.size() == 0) {
            return new ArrayList<>(map.values());
        }
        //a+a1+b+b1求和7天内推送3次
        String recordDate = LocalDateTime.now().minusDays(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> pushThreeRecord = phoneSaleExtendInfoMapper.getJuziPushThreeRecordtikv_(apiCode
                , recordDate, new ArrayList<>(custNumSet));
        custNumSet.removeAll(new HashSet<>(pushThreeRecord));
        return new ArrayList<>(map.values());
    }

    private PhoneSaleExtendInfo getPhoneSaleExtendInfo(MarketingTransferSyncUser transfer, String status) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        phoneSaleExtendInfo.setAppletTime(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setUpdateTime(phoneSaleExtendInfo.getCreateTime());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setTransformType("0");
        phoneSaleExtendInfo.setStatus(status);
        phoneSaleExtendInfo.setType(transfer.getType());
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        return phoneSaleExtendInfo;
    }

    private DassImportDataDTO getDassImportData(MarketingTransferSyncUser transfer) {
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setId(transfer.getId());
        String custNum = transfer.getCustNum();
        String cell = decodeClient.query(custNum, "cell", "md5", "");
        if (StringUtils.isBlank(cell)) {
            log.warn("桔子周期性推送dass，手机号解密失败！id:{};custNum:{}", transfer.getId(), transfer.getCustNum());
            return null;
        }
        //cell转aes加密
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        batchImportData.setPhone(phone);
        batchImportData.setName("1");
        batchImportData.setOrgname("juzi");
        batchImportData.setUid(transfer.getCustNum());
        batchImportData.setUserType(transfer.getUserType());
        batchImportData.setSource("15");
        batchImportData.setOptype("1");
        return batchImportData;
    }

    /**
     * 2022/10/25 16:25
     * 天对象
     */
    private static class DayObj {
        LocalDate localDate;
        int day;

        public DayObj(LocalDate localDate, int day) {
            this.localDate = localDate;
            this.day = day;
        }
    }
}
