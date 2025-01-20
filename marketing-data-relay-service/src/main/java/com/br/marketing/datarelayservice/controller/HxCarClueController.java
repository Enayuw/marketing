package com.br.marketing.datarelayservice.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.datarelayservice.vo.CarClueResponse;
import com.br.marketing.dto.HxClueCallBackReqDTO;
import com.br.marketing.service.carclue.ICarClueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/hxcarclue")
@RestController
public class HxCarClueController {

    @Resource
    ICarClueService iCarClueService;

    @PostMapping("/callBack")
    public CarClueResponse callBack(@RequestBody HxClueCallBackReqDTO reqDTO) {
        try {
            Result result = iCarClueService.callBackClue(reqDTO);
            return CarClueResponse.fromResult(result);
        } catch (Exception ex) {
            log.error(String.format("请求信息：%s;异常信息：%s", JSON.toJSONString(reqDTO), ex.getMessage()), ex);
            return new CarClueResponse().setResultCode(0).setMessage("内部异常请稍后在试");
        }
    }
}
