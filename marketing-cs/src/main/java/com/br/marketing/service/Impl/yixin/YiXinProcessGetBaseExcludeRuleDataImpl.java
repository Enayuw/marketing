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
    public void excludeRuleFirst(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        // 获取当天的日期yyyy-MM-dd
        String today = LocalDate.now().toString();
        List<String> custNums = marketingTransferSyncUsers.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
        List<String> excludeList =
                marketingTransferSyncUserMapper.getExcludeRuleFirstYxTransferByApiCodetikv_(
                        marketingTransferSyncUsers.get(0).gettCid(),
                        apiCode,
                        today,
                        custNums);
        log.warn("宜信推送决策,符合剔除条件:在T日转化的数据中transformType!=1且type!=13的基础数据,剔除的数据量级:{}", excludeList.size());
        if (CollectionUtils.isEmpty(excludeList)) {
            return;
        }
        marketingTransferSyncUsers.removeIf(t -> excludeList.contains(t.getCustNum()));
    }

    @Override
    public void excludeRuleSecond(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {
        log.warn("宜信推送决策,符合剔除条件:在T日转化的数据中caseEffective=0的基础数据,剔除前数据量级:{}", marketingTransferSyncUsers.size());
        long start = System.currentTimeMillis();
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        String tcId = marketingTransferSyncUsers.get(0).gettCid();
        Set<String> set = marketingTransferSyncUsers.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        List<String> resultFilter = marketingTransferSyncUserMapper.getExcludeRuleSecondYxTransferByApiCodetikv_(tcId, apiCode, set);

        log.warn("宜信推送决策,符合剔除条件:在T日转化的数据中caseEffective=0的基础数据。单次处理耗时：{}ms,剔除的数据量级:{}", System.currentTimeMillis() - start,
                resultFilter.size());
        if (CollectionUtils.isEmpty(resultFilter)) {
            return;
        }
        marketingTransferSyncUsers.removeIf(t -> resultFilter.contains(t.getCustNum()));
    }

    @Override
    public void excludeRuleThird(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {
        // 剔除3天内,eg: 当前为01-04，3天内为 01-01至01-03
        Date dateStart = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date dateEnd = Date.from(LocalDate.now().minusDays(1).atTime(23, 59, 59, 999999999)
                .atZone(ZoneId.systemDefault()).toInstant());
        List<String> custNums = marketingTransferSyncUsers.stream().map(MarketingTransferSyncUser::getCustNum)
                .collect(Collectors.toList());
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        example.createCriteria().andCustNumIn(custNums)
                .andApiCodeEqualTo(apiCode)
                .andPushDxTimeBetween(dateStart, dateEnd);
        example.setDistinct(true);
        final List<String> custNumSet = phoneSaleExtendInfoMapper.selectCustNumByExampletikv_(example);
        if (custNumSet.size() > 0) {
            marketingTransferSyncUsers.removeIf(next -> custNumSet.contains(next.getCustNum()));
        }
        if (marketingTransferSyncUsers.size() < 1) {
            return;
        }
        custNums = marketingTransferSyncUsers.stream().map(MarketingTransferSyncUser::getCustNum)
                .collect(Collectors.toList());
        PhoneSaleExample example1 = new PhoneSaleExample();
        example1.createCriteria().andApiCodeEqualTo(apiCode)
                .andUidIn(custNums)
                .andCreateTimeBetween(dateStart, dateEnd);
        example1.setDistinct(true);
        final List<String> uidSet = phoneSaleMapper.selectUidByExampletikv_(example1);
        if (uidSet.size() > 0) {
            marketingTransferSyncUsers.removeIf(next -> uidSet.contains(next.getCustNum()));
        }
    }

    @Override
    public void excludeRuleFourth(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {

    }

    /**
     * 请求时间为T-30日的转化数据取transformType为非1的type=12根据inserTime取最新的custNum，且该custNum在[T-29,T]该transformType为非1的custNum无其他type-20230619更新
     * @param marketingTransferSyncUsers 转化数据集合
     */
    @Override
    public void excludeRuleFifth(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {
        log.warn("宜信推送决策,符合剔除条件:custNum在30天内有type!=12的基础数据,剔除前数据量级:{}", marketingTransferSyncUsers.size());
        long start = System.currentTimeMillis();
        String cid = marketingTransferSyncUsers.get(0).gettCid();
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        List<String> custNums = marketingTransferSyncUsers.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());

        String endDate = LocalDate.now().toString();
        String startDate = LocalDate.now().minusDays(29).toString();

        List<String> includeList =
                marketingTransferSyncUserMapper.getRuleFifthYxTransferByApiCodetikv_(
                        cid,
                        apiCode,
                        startDate, endDate,
                        custNums);

        long end = System.currentTimeMillis();
        log.warn("宜信推送决策,剔除处理:custNum在30天内有type!=12的基础数据。单次处理耗时：{}ms,剔除的数据量级:{}", end - start, includeList.size());
        if (CollectionUtils.isEmpty(includeList)) {
            return;
        }

        marketingTransferSyncUsers.removeIf(t -> includeList.contains(t.getCustNum()));
    }

    @Override
    public void excludeRuleSixth(List<MarketingTransferSyncUser> marketingTransferSyncUsers) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        List<List<MarketingTransferSyncUser>> list = Lists.partition(marketingTransferSyncUsers, 500);
        final Map<String, String> blackMap = new HashMap<>(marketingTransferSyncUsers.size());
        for (List<MarketingTransferSyncUser> transferSyncUsers : list) {
            Result<Map<String, String>> result = iDxService.getBlackByTransfer(transferSyncUsers, apiCode);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                blackMap.putAll(result.getData());
            }
        }
        final String defaultValue = "N";
        marketingTransferSyncUsers.removeIf(next -> "Y".equals(
                blackMap.getOrDefault(next.getId().toString(), defaultValue)));
    }
}
