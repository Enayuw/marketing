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
import com.br.marketing.entity.MarketingTransferSyncUserExample;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        final String tcId = tableCreateService.getTcId(apiCode);
        if (StringUtils.isBlank(tcId)) {
            log.warn("该apicode未维护，{}", apiCode);
            return;
        }
        LocalDate localDate = LocalDate.now();
        int day = 30;
        // 推送优先级为d1>c1>b1>a1,d1为最高优先级，a1为最低优先级
        // 情况d1
        pushPageData(tcId, apiCode, localDate, "d", user -> preRejectWhereD1(user, localDate, day)
                , "B", "d1", "a", "b", "c", "d");
        // 情况c1
        pushPageData(tcId, apiCode, localDate, "c", user -> preRejectWhereC1(user, localDate, day)
                , "B", "c1", "d1", "a", "b", "c", "d");
        // 情况b1
        pushPageData(tcId, apiCode, localDate, "b", user -> preRejectWhereA1OrB1(user, localDate, day)
                , "A", "b1", "c1", "d1", "a", "b", "c", "d");
        // 情况a1
        pushPageData(tcId, apiCode, localDate, "a", user -> preRejectWhereA1OrB1(user, localDate, day)
                , "A", "a1", "b1", "c1", "d1", "a", "b", "c", "d");
    }

    /**
     * 2022/10/20 15:53
     * c1情况前置剔除条件
     * 锁定期：applyLoan=1&applyLoanTime+30天
     */
    private boolean preRejectWhereC1(MarketingTransferSyncUser user, LocalDate localDate, int day) {
        String reserveField1 = user.getReserveField1();
        if (StringUtils.isBlank(reserveField1)) {
            return false;
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
                    return true;
                }
            } catch (Exception e) {
                try {
                    applyLoanTimeLocalDate = LocalDateTime.parse(applyLoanTimeStr
                            , DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT))
                            .toLocalDate().plusDays(day);
                    if (localDate.isBefore(applyLoanTimeLocalDate) || localDate.isEqual(applyLoanTimeLocalDate)) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    /**
     * 2022/10/25 15:53
     * d1情况前置剔除条件
     * 锁定期：unlentAmount=0&lentTime+30天
     */
    private boolean preRejectWhereD1(MarketingTransferSyncUser user, LocalDate localDate, int day) {
        String lentTimeStr = user.getLentTime();
        String unlentAmount = user.getUnlentAmount();
        if (StringUtils.isBlank(unlentAmount)) {
            return false;
        }
        return "0".equals(unlentAmount) && compareDate(lentTimeStr, localDate, day);
    }

    /**
     * 2022/10/20 15:53
     * a1、b1情况前置剔除条件
     * 锁定期：applyDt有值+30天
     */
    private boolean preRejectWhereA1OrB1(MarketingTransferSyncUser user, LocalDate localDate, int day) {
        String applyDtStr = user.getApplyDt();
        return compareDate(applyDtStr, localDate, day);
    }

    /**
     * 2022/10/25 15:47
     * 比较当前日期是否在给定日期加n天范围内（包括等于）
     *
     * @param dateTimeStr 给定时间字符串
     * @param localDate   当前日期
     * @param day         天
     * @return true 在范围内，false 不在范围内
     */
    private boolean compareDate(String dateTimeStr, LocalDate localDate, int day) {
        if (StringUtils.isBlank(dateTimeStr)) {
            return false;
        }
        LocalDate localDateNew;
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
     * 2022/10/20 15:13
     * 分页查询对应情况、推送日期的转化分页数据
     *
     * @param tcId    分表后缀
     * @param apiCode apiCode
     * @param status  情况
     */
    private void pushPageData(final String tcId
            , final String apiCode
            , final LocalDate localDate
            , final String status
            , Predicate<MarketingTransferSyncUser> predicate
            , String userType
            , String... statusList) {
        Set<String> dateSet = getDateSet(status, localDate);
        int page = 1;
        int pageSize = 2000;
        boolean nextBool = true;
        while (nextBool) {
            List<PhoneSaleExtendInfo> pageList = phoneSaleExtendInfoMapper
                    .findOrangeCyclicalPage(apiCode, dateSet, status, page, pageSize);
            if (CollectionUtils.isEmpty(pageList)) {
                break;
            } else if (pageList.size() < pageSize) {
                nextBool = false;
            }
            page++;
            List<PhoneSaleExtendInfo> list = statusFilter(preReject(tcId, apiCode, pageList, predicate)
                    , localDate, apiCode, statusList);
            sendDass(list, status + "1", userType);
        }
    }

    private void sendDass(List<PhoneSaleExtendInfo> list, String status, String userType) {
        List<BatchRealTimeUserDataDTO> transferData = new ArrayList<>();
        for (PhoneSaleExtendInfo u : list) {
            BatchRealTimeUserDataDTO dataDTO = new BatchRealTimeUserDataDTO();
            DassImportDataDTO dassImportData = getDassImportData(u, userType);
            if (dassImportData == null) {
                continue;
            }
            dataDTO.setDassImportDataDTO(dassImportData);
            dataDTO.setPhoneSaleExtendInfo(getPhoneSaleExtendInfo(u, status, userType));
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
     * 2022/10/28 17:41
     * 前置剔除
     */
    private Map<String, PhoneSaleExtendInfo> preReject(String tCid
            , String apiCode
            , List<PhoneSaleExtendInfo> list
            , Predicate<MarketingTransferSyncUser> predicate) {
        int pageSize = 2000;
        int page = 0;
        Map<String, PhoneSaleExtendInfo> map = list.parallelStream().collect(
                Collectors.toMap(PhoneSaleExtendInfo::getCustNum, Function.identity()));
        Set<String> numSet = new HashSet<>();
        for (; ; ) {
            MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andCustNumIn(new ArrayList<>(map.keySet()));
            example.settCid(tCid);
            example.setOrderByClause("id limit ".concat(String.format("%s,%s", page * pageSize, pageSize)));
            List<MarketingTransferSyncUser> userList = marketingTransferSyncUserMapper.selectByExample(example);
            page++;
            if (CollectionUtils.isEmpty(userList)) {
                break;
            }
            Set<String> set = userList.parallelStream().filter(predicate)
                    .map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            numSet.addAll(set);
            if (userList.size() < pageSize) {
                break;
            }
        }
        map.keySet().removeAll(numSet);
        list.clear();
        numSet.clear();
        return map;
    }

    /**
     * 2022/10/21 15:01
     * 过滤其他情况的案件
     */
    private List<PhoneSaleExtendInfo> statusFilter(Map<String, PhoneSaleExtendInfo> map
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

    private PhoneSaleExtendInfo getPhoneSaleExtendInfo(PhoneSaleExtendInfo info, String status
            , String userType) {
        info.setPStatus(1);
        info.setCreateTime(new Date());
        info.setUpdateTime(info.getCreateTime());
        info.setPushDxTime(new Date());
        info.setTransformType("0");
        info.setStatus(status);
        info.setDxType(userType);
        info.setId(null);
        return info;
    }

    private DassImportDataDTO getDassImportData(PhoneSaleExtendInfo info, String userType) {
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setId(info.getSourceId());
        String custNum = info.getCustNum();
        String cell = decodeClient.query(custNum, "cell", "md5", "");
        if (StringUtils.isBlank(cell)) {
            log.warn("桔子周期性推送dass，手机号解密失败！id:{};custNum:{}", info.getId(), info.getCustNum());
            return null;
        }
        //cell转aes加密
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        batchImportData.setPhone(phone);
        batchImportData.setName("1");
        batchImportData.setOrgname("juzi");
        batchImportData.setUid(info.getCustNum());
        batchImportData.setUserType(userType);
        batchImportData.setSource("15");
        batchImportData.setOptype("1");
        return batchImportData;
    }
}
