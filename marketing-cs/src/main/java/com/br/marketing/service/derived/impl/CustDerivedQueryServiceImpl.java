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

    /** VO 固定字段名，与 CustDerivedItemVO 属性一致，避免与 config mappingField 重复 */
    private static final Set<String> FIXED_VO_FIELDS = new HashSet<>(Arrays.asList(
            "custNum", "lowAmount_derived", "changeAmount_derived", "remainDayys_derived", "changeIncrease_derived",
            "pricingValidPeriod", "pricingDiscount", "pricingExpireDays"));

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

            // 2.4 按 custNumList 顺序封装返回（固定字段 + config 的 mappingField 动态字段）
            Map<String, MarketingPreUserDetailDTO> dtoMap = detailList.stream()
                    .collect(Collectors.toMap(MarketingPreUserDetailDTO::getCustNum, d -> d, (a, b) -> a));
            Map<String, MarketingDataCleanGeneralRuleConfig> finalConfigRule = configRule;
            List<CustDerivedItemVO> list = custNumList.stream()
                    .map(custNum -> dtoMap.containsKey(custNum) ? toItemVO(dtoMap.get(custNum), finalConfigRule) : emptyItem(custNum))
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
        vo.setExtraFields(new HashMap<>());
        return vo;
    }

    private CustDerivedItemVO toItemVO(MarketingPreUserDetailDTO dto, Map<String, MarketingDataCleanGeneralRuleConfig> configRule) {
        CustDerivedItemVO vo = new CustDerivedItemVO();
        vo.setCustNum(dto.getCustNum());
        vo.setExtraFields(new HashMap<>());
        JSONObject jo = null;
        String reserveField1 = dto.getReserveField1();
        if (StringUtils.isNotBlank(reserveField1)) {
            try {
                jo = JSON.parseObject(reserveField1);
                String v;
                vo.setLowAmount_derived((v = jo.getString("lowAmount_derived")) != null ? v : "");
                vo.setChangeAmount_derived((v = jo.getString("changeAmount_derived")) != null ? v : "");
                vo.setRemainDayys_derived((v = jo.getString("remainDayys_derived")) != null ? v : "");
                vo.setChangeIncrease_derived((v = jo.getString("changeIncrease_derived")) != null ? v : "");
                vo.setPricingValidPeriod((v = jo.getString("pricingValidPeriod")) != null ? v : "");
                vo.setPricingDiscount((v = jo.getString("pricingDiscount")) != null ? v : "");
                vo.setPricingExpireDays((v = jo.getString("pricingExpireDays")) != null ? v : "");
            } catch (Exception e) {
                log.warn("解析 reserveField1 异常, custNum={}", dto.getCustNum(), e);
            }
        }
        // 填充清洗系统配置的 mappingField 动态字段（与固定字段取并集，不重复）
        if (!CollectionUtils.isEmpty(configRule)) {
            for (String mappingField : configRule.keySet()) {
                if (!FIXED_VO_FIELDS.contains(mappingField)) {
                    String val = (jo != null && jo.containsKey(mappingField)) ? jo.getString(mappingField) : null;
                    vo.getExtraFields().put(mappingField, val != null ? val : "");
                }
            }
        }
        return vo;
    }
}
