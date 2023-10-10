package com.br.marketing.service.Impl.validityperiod;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.ValidityPeriodResendType;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTransfer;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferInfoExample;
import com.br.marketing.enums.ValidityPeriodResendEnum;
import com.br.marketing.mapper.MarketingDataValidConfigMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.strategy.InterfaceHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 360有效期变更重推实现
 *
 * @author senyang.zheng
 * @date 2023/10/08
 */
@Slf4j
@Service
@ValidityPeriodResendType(resendType = ValidityPeriodResendEnum.QI_FU)
public class QiFuResend implements ValidityPeriodResendStrategy<MarketingTransferInfo> {

    @Resource
    private MarketingDataValidConfigMapper marketingDataValidConfigMapper;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    @Resource
    private InterfaceHandlerService interfaceHandlerService;

    /**
     * 获取重推数据
     *
     * @param id 变更有效期id
     * @return {@link List }<{@link MarketingTransfer }>
     * @author senyang.zheng
     * @date 2023/10/08
     */
    @Override
    public List<MarketingTransferInfo> fetchData(Long id) {
        //获取有效期范围
        Map<String, String> validPeriodRange = marketingDataValidConfigMapper.getValidPeriodRangeByApiCodeAndUserType(id);
        Date validStartDate = DateUtil.beginOfDay(DateUtil.parseDate(validPeriodRange.get("validStartDate")));
        Date validEndDate = DateUtil.endOfDay(DateUtil.parseDate(validPeriodRange.get("validEndDate")));
        String apiCode = validPeriodRange.get("apiCode");
        //根据时间范围获取全部转化基础数据
        MarketingTransferInfoExample transferInfoExample = new MarketingTransferInfoExample();
        transferInfoExample.createCriteria().andApiCodeEqualTo(apiCode).andCreateTimeGreaterThanOrEqualTo(validStartDate).andCreateTimeLessThanOrEqualTo(validEndDate);
        return marketingTransferInfoMapper.selectByExample(transferInfoExample);
    }


    /**
     * 处理重推逻辑
     *
     * @param data 重推数据
     * @author senyang.zheng
     * @date 2023/10/08
     */
    @Override
    public void resend(List<MarketingTransferInfo> data) {
        List<String> mqFacts = data.stream()
            .map(QiFuResend::buildMqFact)
            .map(JSONObject::toJSONString)
            .collect(Collectors.toList());
        Map<Boolean, List<String>> partitionedMqFacts = mqFacts.stream()
            .collect(Collectors.partitioningBy(mqFact -> {
                Result<Boolean> result = interfaceHandlerService.handleDataDirection(mqFact);
                return result.getData();
            }));
        List<String> errorList = partitionedMqFacts.get(true);
        if (CollectionUtil.isNotEmpty(errorList)) {
            log.error("360有效期变更重推作业异常数据：{}", JSONObject.toJSONString(errorList));
        }
    }

    /**
     * 构建消息体
     *
     * @param info 信息
     * @return {@link MqFact }
     * @author senyang.zheng
     * @date 2023/10/09
     */
    private static MqFact buildMqFact(MarketingTransferInfo info) {
        MqFact mqFact = new MqFact();
        mqFact.setSourceId(info.getId());
        mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
        return mqFact;
    }
}
