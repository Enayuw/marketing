package com.br.marketing.innerapi.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.yiqianbao.YiQianBaoService;
import com.br.marketing.client.yiqianbao.input.YqbDetailVo;
import com.br.marketing.client.yiqianbao.output.ResponseYqbDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.customer.CallRecordDTO;
import com.br.marketing.service.ZnkfPushService;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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

    @Resource
    YiQianBaoService yiQianBaoService;

    @ApiOperation(value = "客服推送营销数据 回调接口")
    @PostMapping("/znkfPushCallBack")
    public String znkfPushCallBack(@RequestBody CallRecordDTO dto) {
        try {
            return znkfPushService.znkfPushCallBack(dto);
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw ex;
        }
    }

    @ApiOperation(value = "壹钱包营销数据推送接口")
    @PostMapping("/yiqianbaoApiTest")
    public String yiqianbaoApiTest() {
        YqbDetailVo yqbDetailVo = new YqbDetailVo();
        YqbDetailVo.UserInfo userInfo = new YqbDetailVo.UserInfo();
        userInfo.setDataTime("2022-04-13 10:04:13");
        userInfo.setMarketFlag("Y");
        userInfo.setOuterApplyNo("1234");
        userInfo.setPhoneMd5("a3ea925d30a7df1a9d0550e5b7d0284b");
        yqbDetailVo.setUserInfoList(Lists.newArrayList(userInfo));
        Result<ResponseYqbDTO> result = yiQianBaoService.pushMarketingData(yqbDetailVo);
        log.warn("调用壹钱包接口result={}", JSON.toJSONString(result));
        return "success";
    }


}
