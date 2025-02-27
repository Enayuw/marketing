package com.br.marketing.datarelayservice.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.datarelayservice.enums.carclue.CarClueRepEnum;
import com.br.marketing.datarelayservice.vo.carclue.CarClueResponse;
import com.br.marketing.dto.HxClueCallBackReqDTO;
import com.br.marketing.service.carclue.ICarClueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RequestMapping("/hxcarclue")
@RestController
public class HxCarClueController {

    @ExceptionHandler(value = Exception.class)
    public String defaultErrorHandler(HttpServletRequest req, Exception e) {
        log.error("---BaseException Handler---Host {} invokes url {} ERROR: ", req.getRemoteHost(), req.getRequestURL(), e);
        CarClueResponse res = new CarClueResponse()
                .setCode(CarClueRepEnum.FAIL.getCode())
                .setMessage("内部错误");
        return JSON.toJSONString(res);
    }

    @Resource
    ICarClueService iCarClueService;

    @PostMapping("/callback")
    public CarClueResponse callBack(@RequestBody HxClueCallBackReqDTO reqDTO) {
        try {
            Result result = iCarClueService.callBackClue(reqDTO);
            return CarClueResponse.fromResult(result);
        } catch (Exception ex) {
            log.error(String.format("请求信息：%s;异常信息：%s", JSON.toJSONString(reqDTO), ex.getMessage()), ex);
            return new CarClueResponse().setCode(0).setMessage("内部异常请稍后在试");
        }
    }
}
