package com.br.marketing.proxy;


import com.br.marketing.common.annoation.PercentConvertor;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyEightReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalyOneReportDTO;
import com.br.marketing.dto.report.zhongan.ZhongAnBusAnalySevenReportDTO;
import com.br.marketing.entity.SourceStatisticDict;
import com.br.marketing.mapper.ZhongAnBiReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@PercentConvertor
public class ZhongAnBiReportServiceImpl implements ZhongAnBiReportService{

    @Autowired
    ZhongAnBiReportMapper zhongAnBiReportMapper;

    private static final String MARKETING_IFLOGIN = "营销首登组";

    private static final String MARKETING_IFNOTLOGIN = "营销非首登组";

    private static final String ZHONGAN_IFLOGIN = "众安对照首登组";

    private static final String ZHONGAN_IFNOTLOGIN = "众安对照非首登组";


    @Override
    public List<ZhongAnBusAnalyOneReportDTO> selectZaBusAnalyOneListbI_(String reportId) {

        List<ZhongAnBusAnalyOneReportDTO> zhongAnBusAnalyOneReportList = zhongAnBiReportMapper.selectZaBusAnalyOneListbI_(reportId);
        String reportDate = zhongAnBusAnalyOneReportList.get(0).getReportDate()+"-"+
                zhongAnBusAnalyOneReportList.get(zhongAnBusAnalyOneReportList.size()-1).getReportDate();

        List<String> reportDateList = zhongAnBusAnalyOneReportList.stream().map(ZhongAnBusAnalyOneReportDTO::getReportDate).distinct()
                .collect(Collectors.toList());
        //超过一天，计算总计
        if(reportDateList.size()>1){
            ZhongAnBusAnalyOneReportDTO  marketingIfLogin = new ZhongAnBusAnalyOneReportDTO();
            ZhongAnBusAnalyOneReportDTO  marketingIfNoLogin = new ZhongAnBusAnalyOneReportDTO();
            ZhongAnBusAnalyOneReportDTO  zhongAnIfLogin = new ZhongAnBusAnalyOneReportDTO();
            ZhongAnBusAnalyOneReportDTO  zhongAnIfNoLogin = new ZhongAnBusAnalyOneReportDTO();
            marketingIfLogin.setReportDate(reportDate);
            marketingIfLogin.setConstituencies(MARKETING_IFLOGIN);
            marketingIfLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            marketingIfLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            marketingIfLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
            marketingIfLogin.setCompositeIncrNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getCompositeIncrNum).sum());
            marketingIfLogin.setCost(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getCost).reduce(BigDecimal.ZERO,BigDecimal::add));
            marketingIfLogin.setIncome(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getIncome).reduce(BigDecimal.ZERO,BigDecimal::add));
            marketingIfLogin.setRoi(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getRoi).reduce(BigDecimal.ZERO,BigDecimal::add));
            marketingIfLogin.setIncomingTotalRate(marketingIfLogin.getIncome().divide(new BigDecimal(marketingIfLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP));
            marketingIfLogin.setApproversRate(new BigDecimal(marketingIfLogin.getApproversNum()).divide(marketingIfLogin.getIncome(),6,BigDecimal.ROUND_HALF_UP));
            marketingIfLogin.setApproversTotalRate((new BigDecimal(marketingIfLogin.getApproversNum()).divide(new BigDecimal(marketingIfLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP)));
            //众安对照首登组
            zhongAnIfLogin.setReportDate(reportDate);
            zhongAnIfLogin.setConstituencies(ZHONGAN_IFLOGIN);
            zhongAnIfLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(ZHONGAN_IFLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            zhongAnIfLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(ZHONGAN_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            zhongAnIfLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(ZHONGAN_IFLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
            zhongAnIfLogin.setIncomingTotalRate(new BigDecimal(zhongAnIfLogin.getIncomingNum()).divide(new BigDecimal(zhongAnIfLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP));
            zhongAnIfLogin.setApproversRate(new BigDecimal(zhongAnIfLogin.getApproversNum()).divide(zhongAnIfLogin.getIncome(),6,BigDecimal.ROUND_HALF_UP));
            zhongAnIfLogin.setApproversTotalRate((new BigDecimal(zhongAnIfLogin.getApproversNum()).divide(new BigDecimal(zhongAnIfLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP)));
            marketingIfLogin.setIncomingIncreaseRate(new BigDecimal(marketingIfLogin.getIncomingNum()-(marketingIfLogin.getTotalNum()/zhongAnIfLogin.getTotalNum()*zhongAnIfLogin.getIncomingNum()))
                    .divide(new BigDecimal((marketingIfLogin.getTotalNum()/zhongAnIfLogin.getTotalNum())*zhongAnIfLogin.getIncomingNum()),6,BigDecimal.ROUND_HALF_UP));
            marketingIfLogin.setApproversIncreaseRate(new BigDecimal(marketingIfLogin.getApproversNum()-((marketingIfLogin.getTotalNum()/zhongAnIfLogin.getTotalNum())*zhongAnIfLogin.getApproversNum()))
                    .divide(new BigDecimal((marketingIfLogin.getTotalNum()/zhongAnIfLogin.getTotalNum())*zhongAnIfLogin.getApproversNum()),6,BigDecimal.ROUND_HALF_UP));
            zhongAnIfLogin.setIncomingIncreaseRate(marketingIfLogin.getIncomingIncreaseRate());
            zhongAnIfLogin.setApproversIncreaseRate(marketingIfLogin.getApproversIncreaseRate());
            zhongAnIfLogin.setCompositeIncrNum(marketingIfLogin.getCompositeIncrNum());
            zhongAnIfLogin.setIncome(marketingIfLogin.getIncome());
            zhongAnIfLogin.setCost(marketingIfLogin.getCost());
            zhongAnIfLogin.setRoi(marketingIfLogin.getRoi());
            //营销非首登组
            marketingIfNoLogin.setReportDate(reportDate);
            marketingIfNoLogin.setConstituencies(MARKETING_IFNOTLOGIN);
            marketingIfNoLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            marketingIfNoLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            marketingIfNoLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
            marketingIfNoLogin.setCompositeIncrNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getCompositeIncrNum).sum());
            marketingIfNoLogin.setCost(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getCost).reduce(BigDecimal.ZERO,BigDecimal::add));
            marketingIfNoLogin.setIncome(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getIncome).reduce(BigDecimal.ZERO,BigDecimal::add));
            marketingIfNoLogin.setRoi(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(MARKETING_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getRoi).reduce(BigDecimal.ZERO,BigDecimal::add));
            marketingIfNoLogin.setIncomingTotalRate(marketingIfNoLogin.getIncome().divide(new BigDecimal(marketingIfNoLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP));
            marketingIfNoLogin.setApproversRate(new BigDecimal(marketingIfNoLogin.getApproversNum()).divide(marketingIfNoLogin.getIncome(),6,BigDecimal.ROUND_HALF_UP));
            marketingIfNoLogin.setApproversTotalRate((new BigDecimal(marketingIfNoLogin.getApproversNum()).divide(new BigDecimal(marketingIfNoLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP)));
            //众安对照非首登组
            zhongAnIfNoLogin.setReportDate(reportDate);
            zhongAnIfNoLogin.setConstituencies(ZHONGAN_IFNOTLOGIN);
            zhongAnIfNoLogin.setTotalNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(ZHONGAN_IFNOTLOGIN))
                    .map(ZhongAnBusAnalyOneReportDTO::getTotalNum).findFirst().orElse(null));
            zhongAnIfNoLogin.setIncomingNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(ZHONGAN_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getIncomingNum).sum());
            zhongAnIfNoLogin.setApproversNum(zhongAnBusAnalyOneReportList.stream().filter(t->t.getConstituencies().equals(ZHONGAN_IFNOTLOGIN)).
                    mapToLong(ZhongAnBusAnalyOneReportDTO::getApproversNum).sum());
            zhongAnIfNoLogin.setIncomingTotalRate(new BigDecimal(zhongAnIfNoLogin.getIncomingNum()).divide(new BigDecimal(zhongAnIfNoLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP));
            zhongAnIfNoLogin.setApproversRate(new BigDecimal(zhongAnIfNoLogin.getApproversNum()).divide(zhongAnIfNoLogin.getIncome(),6,BigDecimal.ROUND_HALF_UP));
            zhongAnIfNoLogin.setApproversTotalRate((new BigDecimal(zhongAnIfNoLogin.getApproversNum()).divide(new BigDecimal(zhongAnIfNoLogin.getTotalNum()),6,BigDecimal.ROUND_HALF_UP)));
            marketingIfNoLogin.setIncomingIncreaseRate(new BigDecimal(marketingIfNoLogin.getIncomingNum()-(marketingIfNoLogin.getTotalNum()/zhongAnIfNoLogin.getTotalNum()*zhongAnIfNoLogin.getIncomingNum()))
                    .divide(new BigDecimal((marketingIfNoLogin.getTotalNum()/zhongAnIfNoLogin.getTotalNum())*zhongAnIfNoLogin.getIncomingNum()),6,BigDecimal.ROUND_HALF_UP));
            marketingIfNoLogin.setApproversIncreaseRate(new BigDecimal(marketingIfNoLogin.getApproversNum()-((marketingIfNoLogin.getTotalNum()/zhongAnIfNoLogin.getTotalNum())*zhongAnIfNoLogin.getApproversNum()))
                    .divide(new BigDecimal((marketingIfNoLogin.getTotalNum()/zhongAnIfNoLogin.getTotalNum())*zhongAnIfNoLogin.getApproversNum()),6,BigDecimal.ROUND_HALF_UP));
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
        return zhongAnBiReportMapper.selectZaBusAnalySevenListbI_(reportId);
    }
}
