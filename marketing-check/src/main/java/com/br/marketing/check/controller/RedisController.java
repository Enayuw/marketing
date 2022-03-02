package com.br.marketing.check.controller;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.PushBlackListResponse;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bairong on 2020/6/16.
 */
@RestController
@RequestMapping("/redis/")
@Slf4j
public class RedisController {

    @Resource
    RedisChgService redisChgService;

    @Resource
    private DassServiceClient dassServiceClient;

    @GetMapping("get")
    public String get(String key) {
        log.info("key ----{}----", key);
        return redisChgService.get(key);
    }

    @GetMapping("set")
    public String set(String key, String value) {
        log.info("key ----{}----{}", key, value);
        redisChgService.set(key, value);
        return "success";
    }

    @GetMapping("del")
    public String del(String key, String value) {
        log.info("key ----{}----{}", key, value);
        redisChgService.del(key);
        return "success";
    }

    @GetMapping("blackTest")
    public String postBlackList(BlackListDTO blackListDTO) {
        List<BlackListDTO> list = new ArrayList<>();
        if (blackListDTO == null) {
            blackListDTO = new BlackListDTO();
            blackListDTO.setUid("AreYouOk-" + System.currentTimeMillis());
            blackListDTO.setApiCode("YouOk");
            blackListDTO.setUserType("Ok");
            blackListDTO.setOrgName("Are");
            blackListDTO.setType("You");
            blackListDTO.setSource("AreYou");
            blackListDTO.setPhone("98765432111");
        }
        blackListDTO.setPhone(AESUtil.encrypt(blackListDTO.getPhone(), "ovksl39fcl13m9dF"));
        for (int i = 0; i < 10; i++) {
            BlackListDTO blackList = new BlackListDTO();
            BeanUtils.copyProperties(blackListDTO, blackList);
            blackList.setUid(blackList.getUid() + "-" + i);
            blackList.setApiCode(blackList.getUid() + "-" + i);
            list.add(blackList);
        }
        final Result<PushBlackListResponse> result = dassServiceClient.postBlackList(list);
        return result.getData().toString();
    }
}
