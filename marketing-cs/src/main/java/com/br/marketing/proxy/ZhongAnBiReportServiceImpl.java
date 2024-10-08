package com.br.marketing.proxy;


import com.br.marketing.common.annoation.PercentConvertor;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyEightReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyOneReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalySevenReportDTO;
import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        String reportDate = zhongAnBusAnalyOneReportList.get(0).getReportDate() + "-" +
                zhongAnBusAnalyOneReportList.get(zhongAnBusAnalyOneReportList.size() - 1).getReportDate();
        List<String> reportDateList = zhongAnBusAnalyOneReportList.stream().map(ZhongAnBusAnalyOneReportDTO::getReportDate).distinct()
                .collect(Collectors.toList());
        //超过一天，计算总计
        if (reportDateList.size() > 1) {
            ZhongAnBusAnalyOneReportDTO marketingIfLogin = new ZhongAnBusAnalyOneReportDTO();
            ZhongAnBusAnalyOneReportDTO marketingIfNoLogin = new ZhongAnBusAnalyOneReportDTO();
            ZhongAnBusAnalyOneReportDTO zhongAnIfLogin = new ZhongAnBusAnalyOneReportDTO();
            ZhongAnBusAnalyOneReportDTO zhongAnIfNoLogin = new ZhongAnBusAnalyOneReportDTO();
            marketingIfLogin.setReportDate(reportDate);
            marketingIfLogin.setConstituencies(MARKETING_IFLOGIN);
            marketingIfLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            marketingIfLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            marketingIfLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
            marketingIfLogin.setCompositeIncrNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getCompositeIncrNum).sum());
            marketingIfLogin.setCost(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getCost).reduce(BigDecimal.ZERO, BigDecimal::add));
            marketingIfLogin.setIncome(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add));
            marketingIfLogin.setRoi(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getRoi).reduce(BigDecimal.ZERO, BigDecimal::add));
            if (marketingIfLogin.getTotalNum().equals(0L)) {
                marketingIfLogin.setIncomingTotalRate(new BigDecimal(0));
                marketingIfLogin.setApproversTotalRate(new BigDecimal(0));
            } else {
                marketingIfLogin.setIncomingTotalRate(new BigDecimal(marketingIfLogin.getIncomingNum()).divide(new BigDecimal(marketingIfLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                marketingIfLogin.setApproversTotalRate((new BigDecimal(marketingIfLogin.getApproversNum()).divide(new BigDecimal(marketingIfLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP)));
            }
            if (marketingIfLogin.getIncomingNum().equals(0L)) {
                marketingIfLogin.setApproversRate(new BigDecimal(0));
            } else {
                marketingIfLogin.setApproversRate(new BigDecimal(marketingIfLogin.getApproversNum()).divide(new BigDecimal(marketingIfLogin.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
            }
            //众安对照首登组
            zhongAnIfLogin.setReportDate(reportDate);
            zhongAnIfLogin.setConstituencies(ZHONGAN_IFLOGIN);
            zhongAnIfLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(ZHONGAN_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            zhongAnIfLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(ZHONGAN_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            zhongAnIfLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(ZHONGAN_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
            if (zhongAnIfLogin.getTotalNum().equals(0L)) {
                zhongAnIfLogin.setIncomingTotalRate(new BigDecimal(0));
                zhongAnIfLogin.setApproversTotalRate((new BigDecimal(0)));
            } else {
                zhongAnIfLogin.setIncomingTotalRate(new BigDecimal(zhongAnIfLogin.getIncomingNum()).divide(new BigDecimal(zhongAnIfLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                zhongAnIfLogin.setApproversTotalRate((new BigDecimal(zhongAnIfLogin.getApproversNum()).divide(new BigDecimal(zhongAnIfLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP)));
            }
            if (zhongAnIfLogin.getIncomingNum().equals(0L)) {
                zhongAnIfLogin.setApproversRate(new BigDecimal(0));
            } else {
                zhongAnIfLogin.setApproversRate(new BigDecimal(zhongAnIfLogin.getApproversNum()).divide(new BigDecimal(zhongAnIfLogin.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
            }
            if ((zhongAnIfLogin.getTotalNum() != 0L) && ((marketingIfLogin.getTotalNum() / zhongAnIfLogin.getTotalNum() * zhongAnIfLogin.getIncomingNum()) != 0L)) {
                marketingIfLogin.setIncomingIncreaseRate(new BigDecimal(marketingIfLogin.getIncomingNum() - (marketingIfLogin.getTotalNum() / zhongAnIfLogin.getTotalNum() * zhongAnIfLogin.getIncomingNum()))
                        .divide(new BigDecimal((marketingIfLogin.getTotalNum() / zhongAnIfLogin.getTotalNum()) * zhongAnIfLogin.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
            } else {
                marketingIfLogin.setIncomingIncreaseRate(new BigDecimal(0));
            }
            if ((zhongAnIfLogin.getTotalNum() != 0L) && ((marketingIfLogin.getTotalNum() / zhongAnIfLogin.getTotalNum() * zhongAnIfLogin.getApproversNum()) != 0L)) {
                marketingIfLogin.setApproversIncreaseRate(new BigDecimal(marketingIfLogin.getApproversNum() - ((marketingIfLogin.getTotalNum() / zhongAnIfLogin.getTotalNum()) * zhongAnIfLogin.getApproversNum()))
                        .divide(new BigDecimal((marketingIfLogin.getTotalNum() / zhongAnIfLogin.getTotalNum()) * zhongAnIfLogin.getApproversNum()), 6, BigDecimal.ROUND_HALF_UP));
            } else {
                marketingIfLogin.setApproversIncreaseRate(new BigDecimal(0));
            }

            zhongAnIfLogin.setIncomingIncreaseRate(marketingIfLogin.getIncomingIncreaseRate());
            zhongAnIfLogin.setApproversIncreaseRate(marketingIfLogin.getApproversIncreaseRate());
            zhongAnIfLogin.setCompositeIncrNum(marketingIfLogin.getCompositeIncrNum());
            zhongAnIfLogin.setIncome(marketingIfLogin.getIncome());
            zhongAnIfLogin.setCost(marketingIfLogin.getCost());
            zhongAnIfLogin.setRoi(marketingIfLogin.getRoi());
            //营销非首登组
            marketingIfNoLogin.setReportDate(reportDate);
            marketingIfNoLogin.setConstituencies(MARKETING_IFNOTLOGIN);
            marketingIfNoLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            marketingIfNoLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            marketingIfNoLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
            marketingIfNoLogin.setCompositeIncrNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getCompositeIncrNum).sum());
            marketingIfNoLogin.setCost(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getCost).reduce(BigDecimal.ZERO, BigDecimal::add));
            marketingIfNoLogin.setIncome(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add));
            marketingIfNoLogin.setRoi(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getRoi).reduce(BigDecimal.ZERO, BigDecimal::add));
            if (marketingIfNoLogin.getTotalNum().equals(0L)) {
                marketingIfNoLogin.setIncomingTotalRate(new BigDecimal(0));
                marketingIfNoLogin.setApproversTotalRate(new BigDecimal(0));
            } else {
                marketingIfNoLogin.setIncomingTotalRate(new BigDecimal(marketingIfNoLogin.getIncomingNum()).divide(new BigDecimal(marketingIfNoLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                marketingIfNoLogin.setApproversTotalRate((new BigDecimal(marketingIfNoLogin.getApproversNum()).divide(new BigDecimal(marketingIfNoLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP)));
            }
            if (marketingIfNoLogin.getIncomingNum().equals(0L)) {
                marketingIfNoLogin.setApproversRate(new BigDecimal(0));
            } else {
                marketingIfNoLogin.setApproversRate(new BigDecimal(marketingIfNoLogin.getApproversNum()).divide(new BigDecimal(marketingIfNoLogin.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
            }
            //众安对照非首登组
            zhongAnIfNoLogin.setReportDate(reportDate);
            zhongAnIfNoLogin.setConstituencies(ZHONGAN_IFNOTLOGIN);
            zhongAnIfNoLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(ZHONGAN_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            zhongAnIfNoLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(ZHONGAN_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            zhongAnIfNoLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t -> t.getConstituencies().equals(ZHONGAN_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());

            if (zhongAnIfNoLogin.getTotalNum().equals(0L)) {
                zhongAnIfNoLogin.setIncomingTotalRate(new BigDecimal(0));
                zhongAnIfNoLogin.setApproversTotalRate(new BigDecimal(0));
            } else {
                zhongAnIfNoLogin.setIncomingTotalRate(new BigDecimal(zhongAnIfNoLogin.getIncomingNum()).divide(new BigDecimal(zhongAnIfNoLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                zhongAnIfNoLogin.setApproversTotalRate((new BigDecimal(zhongAnIfNoLogin.getApproversNum()).divide(new BigDecimal(zhongAnIfNoLogin.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP)));
            }
            if (zhongAnIfNoLogin.getIncomingNum().equals(0L)) {
                zhongAnIfNoLogin.setApproversRate(new BigDecimal(0));
            } else {
                zhongAnIfNoLogin.setApproversRate(new BigDecimal(zhongAnIfNoLogin.getApproversNum()).divide(new BigDecimal(zhongAnIfNoLogin.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
            }
            if ((zhongAnIfNoLogin.getTotalNum() != 0L) && ((marketingIfNoLogin.getTotalNum() / zhongAnIfNoLogin.getTotalNum() * zhongAnIfNoLogin.getIncomingNum()) != 0L)) {
                marketingIfNoLogin.setIncomingIncreaseRate(new BigDecimal(marketingIfNoLogin.getIncomingNum() - (marketingIfNoLogin.getTotalNum() / zhongAnIfNoLogin.getTotalNum() * zhongAnIfNoLogin.getIncomingNum()))
                        .divide(new BigDecimal((marketingIfNoLogin.getTotalNum() / zhongAnIfNoLogin.getTotalNum()) * zhongAnIfNoLogin.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
            } else {
                marketingIfNoLogin.setIncomingIncreaseRate(new BigDecimal(0));
            }
            if ((zhongAnIfNoLogin.getTotalNum() != 0L) && ((marketingIfNoLogin.getTotalNum() / zhongAnIfNoLogin.getTotalNum()) * zhongAnIfNoLogin.getApproversNum()) != 0L) {
                marketingIfNoLogin.setApproversIncreaseRate(new BigDecimal(marketingIfNoLogin.getApproversNum() - ((marketingIfNoLogin.getTotalNum() / zhongAnIfNoLogin.getTotalNum()) * zhongAnIfNoLogin.getApproversNum()))
                        .divide(new BigDecimal((marketingIfNoLogin.getTotalNum() / zhongAnIfNoLogin.getTotalNum()) * zhongAnIfNoLogin.getApproversNum()), 6, BigDecimal.ROUND_HALF_UP));
            } else {
                marketingIfNoLogin.setApproversIncreaseRate(new BigDecimal(0));
            }
            zhongAnIfNoLogin.setIncomingIncreaseRate(marketingIfNoLogin.getIncomingIncreaseRate());
            zhongAnIfNoLogin.setApproversIncreaseRate(marketingIfNoLogin.getApproversIncreaseRate());
            zhongAnIfNoLogin.setCompositeIncrNum(marketingIfNoLogin.getCompositeIncrNum());
            zhongAnIfNoLogin.setIncome(marketingIfNoLogin.getIncome());
            zhongAnIfNoLogin.setCost(marketingIfNoLogin.getCost());
            zhongAnIfNoLogin.setRoi(marketingIfNoLogin.getRoi());

            zhongAnBusAnalyOneReportList.add(marketingIfLogin);
            zhongAnBusAnalyOneReportList.add(marketingIfNoLogin);
            zhongAnBusAnalyOneReportList.add(zhongAnIfLogin);
            zhongAnBusAnalyOneReportList.add(zhongAnIfNoLogin);
        }
        return zhongAnBusAnalyOneReportList;
    }

    @Override
    public List<ZhongAnBusAnalyEightReportDTO> selectZaBusAnalyEightListbI_(String reportId) {
        return zhongAnBiReportMapper.selectZaBusAnalyEightListbI_(reportId);
    }

    @Override
    public List<ZhongAnBusAnalySevenReportDTO> selectZaBusAnalySeveListbI_(String reportId) {
        List<ZhongAnBusAnalySevenReportDTO> zhongAnBusAnalySevenReportList = zhongAnBiReportMapper.selectZaBusAnalySevenListbI_(reportId);
        String reportDate = zhongAnBusAnalySevenReportList.get(0).getReportDate() + "-" +
                zhongAnBusAnalySevenReportList.get(zhongAnBusAnalySevenReportList.size() - 1).getReportDate();
        List<String> reportDateList = zhongAnBusAnalySevenReportList.stream().map(ZhongAnBusAnalySevenReportDTO::getReportDate).distinct()
                .collect(Collectors.toList());
        List<String> groupList = Lists.newArrayList(BR_MARKETING, ZHONGAN_MARKETING);
        List<ZhongAnBusAnalySevenReportDTO> totalList = new ArrayList<>();
        if (reportDateList.size() > 1) {
            groupList.forEach(group -> {
                ZhongAnBusAnalySevenReportDTO sevenReportDTO = new ZhongAnBusAnalySevenReportDTO();
                sevenReportDTO.setReportDate(reportDate);
                sevenReportDTO.setConstituencies(group);
                sevenReportDTO.setTotalNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group))
                        .map(ZhongAnBusAnalySevenReportDTO::getTotalNum).findFirst().orElse(null));
                sevenReportDTO.setLoginNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                        mapToLong(ZhongAnBusAnalySevenReportDTO::getLoginNum).sum());
                sevenReportDTO.setIncomingNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                        mapToLong(ZhongAnBusAnalySevenReportDTO::getIncomingNum).sum());
                sevenReportDTO.setApproversNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                        mapToLong(ZhongAnBusAnalySevenReportDTO::getApproversNum).sum());
                sevenReportDTO.setApprovalsAvgNum(zhongAnBusAnalySevenReportList.stream().filter(t -> t.getConstituencies().equals(group)).
                        mapToLong(ZhongAnBusAnalySevenReportDTO::getApprovalsAvgNum).sum());
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
                sevenReportDTO.setLendersSucAvgAmount(0L);
                sevenReportDTO.setRoi(new BigDecimal((0)));
                sevenReportDTO.setProductCapacity(new BigDecimal((0)));
                //TODO 提现通过通过率
                sevenReportDTO.setApplyPaySuccessRate(new BigDecimal((0)));
                sevenReportDTO.setLoginRate(new BigDecimal((0)));
                sevenReportDTO.setIncomingTotalRate(new BigDecimal((0)));
                sevenReportDTO.setApproversRate(new BigDecimal((0)));
                sevenReportDTO.setApproversTotalRate(new BigDecimal((0)));
                sevenReportDTO.setApplyPayRate(new BigDecimal((0)));
                sevenReportDTO.setLendersSucRate(new BigDecimal((0)));
                sevenReportDTO.setLendersSucTotalRate(new BigDecimal((0)));
                if (!sevenReportDTO.getTotalNum().equals(0L)) {
                    sevenReportDTO.setLoginRate(new BigDecimal(sevenReportDTO.getLoginNum()).divide(new BigDecimal(sevenReportDTO.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                    sevenReportDTO.setIncomingTotalRate(new BigDecimal(sevenReportDTO.getIncomingNum()).divide(new BigDecimal(sevenReportDTO.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                    sevenReportDTO.setApproversTotalRate(new BigDecimal(sevenReportDTO.getApproversNum()).divide(new BigDecimal(sevenReportDTO.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                    sevenReportDTO.setLendersSucTotalRate(new BigDecimal(sevenReportDTO.getLendersSucNum()).divide(new BigDecimal(sevenReportDTO.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                    sevenReportDTO.setProductCapacity(new BigDecimal(sevenReportDTO.getLendersSucAmount()).divide(new BigDecimal(sevenReportDTO.getTotalNum()), 6, BigDecimal.ROUND_HALF_UP));
                }
                if (!sevenReportDTO.getIncomingNum().equals(0L)) {
                    sevenReportDTO.setApproversRate(new BigDecimal(sevenReportDTO.getApproversNum()).divide(new BigDecimal(sevenReportDTO.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));
                }
                if (!sevenReportDTO.getApproversNum().equals(0L)) {
                    sevenReportDTO.setApplyPayRate(new BigDecimal(sevenReportDTO.getApplyPayNum()).divide(new BigDecimal(sevenReportDTO.getApproversNum()), 6, BigDecimal.ROUND_HALF_UP));
                }
                if (!sevenReportDTO.getApplyPaySuccessNum().equals(0L)) {
                    sevenReportDTO.setLendersSucRate(new BigDecimal(sevenReportDTO.getLendersSucNum()).divide(new BigDecimal(sevenReportDTO.getApplyPaySuccessNum()), 6, BigDecimal.ROUND_HALF_UP));
                }
                if (!sevenReportDTO.getLendersSucNum().equals(0L)) {
                    sevenReportDTO.setLendersSucAvgAmount(Math.round(sevenReportDTO.getLendersSucAmount() / (double) sevenReportDTO.getLendersSucNum()));
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
            if ((zhongAnReportDTO.getTotalNum() != 0L) && ((brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() * zhongAnReportDTO.getIncomingNum()) != 0L)) {
                brReportDTO.setIncomingIncreaseRate(new BigDecimal(brReportDTO.getIncomingNum() - (brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() * zhongAnReportDTO.getIncomingNum()))
                        .divide(new BigDecimal(brReportDTO.getTotalNum() / zhongAnReportDTO.getTotalNum() * zhongAnReportDTO.getIncomingNum()), 6, BigDecimal.ROUND_HALF_UP));

            }
            brReportDTO.setApproversIncreaseRate(new BigDecimal((0)));
            if (zhongAnReportDTO.getApproversTotalRate().compareTo(BigDecimal.ZERO) != 0) {
                brReportDTO.setApproversIncreaseRate((brReportDTO.getApproversTotalRate().subtract(zhongAnReportDTO.getApproversTotalRate()).divide(zhongAnReportDTO.getApproversTotalRate(), 6, BigDecimal.ROUND_HALF_UP)));
            }
            brReportDTO.setApproversIncrNum(0L);
            brReportDTO.setLendersSucIncrAmount(0L);
            if (zhongAnReportDTO.getTotalNum() != 0L) {
                brReportDTO.setApproversIncrNum(Math.round(brReportDTO.getApproversNum() - zhongAnReportDTO.getApproversNum() * (zhongAnReportDTO.getTotalNum()
                        / (double) zhongAnReportDTO.getTotalNum())));
                brReportDTO.setLendersSucIncrAmount(Math.round(brReportDTO.getLendersSucAmount() - zhongAnReportDTO.getLendersSucAmount() * (brReportDTO.getTotalNum()
                        / (double) zhongAnReportDTO.getTotalNum())));
            }
            brReportDTO.setApplyPayIncrRate(new BigDecimal((0)));
            if (zhongAnReportDTO.getApplyPayRate().compareTo(BigDecimal.ZERO) != 0) {
                brReportDTO.setApplyPayIncrRate((brReportDTO.getApplyPayRate().subtract(zhongAnReportDTO.getApplyPayRate()).divide(zhongAnReportDTO.getApplyPayRate(), 6, BigDecimal.ROUND_HALF_UP)));
            }
            brReportDTO.setLendersSucIncrRate(new BigDecimal((0)));
            if (zhongAnReportDTO.getLendersSucTotalRate().compareTo(BigDecimal.ZERO) != 0) {
                brReportDTO.setLendersSucIncrRate((brReportDTO.getLendersSucTotalRate().subtract(zhongAnReportDTO.getLendersSucTotalRate()).divide(zhongAnReportDTO.getLendersSucTotalRate(), 6, BigDecimal.ROUND_HALF_UP)));
            }
            zhongAnReportDTO.setIncomingIncreaseRate(brReportDTO.getIncomingIncreaseRate());
            zhongAnReportDTO.setApproversIncreaseRate(brReportDTO.getApproversIncreaseRate());
            zhongAnReportDTO.setApproversIncrNum(brReportDTO.getApproversIncrNum());
            zhongAnReportDTO.setLendersSucIncrAmount(brReportDTO.getLendersSucIncrAmount());
            zhongAnReportDTO.setApplyPayIncrRate(brReportDTO.getApplyPayIncrRate());
            zhongAnReportDTO.setLendersSucIncrRate(brReportDTO.getLendersSucIncrRate());
            zhongAnBusAnalySevenReportList.addAll(totalList);
        }
        return zhongAnBusAnalySevenReportList;
    }
}
