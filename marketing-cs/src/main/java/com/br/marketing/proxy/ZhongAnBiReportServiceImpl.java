package com.br.marketing.proxy;


import com.br.marketing.common.annoation.PercentConvertor;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyEightReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyOneReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalySevenReportDTO;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@PercentConvertor
public class ZhongAnBiReportServiceImpl implements ZhongAnBiReportService {

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;

    private static final String MARKETING_IFLOGIN = "营销首登组";

    private static final String MARKETING_IFNOTLOGIN = "营销非首登组";

    private static final String ZHONGAN_IFLOGIN = "众安对照首登组";

    private static final String ZHONGAN_IFNOTLOGIN = "众安对照非首登组";

    private static final String BR_MARKETING = "百融营销组";

    private static final String ZHONGAN_MARKETING = "众安对照组";


    @Override
    public List<ZhongAnBusAnalyOneReportDTO> selectZaBusAnalyOneListbI_(String reportId) {
        List<ZhongAnBusAnalyOneReportDTO> zhongAnBusAnalyOneReportList = zhongAnBiReportMapper.selectZaBusAnalyOneListbI_(reportId);
        LocalDate endDate = zhongAnBusAnalyOneReportList.stream()
                .map(item -> LocalDate.parse(item.getReportDate()))
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        LocalDate beginDate = zhongAnBusAnalyOneReportList.stream()
                .map(item -> LocalDate.parse(item.getReportDate()))
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        zhongAnBusAnalyOneReportList.forEach(result -> result.setQueryDate("T"));
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String reportDate = beginDate + "-" + date;
            List<String> reportDateList = zhongAnBusAnalyOneReportList.stream()
                    .map(ZhongAnBusAnalyOneReportDTO::getReportDate)
                    .collect(Collectors.toList());
            List<String> groupList = Lists.newArrayList(MARKETING_IFLOGIN, ZHONGAN_IFLOGIN, MARKETING_IFNOTLOGIN, ZHONGAN_IFNOTLOGIN);
            List<ZhongAnBusAnalyOneReportDTO> totalList = new ArrayList<>();
            if (reportDateList.size() > 1) {
                LocalDate finalEndDate = date;
                groupList.forEach((String group) -> {
                    ZhongAnBusAnalyOneReportDTO oneReportDTO = new ZhongAnBusAnalyOneReportDTO();
                    oneReportDTO.setReportDate(reportDate);
                    oneReportDTO.setQueryDate("T" + finalEndDate.getDayOfMonth());
                    oneReportDTO.setConstituencies(group);
                    oneReportDTO.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
                    oneReportDTO.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
                    oneReportDTO.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
                    oneReportDTO.setCompositeIncrNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getCompositeIncrNum).reduce(BigDecimal.ZERO, BigDecimal::add));
                    oneReportDTO.setIncome(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add));
                    oneReportDTO.setCost(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getCost).reduce(BigDecimal.ZERO, BigDecimal::add));
                    oneReportDTO.setIncomingTotalRate(new BigDecimal((0)));
                    oneReportDTO.setApproversRate(new BigDecimal((0)));
                    oneReportDTO.setApproversTotalRate(new BigDecimal((0)));
                    oneReportDTO.setRoi(new BigDecimal((0)));
                    if (!oneReportDTO.getTotalNum().equals(0L)) {
                        oneReportDTO.setIncomingTotalRate(new BigDecimal(oneReportDTO.getIncomingNum()).divide(new BigDecimal(oneReportDTO.getTotalNum())
                                , 6, BigDecimal.ROUND_HALF_UP));
                        oneReportDTO.setApproversTotalRate(new BigDecimal(oneReportDTO.getApproversNum()).divide(new BigDecimal(oneReportDTO.getTotalNum())
                                , 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (!oneReportDTO.getIncomingNum().equals(0L)) {
                        oneReportDTO.setApproversRate(new BigDecimal(oneReportDTO.getApproversNum()).divide(new BigDecimal(oneReportDTO.getIncomingNum())
                                , 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (oneReportDTO.getCost().compareTo(BigDecimal.ZERO) != 0) {
                        oneReportDTO.setRoi(oneReportDTO.getIncome().divide(oneReportDTO.getCost(), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    totalList.add(oneReportDTO);

                });
                //营销首登组,众安对照首登组
                ZhongAnBusAnalyOneReportDTO brReportDTO = totalList.get(0);
                ZhongAnBusAnalyOneReportDTO zhongAnReportDTO = totalList.get(1);
                brReportDTO.setIncomingIncreaseRate(new BigDecimal((0)));
                if ((zhongAnReportDTO.getTotalNum() != 0L) && (((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                        zhongAnReportDTO.getIncomingNum()) != 0L)) {
                    brReportDTO.setIncomingIncreaseRate(new BigDecimal(brReportDTO.getIncomingNum() - ((double) brReportDTO.getTotalNum() /
                            zhongAnReportDTO.getTotalNum() * zhongAnReportDTO.getIncomingNum()))
                            .divide(new BigDecimal((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                                    zhongAnReportDTO.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));

                }
                brReportDTO.setApproversIncreaseRate(new BigDecimal((0)));
                if ((zhongAnReportDTO.getTotalNum() != 0L) && (((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                        zhongAnReportDTO.getApproversNum()) != 0L)) {
                    brReportDTO.setApproversIncreaseRate(new BigDecimal(brReportDTO.getApproversNum() - ((double) brReportDTO.getTotalNum() /
                            zhongAnReportDTO.getTotalNum() * zhongAnReportDTO.getApproversNum()))
                            .divide(new BigDecimal((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                                    zhongAnReportDTO.getApproversNum()), 6, BigDecimal.ROUND_HALF_UP));

                }
                zhongAnReportDTO.setIncomingIncreaseRate(brReportDTO.getIncomingIncreaseRate());
                zhongAnReportDTO.setApproversIncreaseRate(brReportDTO.getApproversIncreaseRate());
                //营销非首登组,众安对照非首登组
                ZhongAnBusAnalyOneReportDTO brNoLoginReportDTO = totalList.get(2);
                ZhongAnBusAnalyOneReportDTO zhongAnNoLoginReportDTO = totalList.get(3);
                brNoLoginReportDTO.setIncomingIncreaseRate(new BigDecimal((0)));
                if ((zhongAnNoLoginReportDTO.getTotalNum() != 0L) && (((double) brNoLoginReportDTO.getTotalNum() / zhongAnNoLoginReportDTO.getTotalNum()
                        * zhongAnNoLoginReportDTO.getIncomingNum()) != 0L)) {
                    brNoLoginReportDTO.setIncomingIncreaseRate(new BigDecimal(brNoLoginReportDTO.getIncomingNum() - ((double) brNoLoginReportDTO
                            .getTotalNum() / zhongAnNoLoginReportDTO.getTotalNum() * zhongAnNoLoginReportDTO.getIncomingNum()))
                            .divide(new BigDecimal((double) brNoLoginReportDTO.getTotalNum() / zhongAnNoLoginReportDTO.getTotalNum() *
                                    zhongAnNoLoginReportDTO.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));

                }
                brNoLoginReportDTO.setApproversIncreaseRate(new BigDecimal((0)));
                if ((zhongAnNoLoginReportDTO.getTotalNum() != 0L) && (((double) brNoLoginReportDTO.getTotalNum() / zhongAnNoLoginReportDTO.getTotalNum() *
                        zhongAnNoLoginReportDTO.getApproversNum()) != 0L)) {
                    brNoLoginReportDTO.setApproversIncreaseRate(new BigDecimal(brNoLoginReportDTO.getApproversNum() - ((double) brNoLoginReportDTO
                            .getTotalNum() / zhongAnNoLoginReportDTO.getTotalNum() * zhongAnNoLoginReportDTO.getApproversNum()))
                            .divide(new BigDecimal((double) brNoLoginReportDTO.getTotalNum() / zhongAnNoLoginReportDTO.getTotalNum() *
                                    zhongAnNoLoginReportDTO.getApproversNum()), 6, BigDecimal.ROUND_HALF_UP));
                }
                zhongAnNoLoginReportDTO.setIncomingIncreaseRate(brNoLoginReportDTO.getIncomingIncreaseRate());
                zhongAnNoLoginReportDTO.setApproversIncreaseRate(brNoLoginReportDTO.getApproversIncreaseRate());
                zhongAnBusAnalyOneReportList.addAll(totalList);
            }
        }
        List<ZhongAnBusAnalyOneReportDTO> zhongAnBusAnalyOneReportDTOS = traverseDatesOne(zhongAnBusAnalyOneReportList);
        return zhongAnBusAnalyOneReportDTOS;
    }

    public static List<ZhongAnBusAnalyOneReportDTO> traverseDatesOne(List<ZhongAnBusAnalyOneReportDTO> zhongAnBusAnalyEightReportList) {
        List<ZhongAnBusAnalyOneReportDTO> result = new ArrayList<>();

        // 获取今天的日期
        LocalDate now = LocalDate.now();
        String oneDay = LocalDate.now().withDayOfMonth(1).toString();
        String oneDayToOneDay = oneDay + "-" + oneDay;
        // 判断今天是否是1号
        if (now.getDayOfMonth() == 1) {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
        } else {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
            // 如果不是1号，从2号到今天遍历
            LocalDate firstDayOfMonth = now.withDayOfMonth(2);
            LocalDate currentDay = firstDayOfMonth;

            // 遍历从1号到今天的日期
            while (!currentDay.isAfter(now)) {
                String dateStr = currentDay.toString();
                zhongAnBusAnalyEightReportList.stream()
                        .filter(date -> date.getReportDate().contains(dateStr))
                        .forEach(result::add);

                currentDay = currentDay.plusDays(1);
            }
        }

        return result;
    }


    @Override
    public List<ZhongAnBusAnalyEightReportDTO> selectZaBusAnalyEightListbI_(String reportId) {
        List<ZhongAnBusAnalyEightReportDTO> zhongAnBusAnalyEightReportList = zhongAnBiReportMapper.selectZaBusAnalyEightListbI_(reportId);
        LocalDate endDate = zhongAnBusAnalyEightReportList.stream()
                .map(item -> LocalDate.parse(item.getReportDate()))
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        LocalDate beginDate = zhongAnBusAnalyEightReportList.stream()
                .map(item -> LocalDate.parse(item.getReportDate()))
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        zhongAnBusAnalyEightReportList.forEach(result -> result.setQueryDate("T"));
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String reportDate = beginDate + "-" + date;
            List<String> reportDateList = zhongAnBusAnalyEightReportList.stream()
                    .map(ZhongAnBusAnalyEightReportDTO::getReportDate)
                    .collect(Collectors.toList());
            List<String> groupList = Lists.newArrayList(BR_MARKETING, ZHONGAN_MARKETING);
            List<ZhongAnBusAnalyEightReportDTO> totalList = new ArrayList<>();
            if (reportDateList.size() > 1) {
                LocalDate finalDate = date;
                groupList.forEach((String group) -> {
                    ZhongAnBusAnalyEightReportDTO eightReportDTO = new ZhongAnBusAnalyEightReportDTO();
                    eightReportDTO.setReportDate(reportDate);
                    eightReportDTO.setQueryDate("T" + finalDate.getDayOfMonth());
                    eightReportDTO.setConstituencies(group);
                    eightReportDTO.setTotalNum(zhongAnBusAnalyEightReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getTotalNum).findFirst().orElse(null));
                    eightReportDTO.setIncomingNum(zhongAnBusAnalyEightReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalyEightReportDTO::getIncomingNum).sum());
                    eightReportDTO.setApproversNum(zhongAnBusAnalyEightReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalyEightReportDTO::getApproversNum).sum());
                    eightReportDTO.setCompositeIncrNum(zhongAnBusAnalyEightReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getCompositeIncrNum).reduce(BigDecimal.ZERO, BigDecimal::add));
                    eightReportDTO.setIncome(zhongAnBusAnalyEightReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add));
                    eightReportDTO.setCost(zhongAnBusAnalyEightReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getCost).reduce(BigDecimal.ZERO, BigDecimal::add));
                    eightReportDTO.setIncomingTotalRate(new BigDecimal((0)));
                    eightReportDTO.setApproversRate(new BigDecimal((0)));
                    eightReportDTO.setApproversTotalRate(new BigDecimal((0)));
                    eightReportDTO.setRoi(new BigDecimal((0)));
                    if (!eightReportDTO.getTotalNum().equals(0L)) {
                        eightReportDTO.setIncomingTotalRate(new BigDecimal(eightReportDTO.getIncomingNum()).divide(new BigDecimal(eightReportDTO.
                                getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                        eightReportDTO.setApproversTotalRate(new BigDecimal(eightReportDTO.getApproversNum()).divide(new BigDecimal(eightReportDTO.
                                getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (!eightReportDTO.getIncomingNum().equals(0L)) {
                        eightReportDTO.setApproversRate(new BigDecimal(eightReportDTO.getApproversNum()).divide(new BigDecimal(eightReportDTO.
                                getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (eightReportDTO.getCost().compareTo(BigDecimal.ZERO) != 0) {
                        eightReportDTO.setRoi(eightReportDTO.getIncome().divide(eightReportDTO.getCost(), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    totalList.add(eightReportDTO);

                });
                ZhongAnBusAnalyEightReportDTO brReportDTO = totalList.get(0);
                ZhongAnBusAnalyEightReportDTO zhongAnReportDTO = totalList.get(1);
                brReportDTO.setIncomingIncreaseRate(new BigDecimal((0)));
                if ((zhongAnReportDTO.getTotalNum() != 0L) && (((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                        zhongAnReportDTO.getIncomingNum()) != 0L)) {
                    brReportDTO.setIncomingIncreaseRate(new BigDecimal(brReportDTO.getIncomingNum() - ((double) brReportDTO.getTotalNum() /
                            zhongAnReportDTO.getTotalNum()
                            * zhongAnReportDTO.getIncomingNum()))
                            .divide(new BigDecimal((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                                    zhongAnReportDTO.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
                }
                brReportDTO.setApproversIncreaseRate(new BigDecimal((0)));
                if ((zhongAnReportDTO.getTotalNum() != 0L) && (((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                        zhongAnReportDTO.getApproversNum()) != 0L)) {
                    brReportDTO.setApproversIncreaseRate(new BigDecimal(brReportDTO.getApproversNum() - ((double) brReportDTO.getTotalNum() /
                            zhongAnReportDTO.getTotalNum() * zhongAnReportDTO.getApproversNum()))
                            .divide(new BigDecimal((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                                    zhongAnReportDTO.getApproversNum()), 6, BigDecimal.ROUND_HALF_UP));

                }
                zhongAnReportDTO.setIncomingIncreaseRate(brReportDTO.getIncomingIncreaseRate());
                zhongAnReportDTO.setApproversIncreaseRate(brReportDTO.getApproversIncreaseRate());
                zhongAnBusAnalyEightReportList.addAll(totalList);
            }

        }
        List<ZhongAnBusAnalyEightReportDTO> zhongAnBusAnalyEightReportDTOS = traverseDates(zhongAnBusAnalyEightReportList);

        return zhongAnBusAnalyEightReportDTOS;
    }

    public static List<ZhongAnBusAnalyEightReportDTO> traverseDates(List<ZhongAnBusAnalyEightReportDTO> zhongAnBusAnalyEightReportList) {
        List<ZhongAnBusAnalyEightReportDTO> result = new ArrayList<>();

        // 获取今天的日期
        LocalDate now = LocalDate.now();
        String oneDay = LocalDate.now().withDayOfMonth(1).toString();
        String oneDayToOneDay = oneDay + "-" + oneDay;
        // 判断今天是否是1号
        if (now.getDayOfMonth() == 1) {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
        } else {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
            // 如果不是1号，从2号到今天遍历
            LocalDate firstDayOfMonth = now.withDayOfMonth(2);
            LocalDate currentDay = firstDayOfMonth;

            // 遍历从1号到今天的日期
            while (!currentDay.isAfter(now)) {
                String dateStr = currentDay.toString();
                zhongAnBusAnalyEightReportList.stream()
                        .filter(date -> date.getReportDate().contains(dateStr))
                        .forEach(result::add);

                currentDay = currentDay.plusDays(1);
            }
        }

        return result;
    }



    @Override
    public List<ZhongAnBusAnalySevenReportDTO> selectZaBusAnalySeveListbI_(String reportId) {
        List<ZhongAnBusAnalySevenReportDTO> zhongAnBusAnalySevenReportList = zhongAnBiReportMapper.selectZaBusAnalySevenListbI_(reportId);
        LocalDate endDate = zhongAnBusAnalySevenReportList.stream()
                .map(item -> LocalDate.parse(item.getReportDate()))
                .max(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        LocalDate beginDate = zhongAnBusAnalySevenReportList.stream()
                .map(item -> LocalDate.parse(item.getReportDate()))
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());
        zhongAnBusAnalySevenReportList.forEach(result -> result.setQueryDate("T"));
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String reportDate = beginDate + "-" + date;
            List<String> reportDateList = zhongAnBusAnalySevenReportList.stream()
                    .map(ZhongAnBusAnalySevenReportDTO::getReportDate)
                    .collect(Collectors.toList());
            List<String> groupList = Lists.newArrayList(BR_MARKETING, ZHONGAN_MARKETING);
            List<ZhongAnBusAnalySevenReportDTO> totalList = new ArrayList<>();
            if (reportDateList.size() > 1) {
                LocalDate finalDate = date;
                groupList.forEach((String group) -> {
                    ZhongAnBusAnalySevenReportDTO sevenReportDTO = new ZhongAnBusAnalySevenReportDTO();
                    sevenReportDTO.setReportDate(reportDate);
                    sevenReportDTO.setQueryDate("T" + finalDate.getDayOfMonth());
                    sevenReportDTO.setConstituencies(group);
                    sevenReportDTO.setTotalNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalySevenReportDTO::getTotalNum).findFirst().orElse(null));
                    sevenReportDTO.setLoginNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getLoginNum).sum());
                    sevenReportDTO.setIncomingNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getIncomingNum).sum());
                    sevenReportDTO.setApproversNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getApproversNum).sum());
                    sevenReportDTO.setApplyPayNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getApplyPayNum).sum());
                    sevenReportDTO.setApplyPaySuccessNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getApplyPaySuccessNum).sum());
                    sevenReportDTO.setLendersSucNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getLendersSucNum).sum());
                    sevenReportDTO.setLendersSucAmount(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getLendersSucAmount).sum());
                    sevenReportDTO.setIncome(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalySevenReportDTO::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add));
                    sevenReportDTO.setCost(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalySevenReportDTO::getCost).reduce(BigDecimal.ZERO, BigDecimal::add));
                    Long totalApprovalCost = zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group))
                            .map(report-> report.getApprovalsAvgNum()*report.getApproversNum()).reduce(0L, Long::sum);
                    sevenReportDTO.setApprovalsAvgNum(0L);
                    sevenReportDTO.setLendersSucAvgAmount(0L);
                    sevenReportDTO.setRoi(new BigDecimal((0)));
                    sevenReportDTO.setProductCapacity(new BigDecimal((0)));
                    sevenReportDTO.setApplyPaySuccessRate(new BigDecimal((0)));
                    sevenReportDTO.setLoginRate(new BigDecimal((0)));
                    sevenReportDTO.setIncomingTotalRate(new BigDecimal((0)));
                    sevenReportDTO.setApproversRate(new BigDecimal((0)));
                    sevenReportDTO.setApproversTotalRate(new BigDecimal((0)));
                    sevenReportDTO.setApplyPayRate(new BigDecimal((0)));
                    sevenReportDTO.setLendersSucRate(new BigDecimal((0)));
                    sevenReportDTO.setLendersSucTotalRate(new BigDecimal((0)));
                    if (!sevenReportDTO.getTotalNum().equals(0L)) {
                        sevenReportDTO.setLoginRate(new BigDecimal(sevenReportDTO.getLoginNum()).divide(new BigDecimal(sevenReportDTO.getTotalNum())
                                , 6, BigDecimal.ROUND_HALF_UP));
                        sevenReportDTO.setIncomingTotalRate(new BigDecimal(sevenReportDTO.getIncomingNum()).divide(new BigDecimal(sevenReportDTO.
                                getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                        sevenReportDTO.setApproversTotalRate(new BigDecimal(sevenReportDTO.getApproversNum()).divide(new BigDecimal(sevenReportDTO.
                                getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                        sevenReportDTO.setLendersSucTotalRate(new BigDecimal(sevenReportDTO.getLendersSucNum()).divide(new BigDecimal(sevenReportDTO.
                                getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                        sevenReportDTO.setProductCapacity(new BigDecimal(sevenReportDTO.getLendersSucAmount()).divide(new BigDecimal(sevenReportDTO.
                                getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (!sevenReportDTO.getIncomingNum().equals(0L)) {
                        sevenReportDTO.setApproversRate(new BigDecimal(sevenReportDTO.getApproversNum()).divide(new BigDecimal(sevenReportDTO.
                                getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (!sevenReportDTO.getApproversNum().equals(0L)) {
                        sevenReportDTO.setApplyPayRate(new BigDecimal(sevenReportDTO.getApplyPayNum()).divide(new BigDecimal(sevenReportDTO.
                                getApproversNum()), 6, BigDecimal.ROUND_HALF_UP));
                        sevenReportDTO.setApprovalsAvgNum(Math.round(totalApprovalCost / (double) sevenReportDTO.getApproversNum()));
                    }
                    if (!sevenReportDTO.getApplyPaySuccessNum().equals(0L)) {
                        sevenReportDTO.setLendersSucRate(new BigDecimal(sevenReportDTO.getLendersSucNum()).divide(new BigDecimal(sevenReportDTO.
                                getApplyPaySuccessNum()), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (!sevenReportDTO.getApplyPayNum().equals(0L)) {
                        sevenReportDTO.setApplyPaySuccessRate(new BigDecimal(sevenReportDTO.getApplyPaySuccessNum()).divide(new BigDecimal(sevenReportDTO
                                .getApplyPayNum()), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    if (!sevenReportDTO.getLendersSucNum().equals(0L)) {
                        sevenReportDTO.setLendersSucAvgAmount(Math.round(sevenReportDTO.getLendersSucAmount() /
                                (double) sevenReportDTO.getLendersSucNum()));
                    }
                    if (sevenReportDTO.getCost().compareTo(BigDecimal.ZERO) != 0) {
                        sevenReportDTO.setRoi(sevenReportDTO.getIncome().divide(sevenReportDTO.getCost(), 6, BigDecimal.ROUND_HALF_UP));
                    }
                    sevenReportDTO.setLendersApproversRate(sevenReportDTO.getApproversRate().multiply(sevenReportDTO.getApplyPaySuccessRate()).
                            multiply(sevenReportDTO.getLendersSucRate()).setScale(6, BigDecimal.ROUND_HALF_UP));
                    totalList.add(sevenReportDTO);
                });
                ZhongAnBusAnalySevenReportDTO brReportDTO = totalList.get(0);
                ZhongAnBusAnalySevenReportDTO zhongAnReportDTO = totalList.get(1);
                brReportDTO.setIncomingIncreaseRate(new BigDecimal((0)));
                if ((zhongAnReportDTO.getTotalNum() != 0L) && (((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                        zhongAnReportDTO.getIncomingNum()) != 0L)) {
                    brReportDTO.setIncomingIncreaseRate(new BigDecimal(brReportDTO.getIncomingNum() - ((double) brReportDTO.getTotalNum() /
                            zhongAnReportDTO.getTotalNum() * zhongAnReportDTO.getIncomingNum()))
                            .divide(new BigDecimal((double) brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() *
                                    zhongAnReportDTO.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
                }
                brReportDTO.setApproversIncreaseRate(new BigDecimal((0)));
                if (zhongAnReportDTO.getApproversTotalRate().compareTo(BigDecimal.ZERO) != 0) {
                    brReportDTO.setApproversIncreaseRate((brReportDTO.getApproversTotalRate().subtract(zhongAnReportDTO.getApproversTotalRate()).
                            divide(zhongAnReportDTO.getApproversTotalRate(), 6, BigDecimal.ROUND_HALF_UP)));
                }
                brReportDTO.setApproversIncrNum(0L);
                brReportDTO.setLendersSucIncrAmount(0L);
                if (zhongAnReportDTO.getTotalNum() != 0L) {
                    brReportDTO.setApproversIncrNum(Math.round(brReportDTO.getApproversNum() - zhongAnReportDTO.getApproversNum() *
                            (brReportDTO.getTotalNum() / (double) zhongAnReportDTO.getTotalNum())));
                    brReportDTO.setLendersSucIncrAmount(Math.round(brReportDTO.getLendersSucAmount() - zhongAnReportDTO.getLendersSucAmount() *
                            (brReportDTO.getTotalNum() / (double) zhongAnReportDTO.getTotalNum())));
                }
                brReportDTO.setApplyPayIncrRate(new BigDecimal((0)));
                if (zhongAnReportDTO.getApplyPayRate().compareTo(BigDecimal.ZERO) != 0) {
                    brReportDTO.setApplyPayIncrRate((brReportDTO.getApplyPayRate().subtract(zhongAnReportDTO.getApplyPayRate()).
                            divide(zhongAnReportDTO.getApplyPayRate(), 6, BigDecimal.ROUND_HALF_UP)));
                }
                brReportDTO.setLendersSucIncrRate(new BigDecimal((0)));
                if (zhongAnReportDTO.getLendersSucTotalRate().compareTo(BigDecimal.ZERO) != 0) {
                    brReportDTO.setLendersSucIncrRate((brReportDTO.getLendersSucTotalRate().subtract(zhongAnReportDTO.getLendersSucTotalRate()).
                            divide(zhongAnReportDTO.getLendersSucTotalRate(), 6, BigDecimal.ROUND_HALF_UP)));
                }
                zhongAnReportDTO.setIncomingIncreaseRate(brReportDTO.getIncomingIncreaseRate());
                zhongAnReportDTO.setApproversIncreaseRate(brReportDTO.getApproversIncreaseRate());
                zhongAnReportDTO.setApproversIncrNum(brReportDTO.getApproversIncrNum());
                zhongAnReportDTO.setLendersSucIncrAmount(brReportDTO.getLendersSucIncrAmount());
                zhongAnReportDTO.setApplyPayIncrRate(brReportDTO.getApplyPayIncrRate());
                zhongAnReportDTO.setLendersSucIncrRate(brReportDTO.getLendersSucIncrRate());
                zhongAnBusAnalySevenReportList.addAll(totalList);
            }
        }
        List<ZhongAnBusAnalySevenReportDTO> zhongAnBusAnalySevenReportDTOS = traverseDatesSeven(zhongAnBusAnalySevenReportList);
        return zhongAnBusAnalySevenReportDTOS;
    }

    public static List<ZhongAnBusAnalySevenReportDTO> traverseDatesSeven(List<ZhongAnBusAnalySevenReportDTO> zhongAnBusAnalyEightReportList) {
        List<ZhongAnBusAnalySevenReportDTO> result = new ArrayList<>();

        // 获取今天的日期
        LocalDate now = LocalDate.now();
        String oneDay = LocalDate.now().withDayOfMonth(1).toString();
        String oneDayToOneDay = oneDay + "-" + oneDay;
        // 判断今天是否是1号
        if (now.getDayOfMonth() == 1) {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
        } else {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
            // 如果不是1号，从2号到今天遍历
            LocalDate firstDayOfMonth = now.withDayOfMonth(2);
            LocalDate currentDay = firstDayOfMonth;

            // 遍历从1号到今天的日期
            while (!currentDay.isAfter(now)) {
                String dateStr = currentDay.toString();
                zhongAnBusAnalyEightReportList.stream()
                        .filter(date -> date.getReportDate().contains(dateStr))
                        .forEach(result::add);

                currentDay = currentDay.plusDays(1);
            }
        }

        return result;
    }
}
