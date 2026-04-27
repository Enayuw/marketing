package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.vo.MarketingSyncUserVO;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.Impl.MarketingCustomertestImpl;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.thirdpartner.ThirdPartnerDataService;
import com.br.marketing.service.thirdpartner.dto.ThirdPartnerDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @Description 后台接口控制器
 * @Author hong.chen
 * @CreateTime 2023/06/28
 */
@Tag(name = "BackendController", description = "BackendController")
@RequestMapping("/backend")
@RestController
public class BackEndController {
    private static final Logger log = LoggerFactory.getLogger(BackEndController.class);
    @Autowired
    PushRuleService pushRuleService;
    @Autowired
    MarketingCustomertestImpl marketingCustomertest;
    @Autowired
    ThirdPartnerDataService thirdPartnerDataService;

    /**
     * 查询客户信息接口（外呼→营销）
     * @param cid
     * @param apiCode
     * @param custNum
     * @return
     */
    @Operation(summary = "查询客户信息接口")
    @PostMapping("/queryCustInfo")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public Result queryCustInfo(@RequestParam(required = false) String cid,
                                @RequestParam(required = false) String apiCode,
                                String custNum, String cell) {
        try {
            return pushRuleService.queryCustInfo(cid, apiCode, custNum, cell);
        } catch (Exception ex) {
            log.error("外呼查询营销客户信息接口异常", ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }

    /**
     * 外呼推送三方上传数据接口（外呼→营销）
     * @param data
     * @return
     */
    @Operation(summary = "外呼推送三方上传数据接口")
    @PostMapping("/thirdPartner/uploadData")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public Result thirdPartnerUploadData(String data, String accessNumber) {
        // 校验请求参数
        if (StringUtils.isEmpty(data)) {
            return new Result().setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("参数异常");
        }

        List<ThirdPartnerDataDTO> dataList;
        try {
            dataList = JSON.parseObject(data,
                    new TypeReference<List<ThirdPartnerDataDTO>>() {
                    });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), data, "外呼推送三方上传数据接口，参数JSON解析异常"));
            return new Result().setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("参数异常");
        }

        if (CollectionUtils.isEmpty(dataList)) {
            return new Result().setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("参数异常");
        }
        if (dataList.size() > 5000) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), data, "外呼推送三方上传数据接口，外呼推送量级超限"));
            return new Result().setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("参数异常");
        }

        return thirdPartnerDataService.saveData(dataList, accessNumber, data);
    }

    /**
     * 查询上传逾期金额接口（外呼→营销）
     * @param custNum
     * @return
     */
    @Operation(summary = "查询上传逾期金额接口")
    @GetMapping("/queryUploadOverAmt")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public Result<String> queryUploadOverAmt(@RequestParam(required = true) String custNum, HttpServletRequest request) {
        try {
            return pushRuleService.queryUploadOverAmt(custNum, request);
        } catch (Exception ex) {
            log.error("外呼查询上传逾期金额接口异常", ex);
            return new Result<String>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }

    @Operation(summary = "查询最新变量接口")
    @GetMapping("/queryLatestSyncUser")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public Result<MarketingSyncUserVO> queryLatestSyncUser(
            @RequestParam String apiCode,
            @RequestParam String custNum,
            @RequestParam(required = false) String userType) {
        try {
            return pushRuleService.queryLatestSyncUser(apiCode, custNum, userType);
        } catch (Exception ex) {
            log.error("查询最新变量接口异常, apiCode={}, custNum={}", apiCode, custNum, ex);
            return new Result<MarketingSyncUserVO>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }

}
