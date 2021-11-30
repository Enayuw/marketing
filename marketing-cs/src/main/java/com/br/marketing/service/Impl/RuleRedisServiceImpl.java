package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.vo.CustomerSoleRuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleRedisServiceImpl {

    final static String soleConfigByApiCodeKey = "sole:config:";

    final static String transferContextIdKey = "transferfile:contextid";

    @Autowired
    RedisChgService redisChgService;

    public void setSoleConfigRedis(String apiCode,List<CustomerSoleRuleVO> ruleVOS){
        if(StringUtils.isNotBlank(apiCode)
                &&ruleVOS != null && ruleVOS.size()>0){
            String key = soleConfigByApiCodeKey.concat(apiCode);
            redisChgService.set(key,JSON.toJSONString(ruleVOS));
        }
    }


    public Result<List<CustomerSoleRuleVO>> getSoleConfigRedis(String apiCode){
        String key = soleConfigByApiCodeKey.concat(apiCode);
        if(redisChgService.exists(key)){
            String s = redisChgService.get(key);
            if(StringUtils.isNotBlank(s)){
                return new Result<>()
                        .setCode(ResultCode.SUCCESS.getValue())
                        .setDate(JSON.parseObject(s,new TypeReference<List<CustomerSoleRuleVO>>(){}.getType()));
            }
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue());
    }

    public void delSoleConfigRedis(String apiCode){
        String key = soleConfigByApiCodeKey.concat(apiCode);
        if(redisChgService.exists(key)){
            redisChgService.del(key);
        }
    }

    public Long getTransferFileContextId(){
        Long incr = redisChgService.incr(transferContextIdKey);
        return incr;
    }
}
