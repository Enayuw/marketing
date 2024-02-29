package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.check.service.OrangePushDassService;
import com.br.marketing.check.service.OriginPeriodPredicateGetDataService;
import com.br.marketing.check.service.OriginPeriodPredicateService;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialBatchRealTimeDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private DataDistributeDetailLogMapper dataDistributeDetailLogMapper;
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
        String[] statusList = new String[]{"a1", "b1", "c1", "d1", "a", "b", "c", "d"};
        String dxTypeA = "A";
        String dxTypeB = "B";
        // 推送优先级为d1>c1>b1>a1,d1为最高优先级，a1为最低优先级
        // 情况d1
        pushPageData(tcId, apiCode, localDate, "d", user -> preRejectWhereD1(user, localDate, day)
                , dxTypeB, statusList);
        // 情况c1
        pushPageData(tcId, apiCode, localDate, "c", user -> preRejectWhereC1(user, localDate, day)
                , dxTypeB, statusList);
        // 情况b1
        pushPageData(tcId, apiCode, localDate, "b", user -> preRejectWhereA1OrB1(user, localDate, day)
                , dxTypeA, statusList);
        // 情况a1
        pushPageData(tcId, apiCode, localDate, "a", user -> preRejectWhereA1OrB1(user, localDate, day)
                , dxTypeA, statusList);
    }

    @Override
    public void transferPeriodToPushDaas(String tcid, String apiCode, String status,
                                         List<OriginPeriodPredicateService> juZiPeriodPredicateServiceList,
                                         List<OriginPeriodPredicateGetDataService> originPeriodPredicateGetDataServices) {

        boolean ruleContinue = Boolean.TRUE;
        Long minId = null;
        while (ruleContinue) {

            List<MarketingTransferSyncUser> juZiRuleDataList = new ArrayList<>();
            // 获取需要处理的数据  a,b,c,d 4种情况。
            for (int i = 0; i < originPeriodPredicateGetDataServices.size(); i++) {
                juZiRuleDataList = originPeriodPredicateGetDataServices.get(i).getJuZiRuleData(status, tcid, apiCode, minId);
                if (juZiRuleDataList.size() > 0) break;
            }
            if (juZiRuleDataList.size() == 0) {
                ruleContinue = Boolean.FALSE;
                continue;
            }
            minId = juZiRuleDataList.get(juZiRuleDataList.size() - 1).getId() + 1;

            Set<String> custNumSet = juZiRuleDataList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, apiCode, null);
            // 1. 获取有效期内的最新的数据
            List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists = new ArrayList<>();
            juZiRuleDataList.forEach((MarketingTransferSyncUser jz) -> {
                SyncUserValidityPeriodsBO userValidityPeriodsBO = validityPeriodsByCustNum.get(jz.getCustNum());
                if (userValidityPeriodsBO != null) {
                    MarketingSyncUser marketingSyncUser = userValidityPeriodsBO.getSyncUsers().get(0);
                    MarketingTransferSyncUserCell marketingTransferSyncUserCell = new MarketingTransferSyncUserCell();
                    BeanUtils.copyProperties(jz, marketingTransferSyncUserCell);
                    marketingTransferSyncUserCell.setCell(marketingSyncUser.getCell());
                    marketingTransferSyncUserCell.setTaskId(marketingSyncUser.getCusBatch());
                    marketingTransferSyncUserCell.setUserType(marketingSyncUser.getUserType());
                    marketingTransferSyncUserCellLists.add(marketingTransferSyncUserCell);
                }
            });

            // 2. 批次内去重
            Set<MarketingTransferSyncUserCell> marketingTransferSyncUserCellSet = new TreeSet<>(Comparator.comparing(MarketingTransferSyncUserCell::getCell));
            marketingTransferSyncUserCellSet.addAll(marketingTransferSyncUserCellLists);


            // 3. 情况b 和 c 要做剔除 < 1000
            if ("b".equals(status) || "c".equals(status)) {
                marketingTransferSyncUserCellSet.removeIf((MarketingTransferSyncUserCell m) -> {
                            List<MarketingTransferSyncUser> transferSyncUserList =
                                    marketingTransferSyncUserMapper.getValidityPeriodData(tcid, apiCode, m.getCustNum());
                            Set<String> numSet = transferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                            Map<String, SyncUserValidityPeriodsBO> validityPeriodsBoMap =
                                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(numSet, apiCode, null);
                            return validityPeriodsBoMap.get(m.getCustNum()) != null;
                        }
                );
            }

            if (marketingTransferSyncUserCellSet.size() > 0) {
                // 获取cell set 集合
                Set<String> cellSet = marketingTransferSyncUserCellSet.stream().map(MarketingTransferSyncUserCell::getCell).collect(Collectors.toSet());
                // 查询电销推送日志表
                Set<String> toDassLogInfoSet = phoneSaleExtendInfoMapper.getToDassLogInfoList(apiCode,cellSet);
                // 查询决策推送日志表
                Set<String> distributionToDassLogInfoSet = dataDistributeDetailLogMapper.getToDataDistributeInfoList(apiCode, cellSet);
                // 合并2个集合
                Set<String> resultSet = new HashSet<>();
                Stream.of(toDassLogInfoSet, distributionToDassLogInfoSet).forEach(resultSet::addAll);

                // 4. 剔除当天推过的数据。
                marketingTransferSyncUserCellSet.removeIf(m -> resultSet.contains(m.getCell()));

                // 5. 推送daas 、 决策
                if (marketingTransferSyncUserCellSet.size() > 0) {
                    juZiPeriodPredicateServiceList.forEach(juZiPeriodPredicateService -> juZiPeriodPredicateService.transferDataPeriod(apiCode, status, marketingTransferSyncUserCellSet));
                }
            }
        }

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
        boolean bool = "1".equals(applyLoan) && StringUtils.isNotBlank(applyLoanTimeStr);
        if (bool) {
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
        String unlentAmount = user.getUnlentAmount();
        if (StringUtils.isBlank(unlentAmount)) {
            return false;
        }
        return "0".equals(unlentAmount) && compareDate(user.getLentTime(), localDate, day);
    }

    /**
     * 2022/10/20 15:53
     * a1、b1情况前置剔除条件
     * 锁定期：applyDt有值+30天
     */
    private boolean preRejectWhereA1OrB1(MarketingTransferSyncUser user, LocalDate localDate, int day) {
        return compareDate(user.getApplyDt(), localDate, day);
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
     * 分页查询对应情况、推送日期的数据
     *
     * @param tcId            分表后缀
     * @param apiCode         apiCode
     * @param localDate       当前日期
     * @param status          情况
     * @param predicateReject 剔除函数
     * @param dxType          电销场景
     * @param statusList      情况集合
     */
    private void pushPageData(final String tcId
            , final String apiCode
            , final LocalDate localDate
            , final String status
            , final Predicate<MarketingTransferSyncUser> predicateReject
            , final String dxType
            , final String... statusList) {
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
            List<PhoneSaleExtendInfo> list = statusFilter(preReject(tcId, apiCode, pageList, predicateReject)
                    , status, localDate, apiCode, statusList);
            sendDass(list, status + "1", dxType);
        }
    }

    /**
     * 2022/10/28 20:08
     */
    private void sendDass(List<PhoneSaleExtendInfo> list, String status, String userType) {
        List<BatchRealTimeUserDataDTO> transferData = new ArrayList<>();
        for (PhoneSaleExtendInfo u : list) {
            DassImportDataDTO dassImportData = getDassImportData(u, userType);
            if (dassImportData == null) {
                continue;
            }
            BatchRealTimeUserDataDTO dataDTO = new BatchRealTimeUserDataDTO();
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
        Map<String, List<Integer>> map = marketingCommonConfig.getOrangeTransferCyclicalPushDassDay();
        if (map == null || map.size() == 0) {
            map = new HashMap<>(8);
            List<Integer> a = Collections.singletonList(2);
            List<Integer> c = Arrays.asList(2, 6, 13, 27);
            List<Integer> d = Collections.singletonList(6);
            map.put("a", a);
            map.put("b", a);
            map.put("c", c);
            map.put("d", d);
        }
        Set<Integer> daySet = new HashSet<>(map.get(status));
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
     *
     * @param predicateReject 剔除函数{@link MarketingTransferSyncUser}
     * @return map key custNum value {@link PhoneSaleExtendInfo}
     */
    private Map<String, PhoneSaleExtendInfo> preReject(String tCid
            , String apiCode
            , List<PhoneSaleExtendInfo> list
            , Predicate<MarketingTransferSyncUser> predicateReject) {
        int pageSize = 2000;
        int page = 0;
        Map<String, PhoneSaleExtendInfo> map = list.parallelStream().collect(
                Collectors.toMap(PhoneSaleExtendInfo::getCustNum, Function.identity()
                        , BinaryOperator.maxBy(Comparator.comparing(PhoneSaleExtendInfo::getCreateTime))));
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
            Set<String> set = userList.parallelStream().filter(predicateReject)
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
    private List<PhoneSaleExtendInfo> statusFilter(Map<String, PhoneSaleExtendInfo> map, String status
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
        if ("a".equals(status) || "b".equals(status)) {
            String recordDate = localDate.minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE);
            List<String> pushThreeRecord = phoneSaleExtendInfoMapper.getJuziPushThreeRecordtikv_(apiCode
                    , recordDate, new ArrayList<>(custNumSet));
            custNumSet.removeAll(new HashSet<>(pushThreeRecord));
        }
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
        String cell = RpcClientProxy.decode(custNum, "cell", "md5", "");
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
