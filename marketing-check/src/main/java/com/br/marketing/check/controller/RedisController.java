package com.br.marketing.check.controller;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.haier.HaierServiceClient;
import com.br.marketing.client.haier.output.PushDTO;
import com.br.marketing.client.haier.output.Response2Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private HaierServiceClient haierServiceClient;

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

    private volatile int count = 0;

    @GetMapping("test")
    public Response2Entity testClient() {
        List<Map<String, String>> list = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        try {
            final Response2Entity response2Entity = haierServiceClient.pushToTeleSales(list, maps -> {
                        Set<PushDTO.DataItems> dataItemsSet = new HashSet<>();
                        final String format = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
                        dataItemsSet.add(new PushDTO.DataItems("别害怕", format.concat("-我是测试-" + count)));
                        dataItemsSet.add(new PushDTO.DataItems("don'tBeAfraid", format.concat("-I'mTesting-" + count)));
                        return dataItemsSet;
                    }, UUID.randomUUID().toString().concat("-").concat(String.valueOf(count))
                    , String.valueOf(random.nextInt(3) + 1));
            return response2Entity;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
