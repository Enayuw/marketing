package com.br.marketing.innerapi.controller;

import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.service.ZnkfPushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 智能客服推送接口
 */
@RestController
@RequestMapping("/znkePush")
@Api(value = "客服推送营销数据")
public class ZnkfPushController {

    private static final Logger log = LoggerFactory.getLogger(ZnkfPushController.class);

    @Autowired
    private ZnkfPushService znkfPushService;

    @ApiOperation(value = "客服推送营销数据 回调接口")
    @PostMapping("/znkfPushCallBack")
    public String znkfPushCallBack(@RequestBody CallRecordDTO dto) {
        try {
            Boolean flag = znkfPushService.znkfPushCallBack(dto);
            if (!flag){
                return "fail";
            }
            return "success";
        }catch (Exception ex){
            log.error(ex.getMessage());
            return "fail";
        }
    }


}
