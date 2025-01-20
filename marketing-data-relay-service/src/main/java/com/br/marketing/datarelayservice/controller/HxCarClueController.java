package com.br.marketing.datarelayservice.controller;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.datarelayservice.vo.CarClueResponse;
import com.br.marketing.dto.HxClueCallBackReqDTO;
import com.br.marketing.service.carclue.ICarClueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/hxcarclue")
@RestController
public class HxCarClueController {

    @Resource
    ICarClueService iCarClueService;

    @PostMapping("/callBack")
    public CarClueResponse callBack(@RequestBody HxClueCallBackReqDTO reqDTO) {
        Result result = iCarClueService.callBackClue(reqDTO);
        return CarClueResponse.fromResult(result);
    }
}
