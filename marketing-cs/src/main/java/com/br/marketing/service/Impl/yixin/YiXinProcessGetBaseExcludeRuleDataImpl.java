package com.br.marketing.service.Impl.yixin;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExample;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.PhoneSaleMapper;
import com.br.marketing.service.IDxService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 宜信基础剔除规则实现类
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:33
 */
@Service
@Slf4j
public class YiXinProcessGetBaseExcludeRuleDataImpl implements YiXinProcessGetBaseExcludeRuleDataService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    @Resource
    private PhoneSaleMapper phoneSaleMapper;
    @Resource
    private IDxService iDxService;

    @Override
    public void excludeRuleFirst(List<MarketingTransferSyncUser> marketingTransferSyncUser) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        // 获取当天的日期yyyy-MM-dd
        String today = LocalDate.now().toString();
        List<String> custNums = marketingTransferSyncUser.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
        List<String> excludeList = marketingTransferSyncUserMapper.getExcludeRuleFirstYxTransferByApiCode(marketingTransferSyncUser.get(0).gettCid(), apiCode,today, custNums);
        if (CollectionUtils.isEmpty(excludeList)) {
            return;
        }
        // custNum去重
        Set<String> excludeSet = excludeList.stream().collect(Collectors.toSet());
        marketingTransferSyncUser.removeIf(t -> excludeSet.contains(t.getCustNum()));
    }

    @Override
    public void excludeRuleSecond(List<MarketingTransferSyncUser> marketingTransferSyncUser) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        String tcId = marketingTransferSyncUser.get(0).gettCid();
        Set<String> set = marketingTransferSyncUser.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        List<String> resultFilter = marketingTransferSyncUserMapper.getExcludeRuleSecondYxTransferByApiCode(tcId, apiCode, set);
        if (CollectionUtils.isEmpty(resultFilter)) {
            return;
        }
        // custNum去重
        Set<String> custNumFilter = resultFilter.stream().collect(Collectors.toSet());
        marketingTransferSyncUser.removeIf(t -> custNumFilter.contains(t.getCustNum()));
    }

    @Override
    public void excludeRuleThird(List<MarketingTransferSyncUser> marketingTransferSyncUser) {
        // 剔除3天内,eg: 当前为01-04，3天内为 01-02至01-04
        Date dateStart = Date.from(LocalDate.now().minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dateEnd = Date.from(LocalDate.now().atTime(23, 59, 59, 999999999)
                .atZone(ZoneId.systemDefault()).toInstant());
        List<String> custNums = marketingTransferSyncUser.parallelStream().map(MarketingTransferSyncUser::getCustNum)
                .collect(Collectors.toList());
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        example.createCriteria().andCustNumIn(custNums)
                .andApiCodeEqualTo(apiCode)
                .andPushDxTimeBetween(dateStart, dateEnd);
        example.setDistinct(true);
        final List<String> custNumSet = phoneSaleExtendInfoMapper.selectCustNumByExampletikv_(example);
        if (custNumSet.size() > 0) {
            marketingTransferSyncUser.removeIf(next -> custNumSet.contains(next.getCustNum()));
        }
        if (marketingTransferSyncUser.size() < 1) {
            return;
        }
        custNums = marketingTransferSyncUser.parallelStream().map(MarketingTransferSyncUser::getCustNum)
                .collect(Collectors.toList());
        PhoneSaleExample example1 = new PhoneSaleExample();
        example1.createCriteria().andApiCodeEqualTo(apiCode)
                .andUidIn(custNums)
                .andCreateTimeBetween(dateStart, dateEnd);
        example1.setDistinct(true);
        final List<String> uidSet = phoneSaleMapper.selectUidByExampletikv_(example1);
        if (uidSet.size() > 0) {
            marketingTransferSyncUser.removeIf(next -> uidSet.contains(next.getCustNum()));
        }
    }


    @Override
    public void excludeRuleFifth(List<MarketingTransferSyncUser> marketingTransferSyncUser) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        // 获取当天的日期yyyy-MM-dd
        String today = LocalDate.now().toString();
        // 获取30天之前的日期yyyy-MM-dd
        String minus30Days = LocalDate.now().minusDays(30).toString();
        List<String> custNums = marketingTransferSyncUser.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
        List<String> excludeList = marketingTransferSyncUserMapper.getExcludeRuleFifthYxTransferByApiCode(marketingTransferSyncUser.get(0).gettCid(), apiCode,
                today, minus30Days, custNums);
        if (CollectionUtils.isEmpty(excludeList)) {
            return;
        }
        // custNum去重
        Set<String> excludeSet = excludeList.stream().collect(Collectors.toSet());
        marketingTransferSyncUser.removeIf(t -> excludeSet.contains(t.getCustNum()));
    }

    @Override
    public void excludeRuleSixth(List<MarketingTransferSyncUser> marketingTransferSyncUser) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        List<List<MarketingTransferSyncUser>> list = Lists.partition(marketingTransferSyncUser, 500);
        final Map<String, String> blackMap = new HashMap<>(marketingTransferSyncUser.size());
        for (List<MarketingTransferSyncUser> transferSyncUsers : list) {
            Result<Map<String, String>> result = iDxService.getBlackByTransfer(transferSyncUsers, apiCode);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                blackMap.putAll(result.getData());
            }
        }
        final String defaultValue = "N";
        marketingTransferSyncUser.removeIf(next -> "Y".equals(
                blackMap.getOrDefault(next.getId().toString(), defaultValue)));
    }
}
