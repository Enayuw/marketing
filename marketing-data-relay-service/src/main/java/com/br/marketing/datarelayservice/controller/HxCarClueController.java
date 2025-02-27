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

    @Resource
    ICarClueService iCarClueService;

    @PostMapping("/callback")
    public CarClueResponse callBack(@RequestBody HxClueCallBackReqDTO reqDTO) {
        Result result = iCarClueService.callBackClue(reqDTO);
        return CarClueResponse.fromResult(result);
    }
}
