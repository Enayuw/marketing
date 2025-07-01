package com.br.marketing.client;

import com.br.marketing.aspect.Mockable;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Random;

/**
 * @ClassName MockClient
 * @Author kongbx
 * @Date 2025/7/1 11:19
 */
@Slf4j
@Service
public class MockClient {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Mockable(mockName = "test_random")
    public Result<String> testMock() {
        //获取挡板开关

        Map<String, Object> mock = marketingCommonConfig.getMockBaffle();

        if (mock.get("switch") == Boolean.TRUE) {
            log.warn("mock进入挡板");
            long start = System.currentTimeMillis();
            Result<String> stringResult = test(mock);
            long end = System.currentTimeMillis();
            log.warn("mock结束挡板, result:{}, 耗时:{}", stringResult, end - start);
            return stringResult;
        }
        return new Result<>();
    }

    private Result<String> test(Map<String, Object> mock) {
        Result<String> result = new Result<>();
        Integer code = (Integer) mock.get("code");
        if(ResultCode.SUCCESS.getValue().equals(code)){
            Random random = new Random();
            int number = 10000 + random.nextInt(10000); // 生成一个5位数
            result.setDate(String.valueOf(number));
            result.setCode(ResultCode.SUCCESS.getValue());
            result.setMessage("请求成功");
            return result;
        }
        result.setCode(ResultCode.FAIL.getValue());
        result.setMessage("请求失败");
        return result;
    }

}
