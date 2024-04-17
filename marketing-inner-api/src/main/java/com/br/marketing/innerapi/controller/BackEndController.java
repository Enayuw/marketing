package com.br.marketing.innerapi.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.PushRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 后台接口控制器
 * @Author hong.chen
 * @CreateTime 2023/06/28
 */
@Api(value = "BackendController")
@RequestMapping("/backend")
@RestController
public class BackEndController {
    private static final Logger log = LoggerFactory.getLogger(BackEndController.class);
    @Autowired
    PushRuleService pushRuleService;

    /**
     * 查询客户信息接口（外呼→营销）
     *
     * @param cid
     * @param apiCode
     * @param custNum
     * @return
     */
    @ApiOperation(value = "查询客户信息接口")
    @PostMapping("/queryCustInfo")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public Result queryCustInfo(@RequestParam(required = false) String cid,
                                @RequestParam(required = false) String apiCode,
                                String custNum, String cell) {
        try {
            return pushRuleService.queryCustInfo(cid, apiCode, custNum, cell);
        } catch (Exception ex) {
            log.error("外呼查询营销客户信息接口异常",ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }
    }
}
