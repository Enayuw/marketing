package com.br.marketing.service.Impl;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialRealTimeUserDataSoleHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 描述：： 中原接口实现
 * <p>
 * ------------------------------------
 *
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
    private ArtificialRealTimeUserDataSoleHandler artificialRealTimeUserDataSoleHandler;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListWithValidityPeriod(String tcId, String apiCode, Long indexId,
                                                                                              String requestStartDate, String requestEndDate) {

        return marketingTransferSyncUserMapper.getZhongYuanTransferByRequestDate(tcId, apiCode, requestStartDate, requestEndDate, indexId);

    }

    @Override
    public void zhongYuanTransferDataToDaas(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        String apiCode = marketingTransferSyncUserList.get(0).getApiCode();
        // 获取ifLogin 的数据集合
        List<MarketingTransferSyncUser> ifLoginCollect = marketingTransferSyncUserList.stream().
                filter(m -> "1".equals(m.getIfLogin())).collect(Collectors.toList());

        // 剔除并返回有效期内最新一条的转化数据
        Map<String, SyncUserValidityPeriodBO> periodBOMap = eliminateAndValidity(apiCode, ifLoginCollect);
        // 推 Daas
        List<RealTimeUserDataSoleDTO> transferData = new ArrayList<>();
        for (SyncUserValidityPeriodBO bo : periodBOMap.values()) {
            PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
            System.out.println("Value = " + bo);
            RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = new RealTimeUserDataSoleDTO();
            DassSingleImportAdapSoleDTO dassSingleImportAdapSoleDTO = new DassSingleImportAdapSoleDTO();
            PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();

        }



        ProcessHandlerContext context = new ProcessHandlerContext();
        context.setApiCode(apiCode);
        artificialRealTimeUserDataSoleHandler.call(transferData, context);
    }

    private Map<String, SyncUserValidityPeriodBO> eliminateAndValidity(String apiCode,
                                                                       List<MarketingTransferSyncUser> ifLoginCollect) {
        // 获取custNum 集合
        Set<String> custNumCollect = ifLoginCollect.stream().map(m -> m.getCustNum()).collect(Collectors.toSet());

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
                    Boolean ifApplyOrIsBlack = validityPeriodDataService.judgmentMarketingTransferDataInvalidWithValidityPeriod(apiCode,
                            transferSyncUser.getCustNum());
                    if (!ifApplyOrIsBlack) {
                        filterSyncUserValidityPeriodBO.put(transferSyncUser.getCustNum(), bo);
                    } else {
                        log.warn("{}:中原转化数据推Daas满足【isBlack=1 or ifApply=1】条件", transferSyncUser.getCustNum());
                    }
//
                }
            });
        }
        return filterSyncUserValidityPeriodBO;
    }

    @Override
    public void zhongYuanTransferDataToCustomerFilter(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {

    }

}
