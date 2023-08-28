package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述：： 中原接口实现
 * <p>
 * ------------------------------------
 * @program: marketing
 * @ClassName ZhongYuanServiceImpl
 * @author: it-yml
 * @create: 2023-08-25 19:41
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ZhongYuanServiceImpl implements ZhongYuanService {


    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;


    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList(String tcId, String apiCode, Long indexId,
                                                                            String requestStartDate, String requestEndDate) {

        return marketingTransferSyncUserMapper.getZhongYuanTransferByRequestDate(tcId, apiCode, requestStartDate, requestEndDate, indexId);

    }

    @Override
    public void zhongYuanTransferDataToDaas(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        // 获取ifLogin 的数据集合
        List<MarketingTransferSyncUser> ifLoginCollect = marketingTransferSyncUserList.stream().
                filter(m -> "1".equals(m.getIfLogin())).collect(Collectors.toList());
        // 获取custNum 集合
        Set<String> custNumCollect = ifLoginCollect.stream().map(m -> m.getCustNum()).collect(Collectors.toSet());

        // 剔除并返回有效期内最新一条
        Map<String, SyncUserValidityPeriodBO> periodBOMap = eliminateAndValidity(custNumCollect, apiCode, ifLoginCollect);

        // 推 Daas

    }

    private Map<String, SyncUserValidityPeriodBO> eliminateAndValidity(Set<String> custNumCollect, String apiCode,
                                                                       List<MarketingTransferSyncUser> ifLoginCollect) {
        Map<String, SyncUserValidityPeriodBO> filterSyncUserValidityPeriodBO = new HashMap<>();
        // 判断有效期
        Map<String, SyncUserValidityPeriodBO> periodBOMap =
                transferDataValidityPeriodService.getValidityPeriodCustNumBatchFirstVersion(custNumCollect, apiCode, new Date());
        if (periodBOMap != null) {
            ifLoginCollect.forEach(transferSyncUser -> {
                SyncUserValidityPeriodBO bo = periodBOMap.get(transferSyncUser.getCustNum());
                if (bo == null) {
                    log.warn("{}:中原转化数据推Daas不满足案件编号“有效期内”条件", transferSyncUser.getCustNum());
                } else {
                    // ifApply =1 and isBlack =1 剔除
                    Boolean ifApplyOrIsBlack = validityPeriodDataService.getMarketingTransferDataWithValidityPeriod(apiCode,
                            transferSyncUser.getCustNum());
                    if (!ifApplyOrIsBlack) {
                        filterSyncUserValidityPeriodBO.put(transferSyncUser.getCustNum(), bo);
                    } else {
                        log.warn("{}:中原转化数据推Daas满足【isBlack=1 or ifApply=1】条件", transferSyncUser.getCustNum());
                    }
                    PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
//                    contextRuleNecessaryData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
                }
            });
        }
        return filterSyncUserValidityPeriodBO;
    }

    @Override
    public void zhongYuanTransferDataToCustomerFilter(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {

    }

}
