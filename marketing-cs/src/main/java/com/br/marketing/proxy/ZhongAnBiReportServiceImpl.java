package com.br.marketing.proxy;


import cn.hutool.core.util.ObjectUtil;
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
            String reportDate = beginDate + "--" + date;
            List<String> reportDateList = zhongAnBusAnalyOneReportList.stream()
                    .map(ZhongAnBusAnalyOneReportDTO::getReportDate)
                    .collect(Collectors.toList());
            List<String> groupList = Lists.newArrayList(MARKETING_IFLOGIN, ZHONGAN_IFLOGIN, MARKETING_IFNOTLOGIN, ZHONGAN_IFNOTLOGIN);
            List<ZhongAnBusAnalyOneReportDTO> totalList = new ArrayList<>();
            if (reportDateList.size() > 1) {
                LocalDate finalDate = date;
                groupList.forEach((String group) -> {
                    ZhongAnBusAnalyOneReportDTO oneReportDTO = new ZhongAnBusAnalyOneReportDTO();
                    oneReportDTO.setReportDate(reportDate);
                    oneReportDTO.setQueryDate("T" + finalDate.getDayOfMonth());
                    oneReportDTO.setConstituencies(group);
                    oneReportDTO.setTotalNum(zhongAnBusAnalyOneReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getTotalNum)
                            .distinct()
                            .reduce((a, b) -> 0L)
                            .orElse(0L));
                    oneReportDTO.setIncomingNum(zhongAnBusAnalyOneReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum)
                            .sum());
                    oneReportDTO.setApproversNum(zhongAnBusAnalyOneReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum)
                            .sum());
                    oneReportDTO.setCompositeIncrNum(zhongAnBusAnalyOneReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getCompositeIncrNum)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    oneReportDTO.setIncome(zhongAnBusAnalyOneReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getIncome)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    oneReportDTO.setCost(zhongAnBusAnalyOneReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyOneReportDTO::getCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));

                    oneReportDTO.setIncomingTotalRate(calculateRate(oneReportDTO.getIncomingNum(), oneReportDTO.getTotalNum()));
                    oneReportDTO.setApproversTotalRate(calculateRate(oneReportDTO.getApproversNum(), oneReportDTO.getTotalNum()));
                    oneReportDTO.setApproversRate(calculateRate(oneReportDTO.getApproversNum(), oneReportDTO.getIncomingNum()));
                    oneReportDTO.setRoi(calculateRoi(oneReportDTO.getIncome(), oneReportDTO.getCost()));

                    totalList.add(oneReportDTO);

                });
                //营销首登组,众安对照首登组
                ZhongAnBusAnalyOneReportDTO brReportDTO = totalList.get(0);
                ZhongAnBusAnalyOneReportDTO zhongAnReportDTO = totalList.get(1);
                brReportDTO.setIncomingIncreaseRate(calculateIncomingIncreaseRate(brReportDTO.getTotalNum(),
                        brReportDTO.getIncomingNum(), zhongAnReportDTO.getTotalNum(),
                        zhongAnReportDTO.getIncomingNum()));
                brReportDTO.setApproversIncreaseRate(calculateIncomingIncreaseRate(brReportDTO.getTotalNum(),
                        brReportDTO.getApproversNum(), zhongAnReportDTO.getTotalNum(),
                        zhongAnReportDTO.getApproversNum()));

                zhongAnReportDTO.setIncomingIncreaseRate(brReportDTO.getIncomingIncreaseRate());
                zhongAnReportDTO.setApproversIncreaseRate(brReportDTO.getApproversIncreaseRate());

                //营销非首登组,众安对照非首登组
                ZhongAnBusAnalyOneReportDTO brNoLoginReportDTO = totalList.get(2);
                ZhongAnBusAnalyOneReportDTO zhongAnNoLoginReportDTO = totalList.get(3);
                brNoLoginReportDTO.setIncomingIncreaseRate(calculateIncomingIncreaseRate(brNoLoginReportDTO.getTotalNum(),
                        brNoLoginReportDTO.getIncomingNum(), zhongAnNoLoginReportDTO.getTotalNum(),
                        zhongAnNoLoginReportDTO.getIncomingNum()));
                brNoLoginReportDTO.setApproversIncreaseRate(calculateIncomingIncreaseRate(brNoLoginReportDTO.getTotalNum(),
                        brNoLoginReportDTO.getApproversNum(), zhongAnNoLoginReportDTO.getTotalNum(),
                        zhongAnNoLoginReportDTO.getApproversNum()));

                zhongAnNoLoginReportDTO.setIncomingIncreaseRate(brNoLoginReportDTO.getIncomingIncreaseRate());
                zhongAnNoLoginReportDTO.setApproversIncreaseRate(brNoLoginReportDTO.getApproversIncreaseRate());
                zhongAnBusAnalyOneReportList.addAll(totalList);
            }
        }
        List<ZhongAnBusAnalyOneReportDTO> zhongAnBusAnalyOneReportDTOS =
                traverseDatesOne(zhongAnBusAnalyOneReportList, beginDate, endDate);
        return zhongAnBusAnalyOneReportDTOS;
    }

    public static List<ZhongAnBusAnalyOneReportDTO> traverseDatesOne(List<ZhongAnBusAnalyOneReportDTO> zhongAnBusAnalyEightReportList,
                                                                     LocalDate beginDate, LocalDate endDate) {
        List<ZhongAnBusAnalyOneReportDTO> result = new ArrayList<>();

        String oneDay = beginDate.withDayOfMonth(1).toString();
        String oneDayToOneDay = beginDate + "--" + beginDate;
        // 判断是否是1号
        if (endDate.getDayOfMonth() == 1) {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
        } else {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
            // 如果不是1号，从2号到今天遍历
            LocalDate firstDayOfMonth = endDate.withDayOfMonth(2);
            LocalDate currentDay = firstDayOfMonth;

            // 遍历从1号到今天的日期
            while (!currentDay.isAfter(endDate)) {
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

        List<String> groupList = Lists.newArrayList(BR_MARKETING, ZHONGAN_MARKETING);
        for (LocalDate date = beginDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            String reportDate = beginDate + "--" + date;
            List<String> reportDateList = zhongAnBusAnalyEightReportList.stream()
                    .map(ZhongAnBusAnalyEightReportDTO::getReportDate)
                    .collect(Collectors.toList());
            List<ZhongAnBusAnalyEightReportDTO> totalList = new ArrayList<>();
            if (reportDateList.size() > 1) {
                LocalDate finalDate = date;
                groupList.forEach((String group) -> {
                    ZhongAnBusAnalyEightReportDTO eightReportDTO = new ZhongAnBusAnalyEightReportDTO();
                    eightReportDTO.setReportDate(reportDate);
                    eightReportDTO.setQueryDate("T" + finalDate.getDayOfMonth());
                    eightReportDTO.setConstituencies(group);
                    eightReportDTO.setTotalNum(zhongAnBusAnalyEightReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getTotalNum)
                            .distinct()
                            .reduce((a, b) -> 0L)
                            .orElse(0L));
                    eightReportDTO.setIncomingNum(zhongAnBusAnalyEightReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .mapToLong(ZhongAnBusAnalyEightReportDTO::getIncomingNum)
                            .sum());
                    eightReportDTO.setApproversNum(zhongAnBusAnalyEightReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalyEightReportDTO::getApproversNum)
                            .sum());
                    eightReportDTO.setCompositeIncrNum(zhongAnBusAnalyEightReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getCompositeIncrNum)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    eightReportDTO.setIncome(zhongAnBusAnalyEightReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getIncome)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    eightReportDTO.setCost(zhongAnBusAnalyEightReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalyEightReportDTO::getCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    eightReportDTO.setIncomingTotalRate(calculateRate(eightReportDTO.getIncomingNum(), eightReportDTO.getTotalNum()));

                    eightReportDTO.setApproversTotalRate(calculateRate(eightReportDTO.getApproversNum(), eightReportDTO.getTotalNum()));
                    eightReportDTO.setApproversRate(calculateRate(eightReportDTO.getApproversNum(), eightReportDTO.getIncomingNum()));
                    eightReportDTO.setRoi(calculateRoi(eightReportDTO.getIncome(), eightReportDTO.getCost()));
                    totalList.add(eightReportDTO);

                });
                ZhongAnBusAnalyEightReportDTO brReportDTO = totalList.get(0);
                ZhongAnBusAnalyEightReportDTO zhongAnReportDTO = totalList.get(1);
                brReportDTO.setIncomingIncreaseRate(
                        calculateIncomingIncreaseRate(brReportDTO.getTotalNum(), brReportDTO.getIncomingNum(),
                                zhongAnReportDTO.getTotalNum(), zhongAnReportDTO.getIncomingNum()));
                brReportDTO.setApproversIncreaseRate(calculateIncomingIncreaseRate(brReportDTO.getTotalNum(),
                        brReportDTO.getApproversNum(), zhongAnReportDTO.getTotalNum(),
                        zhongAnReportDTO.getApproversNum()));
                zhongAnReportDTO.setIncomingIncreaseRate(brReportDTO.getIncomingIncreaseRate());
                zhongAnReportDTO.setApproversIncreaseRate(brReportDTO.getApproversIncreaseRate());
                zhongAnBusAnalyEightReportList.addAll(totalList);
            }

        }

        List<ZhongAnBusAnalyEightReportDTO> zhongAnBusAnalyEightReportDTOS =
                traverseDates(zhongAnBusAnalyEightReportList, beginDate, endDate);

        return zhongAnBusAnalyEightReportDTOS;
    }

    public static List<ZhongAnBusAnalyEightReportDTO> traverseDates(List<ZhongAnBusAnalyEightReportDTO> zhongAnBusAnalyEightReportList,
                                                                    LocalDate beginDate, LocalDate endDate) {
        List<ZhongAnBusAnalyEightReportDTO> result = new ArrayList<>();

        String oneDay = beginDate.withDayOfMonth(1).toString();
        String oneDayToOneDay = beginDate + "--" + beginDate;
        // 判断是否是1号
        if (endDate.getDayOfMonth() == 1) {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
        } else {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
            // 如果不是1号，从2号到今天遍历
            LocalDate firstDayOfMonth = endDate.withDayOfMonth(2);
            LocalDate currentDay = firstDayOfMonth;

            // 遍历从1号到今天的日期
            while (!currentDay.isAfter(endDate)) {
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
            String reportDate = beginDate + "--" + date;
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
                    sevenReportDTO.setTotalNum(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalySevenReportDTO::getTotalNum)
                            .distinct()
                            .reduce((a, b) -> 0L)
                            .orElse(0L));
                    sevenReportDTO.setLoginNum(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getLoginNum)
                            .sum());
                    sevenReportDTO.setIncomingNum(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getIncomingNum)
                            .sum());
                    sevenReportDTO.setApproversNum(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getApproversNum)
                            .sum());
                    sevenReportDTO.setApplyPayNum(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getApplyPayNum)
                            .sum());
                    sevenReportDTO.setApplyPaySuccessNum(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getApplyPaySuccessNum)
                            .sum());
                    sevenReportDTO.setLendersSucNum(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getLendersSucNum)
                            .sum());
                    sevenReportDTO.setLendersSucAmount(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group)).
                            mapToLong(ZhongAnBusAnalySevenReportDTO::getLendersSucAmount)
                            .sum());
                    sevenReportDTO.setIncome(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalySevenReportDTO::getIncome)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    sevenReportDTO.setCost(zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(ZhongAnBusAnalySevenReportDTO::getCost)
                            .reduce(BigDecimal.ZERO, BigDecimal::add));
                    Long totalApprovalCost = zhongAnBusAnalySevenReportList.stream()
                            .filter(t -> !t.getReportDate().contains("--"))
                            .filter(t -> LocalDate.parse(t.getReportDate()).isBefore(finalDate.plusDays(1)))
                            .filter(t -> t.getConstituencies().equals(group))
                            .map(report -> report.getApprovalsAvgNum() * report.getApproversNum())
                            .reduce(0L, Long::sum);
                    sevenReportDTO.setApprovalsAvgNum(0L);
                    sevenReportDTO.setLendersSucAvgAmount(0L);

                    sevenReportDTO.setLoginRate(calculateRate(sevenReportDTO.getLoginNum(), sevenReportDTO.getTotalNum()));
                    sevenReportDTO.setIncomingTotalRate(calculateRate(sevenReportDTO.getIncomingNum(), sevenReportDTO.getTotalNum()));
                    sevenReportDTO.setApproversTotalRate(calculateRate(sevenReportDTO.getApproversNum(), sevenReportDTO.getTotalNum()));
                    sevenReportDTO.setLendersSucTotalRate(calculateRate(sevenReportDTO.getLendersSucNum(), sevenReportDTO.getTotalNum()));
                    sevenReportDTO.setProductCapacity(calculateRate(sevenReportDTO.getLendersSucAmount(), sevenReportDTO.getTotalNum()));
                    sevenReportDTO.setApproversRate(calculateRate(sevenReportDTO.getApproversNum(), sevenReportDTO.getIncomingNum()));
                    sevenReportDTO.setApplyPayRate(calculateRate(sevenReportDTO.getApplyPayNum(), sevenReportDTO.getApproversNum()));
                    sevenReportDTO.setLendersSucRate(calculateRate(sevenReportDTO.getLendersSucNum(), sevenReportDTO.getApplyPaySuccessNum()));
                    sevenReportDTO.setApplyPaySuccessRate(calculateRate(sevenReportDTO.getApplyPaySuccessNum(), sevenReportDTO.getApplyPayNum()));

                    if (ObjectUtil.isNotEmpty(sevenReportDTO.getApproversNum()) && !sevenReportDTO.getApproversNum().equals(0L)) {
                        sevenReportDTO.setApprovalsAvgNum(Math.round(totalApprovalCost / (double) sevenReportDTO.getApproversNum()));
                    }

                    if (ObjectUtil.isNotEmpty(sevenReportDTO.getLendersSucNum()) && !sevenReportDTO.getLendersSucNum().equals(0L)) {
                        sevenReportDTO.setLendersSucAvgAmount(Math.round(sevenReportDTO.getLendersSucAmount() /
                                (double) sevenReportDTO.getLendersSucNum()));
                    }
                    sevenReportDTO.setRoi(calculateRoi(sevenReportDTO.getIncome(), sevenReportDTO.getCost()));

                    sevenReportDTO.setLendersApproversRate(sevenReportDTO.getApproversRate().multiply(sevenReportDTO.getApplyPaySuccessRate()).
                            multiply(sevenReportDTO.getLendersSucRate()).setScale(6, BigDecimal.ROUND_HALF_UP));
                    totalList.add(sevenReportDTO);
                });

                ZhongAnBusAnalySevenReportDTO brReportDTO = totalList.get(0);
                ZhongAnBusAnalySevenReportDTO zhongAnReportDTO = totalList.get(1);
                brReportDTO.setIncomingIncreaseRate(calculateIncomingIncreaseRate(brReportDTO.getTotalNum(),
                        brReportDTO.getIncomingNum(), zhongAnReportDTO.getTotalNum(), zhongAnReportDTO.getIncomingNum()));
                brReportDTO.setApproversIncreaseRate(calculateApproversIncreaseRate(brReportDTO.getApproversTotalRate(),
                        zhongAnReportDTO.getApproversTotalRate()));

                brReportDTO.setApproversIncrNum(calculateApproversIncrNum(brReportDTO.getApproversNum(),
                        zhongAnReportDTO.getApproversNum(), brReportDTO.getTotalNum(), zhongAnReportDTO.getTotalNum()));
                brReportDTO.setLendersSucIncrAmount(calculateApproversIncrNum(brReportDTO.getLendersSucAmount(),
                        zhongAnReportDTO.getLendersSucAmount(), brReportDTO.getTotalNum(), zhongAnReportDTO.getTotalNum()));

                brReportDTO.setApplyPayIncrRate(calculateApproversIncreaseRate(brReportDTO.getApplyPayRate(),
                        zhongAnReportDTO.getApplyPayRate()));
                brReportDTO.setLendersSucIncrRate(calculateApproversIncreaseRate(brReportDTO.getLendersSucTotalRate(),
                        zhongAnReportDTO.getLendersSucTotalRate()));

                zhongAnReportDTO.setIncomingIncreaseRate(brReportDTO.getIncomingIncreaseRate());
                zhongAnReportDTO.setApproversIncreaseRate(brReportDTO.getApproversIncreaseRate());
                zhongAnReportDTO.setApproversIncrNum(brReportDTO.getApproversIncrNum());
                zhongAnReportDTO.setLendersSucIncrAmount(brReportDTO.getLendersSucIncrAmount());
                zhongAnReportDTO.setApplyPayIncrRate(brReportDTO.getApplyPayIncrRate());
                zhongAnReportDTO.setLendersSucIncrRate(brReportDTO.getLendersSucIncrRate());
                zhongAnBusAnalySevenReportList.addAll(totalList);
            }
        }
        List<ZhongAnBusAnalySevenReportDTO> zhongAnBusAnalySevenReportDTOS =
                traverseDatesSeven(zhongAnBusAnalySevenReportList, beginDate, endDate);
        return zhongAnBusAnalySevenReportDTOS;
    }

    public static List<ZhongAnBusAnalySevenReportDTO> traverseDatesSeven(List<ZhongAnBusAnalySevenReportDTO> zhongAnBusAnalyEightReportList,
                                                                         LocalDate beginDate, LocalDate endDate) {
        List<ZhongAnBusAnalySevenReportDTO> result = new ArrayList<>();

        String oneDay = beginDate.withDayOfMonth(1).toString();
        String oneDayToOneDay = beginDate + "--" + beginDate;
        // 判断是否是1号
        if (endDate.getDayOfMonth() == 1) {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
        } else {
            zhongAnBusAnalyEightReportList.stream()
                    .filter(date -> date.getReportDate().equals(oneDay) || date.getReportDate().equals(oneDayToOneDay))
                    .forEach(result::add);
            // 如果不是1号，从2号到今天遍历
            LocalDate firstDayOfMonth = endDate.withDayOfMonth(2);
            LocalDate currentDay = firstDayOfMonth;

            // 遍历从1号到今天的日期
            while (!currentDay.isAfter(endDate)) {
                String dateStr = currentDay.toString();
                zhongAnBusAnalyEightReportList.stream()
                        .filter(date -> date.getReportDate().contains(dateStr))
                        .forEach(result::add);

                currentDay = currentDay.plusDays(1);
            }
        }

        return result;
    }

    public static BigDecimal calculateRate(Long numerator, Long denominator) {
        if (numerator != null && numerator != 0L && denominator != null && denominator != 0L) {
            return new BigDecimal(numerator).divide(new BigDecimal(denominator), 6, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal calculateIncomingIncreaseRate(Long brTotalNum, Long brIncomingNum, Long zhongAnTotalNum, Long zhongAnIncomingNum) {
        if (ObjectUtil.isEmpty(zhongAnTotalNum) || ObjectUtil.isEmpty(zhongAnIncomingNum) || zhongAnTotalNum == 0L || zhongAnIncomingNum == 0L) {
            return BigDecimal.ZERO;
        }

        if (ObjectUtil.isEmpty(brTotalNum) || brTotalNum == 0L) {
            return BigDecimal.ZERO;
        }
        double numerator = brIncomingNum - ((double) brTotalNum / zhongAnTotalNum * zhongAnIncomingNum);

        double denominator = (double) brTotalNum / zhongAnTotalNum * zhongAnIncomingNum;

        if (denominator == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 6, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal calculateApproversIncreaseRate(BigDecimal brApproversTotalRate, BigDecimal zhongAnApproversTotalRate) {
        if (brApproversTotalRate == null || zhongAnApproversTotalRate == null) {
            return BigDecimal.ZERO;
        }

        if (zhongAnApproversTotalRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return brApproversTotalRate.subtract(zhongAnApproversTotalRate)
                .divide(zhongAnApproversTotalRate, 6, BigDecimal.ROUND_HALF_UP);
    }

    public static long calculateApproversIncrNum(Long brApproversNum, Long zhongAnApproversNum, Long brTotalNum, Long zhongAnTotalNum) {
        if (brApproversNum == null || zhongAnApproversNum == null || brTotalNum == null || zhongAnTotalNum == null) {
            return 0L;
        }

        if (zhongAnTotalNum == 0) {
            return 0L;
        }

        // 执行计算
        double result = brApproversNum - zhongAnApproversNum * (brTotalNum / (double) zhongAnTotalNum);
        return Math.round(result);
    }

    public static BigDecimal calculateRoi(BigDecimal income, BigDecimal cost) {
        if (income == null || cost == null || cost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.divide(cost, 6, BigDecimal.ROUND_HALF_UP);
    }
}
