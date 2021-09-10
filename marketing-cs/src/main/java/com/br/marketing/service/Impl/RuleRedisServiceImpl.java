package com.br.marketing.service.Impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.vo.CustomerSoleRuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleRedisServiceImpl {

//    final static String soleConfigByApiCodeKey = "sole:config:";
//
//    @Autowired
//    RedisChgService redisChgService;
//
//    public Result<List<CustomerSoleRuleVO>> getSoleConfigRedis(String apiCode){
//
//    }
//
//    public void delSoleConfigKey(String apiCode){
//        String key = soleConfigByApiCodeKey.concat(apiCode);
//        if(redisChgService.exists(key)){
//            redisChgService.del(key);
//        }
//    }
}
