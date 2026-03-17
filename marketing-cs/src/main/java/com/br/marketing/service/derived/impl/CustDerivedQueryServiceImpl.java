package com.br.marketing.service.derived.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.qifu.ResponseData;
import com.br.marketing.client.qifu.callrealtime.CallRealTimeDTO;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeReq;
import com.br.marketing.client.qifu.callrealtime.QryCallRealTimeResp;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.derived.CustDerivedItemVO;
import com.br.marketing.dto.derived.CustDerivedQueryRequest;
import com.br.marketing.entity.MarketingDataCleanGeneralRuleConfig;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.service.clean.common.DataCleanService;
import com.br.marketing.service.derived.CustDerivedQueryService;
import com.br.marketing.service.qifu.QiFuAiCleanService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户衍生信息查询服务实现
 */
@Slf4j
@Service
public class CustDerivedQueryServiceImpl implements CustDerivedQueryService {

    private static final List<String> GENERAL_FIELDS = Arrays.asList("dataItems", "item", "reserveField1", "reserveField2");

    private static final String TITLE = "[360查询券等衍生信息接口]";

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private QiFuAiCleanService qiFuAiCleanService;

    @Resource
    private DataCleanService dataCleanService;

    @Override
    public ApiResult<List<CustDerivedItemVO>> queryByCustNumList(CustDerivedQueryRequest request) {
        try {
            String apiCode = request.getApiCode();
            List<String> custNumList = request.getCustNumList();
            if (CollectionUtils.isEmpty(custNumList)) {
                return new ApiResult<List<CustDerivedItemVO>>().fail("custNumList不能为空");
            }

            // 2.1 调用卷信息查询
            QryCallRealTimeReq qryReq = new QryCallRealTimeReq();
            qryReq.setCallType("AI");
            qryReq.setRequestNo(UUID.randomUUID().toString());
            qryReq.setSerialNoList(custNumList);

            Result<ResponseData<QryCallRealTimeResp>> result = methodRetryHandlerService.qryCallRealTime(qryReq, 0);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                String errMsg = StringUtils.isNotBlank(result.getMessage()) ? result.getMessage() : "360查询卷信息接口查询失败";
                return new ApiResult<List<CustDerivedItemVO>>().fail(errMsg);
            }

            List<MarketingPreUserDetailDTO> detailList = new ArrayList<>();
            if (result.getData() != null && result.getData().getData() != null && result.getData().getData().getT() != null) {
                List<CallRealTimeDTO> dataDetails = result.getData().getData().getT().getDataDetails();
                if (!CollectionUtils.isEmpty(dataDetails)) {
                    // 2.2 卷信息清洗，组装 MarketingPreUserDetailDTO
                    detailList = qiFuAiCleanService.buildListFromCallRealTimeDetails(dataDetails);
                }
            }

            if (detailList.isEmpty()) {
                // 查不到卷：按 custNumList 顺序返回占位行
                List<CustDerivedItemVO> emptyList = custNumList.stream().map(this::emptyItem).collect(Collectors.toList());
                return new ApiResult<List<CustDerivedItemVO>>().success(emptyList);
            }

            // 2.3 获取清洗规则并执行
            Map<String, MarketingDataCleanGeneralRuleConfig> configRule = dataCleanService.getConfigRule(
                    apiCode,
                    DataProcessEnum.SystemTypeEnum.MARKETING.getCode(),
                    DataProcessEnum.DataTypeEnum.UPLOAD.getCode(),
                    DataProcessEnum.AcceptTypeEnum.GENERAL.getCode(),
                    DataProcessEnum.RuleStatusEnum.PRE_SUCCESS.getCode());
            if (!CollectionUtils.isEmpty(configRule)) {
                configRule = new HashMap<>(configRule);
                configRule.keySet().removeIf(GENERAL_FIELDS::contains);
            }
            if (!CollectionUtils.isEmpty(configRule)) {
                for (MarketingPreUserDetailDTO dto : detailList) {
                    try {
                        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(dto);
                        dataCleanService.dataCleanHandler(jsonObject, configRule.values(), dto);
                    } catch (Exception e) {
                        log.warn(TITLE + "清洗异常, custNum={}", dto.getCustNum(), e);
                    }
                }
            }

            // 2.4 按 custNumList 顺序封装返回
            Map<String, MarketingPreUserDetailDTO> dtoMap = detailList.stream()
                    .collect(Collectors.toMap(MarketingPreUserDetailDTO::getCustNum, d -> d, (a, b) -> a));
            List<CustDerivedItemVO> list = custNumList.stream()
                    .map(custNum -> dtoMap.containsKey(custNum) ? toItemVO(dtoMap.get(custNum)) : emptyItem(custNum))
                    .collect(Collectors.toList());
            return new ApiResult<List<CustDerivedItemVO>>().success(list);
        } catch (Exception ex) {
            log.error(TITLE + "异常", ex);
            return new ApiResult<List<CustDerivedItemVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    private CustDerivedItemVO emptyItem(String custNum) {
        CustDerivedItemVO vo = new CustDerivedItemVO();
        vo.setCustNum(custNum);
        vo.setLowAmount_derived("");
        vo.setChangeAmount_derived("");
        vo.setRemainDayys_derived("");
        vo.setChangeIncrease_derived("");
        vo.setPricingValidPeriod("");
        vo.setPricingDiscount("");
        vo.setPricingExpireDays("");
        vo.setCoupon_derived1("");
        vo.setCoupon_derived2("");
        vo.setCoupon_derived3("");
        return vo;
    }

    private CustDerivedItemVO toItemVO(MarketingPreUserDetailDTO dto) {
        CustDerivedItemVO vo = new CustDerivedItemVO();
        vo.setCustNum(dto.getCustNum());
        String reserveField1 = dto.getReserveField1();
        if (StringUtils.isNotBlank(reserveField1)) {
            try {
                JSONObject jo = JSON.parseObject(reserveField1);
                vo.setLowAmount_derived(jo.getString("lowAmount_derived"));
                vo.setChangeAmount_derived(jo.getString("changeAmount_derived"));
                vo.setRemainDayys_derived(jo.getString("remainDayys_derived"));
                vo.setChangeIncrease_derived(jo.getString("changeIncrease_derived"));
                vo.setPricingValidPeriod(jo.getString("pricingValidPeriod"));
                vo.setPricingDiscount(jo.getString("pricingDiscount"));
                vo.setPricingExpireDays(jo.getString("pricingExpireDays"));
                vo.setCoupon_derived1(jo.getString("coupon_derived1"));
                vo.setCoupon_derived2(jo.getString("coupon_derived2"));
                vo.setCoupon_derived3(jo.getString("coupon_derived3"));
            } catch (Exception e) {
                log.warn("解析 reserveField1 异常, custNum={}", dto.getCustNum(), e);
            }
        }
        if (vo.getLowAmount_derived() == null) vo.setLowAmount_derived("");
        if (vo.getChangeAmount_derived() == null) vo.setChangeAmount_derived("");
        if (vo.getRemainDayys_derived() == null) vo.setRemainDayys_derived("");
        if (vo.getChangeIncrease_derived() == null) vo.setChangeIncrease_derived("");
        if (vo.getPricingValidPeriod() == null) vo.setPricingValidPeriod("");
        if (vo.getPricingDiscount() == null) vo.setPricingDiscount("");
        if (vo.getPricingExpireDays() == null) vo.setPricingExpireDays("");
        if (vo.getCoupon_derived1() == null) vo.setCoupon_derived1("");
        if (vo.getCoupon_derived2() == null) vo.setCoupon_derived2("");
        if (vo.getCoupon_derived3() == null) vo.setCoupon_derived3("");
        return vo;
    }
}
