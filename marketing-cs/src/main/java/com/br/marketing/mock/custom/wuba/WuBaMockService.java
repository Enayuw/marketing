package com.br.marketing.mock.custom.wuba;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.WubaSubmitConversionDataLog;
import com.br.marketing.entity.WubaSubmitConversionDataLogExample;
import com.br.marketing.mapper.WubaSubmitConversionDataLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @Description WuBaMockService
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class WuBaMockService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private WubaSubmitConversionDataLogMapper wubaSubmitConversionDataLogMapper;
    private static final Random RANDOM = new Random();

    public HashMap<String, String> getMock03(HashMap<String, String> resMap) {
        HashMap<String, String> resMock = new HashMap<>();
        JSONObject content = JSONObject.parseObject(resMap.get("content"));
        if (Objects.equals(content.get("code"), "66666")) {
            HashMap<String, Object> contentMock = new HashMap<>();
            contentMock.put("code", 0);
            contentMock.put("msg", "成功");
            String batchNoMock = "wuba_yxmd_test" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            contentMock.put("data", batchNoMock);

            resMock.put("httpcode", "200");
            resMock.put("content", JSON.toJSONString(contentMock));
            return resMock;
        } else {
            return resMap;
        }
    }

    public HashMap<String, String> getMock04(String batchNo, HashMap<String, String> resMap) {
        HashMap<String, String> resMock = new HashMap<>();
        JSONObject content = JSONObject.parseObject(resMap.get("content"));
        if (Objects.equals(content.get("code"), "66666")) {
            WubaSubmitConversionDataLogExample example = new WubaSubmitConversionDataLogExample();
            example.createCriteria().andBatchNoEqualTo(batchNo).andIsDeletedEqualTo(0);
            List<WubaSubmitConversionDataLog> logs = wubaSubmitConversionDataLogMapper.selectByExample(example);

            JSONArray array = new JSONArray();
            for (WubaSubmitConversionDataLog log : logs) {
                JSONObject jsonObject = new JSONObject();
                //
                String randomNumber = RANDOM.ints(1, 10)
                        .limit(10).mapToObj(String::valueOf).collect(Collectors.joining()) + "0";
                jsonObject.put("id", randomNumber);
                jsonObject.put("mobileEncrypt", log.getCell());
                jsonObject.put("lastLoginTime", "");
                jsonObject.put("financeApplyTime", "");
                jsonObject.put("financeCreditStatus", "");
                jsonObject.put("financeCreditFinishTime", "");
                jsonObject.put("debtTime", "");
                jsonObject.put("debtPassTime", "");
                jsonObject.put("loanAmt", "");
                jsonObject.put("field1", "field1");
                jsonObject.put("field2", "field2");
                if (log.getId().intValue() % 5 == 1) {
                    jsonObject.put("lastLoginTime", "2024-07-12 09:00:00");
                    jsonObject.put("financeApplyTime", "2024-07-12 09:00:00");
                    jsonObject.put("financeCreditStatus", "1");
                    jsonObject.put("financeCreditFinishTime", "2024-07-12 09:00:00");
                    jsonObject.put("debtTime", "2024-07-12 09:00:00");
                    jsonObject.put("debtPassTime", "2024-07-12 09:00:00");
                    jsonObject.put("loanAmt", "1000.00");
                    jsonObject.put("field1", "field1");
                    jsonObject.put("field2", "field2");
                }
                array.add(jsonObject);
            }

            HashMap<String, Object> contentMock = new HashMap<>();
            contentMock.put("code", 0);
            contentMock.put("msg", "成功");
            contentMock.put("data", array);

            resMock.put("httpcode", "200");
            resMock.put("content", JSON.toJSONString(contentMock));
            return resMock;
        } else if (Objects.equals(content.get("code"), "888") ) {
            WubaSubmitConversionDataLogExample example = new WubaSubmitConversionDataLogExample();
            example.createCriteria().andBatchNoEqualTo(batchNo).andIsDeletedEqualTo(0);
            List<WubaSubmitConversionDataLog> logs = wubaSubmitConversionDataLogMapper.selectByExample(example);

            JSONArray array = new JSONArray();
            for (WubaSubmitConversionDataLog log : logs) {
                JSONObject jsonObject = new JSONObject();
                //
                String randomNumber = RANDOM.ints(1, 10)
                        .limit(10).mapToObj(String::valueOf).collect(Collectors.joining()) + "0";
                jsonObject.put("id", randomNumber);
                jsonObject.put("mobileEncrypt", log.getCell());
                jsonObject.put("lastLoginTime", "");
                jsonObject.put("financeApplyTime", "");
                jsonObject.put("financeCreditStatus", "");
                jsonObject.put("financeCreditFinishTime", "");
                jsonObject.put("debtTime", "");
                jsonObject.put("debtPassTime", "");
                jsonObject.put("loanAmt", "");
                jsonObject.put("field1", "field1");
                jsonObject.put("field2", "field2");
                jsonObject.put("field3", "field3");
                if (log.getId().intValue() % 5 == 1 || log.getId().intValue() % 5 == 2) {
                    jsonObject.put("lastLoginTime", "2024-07-12 11:00:00");
                    jsonObject.put("financeApplyTime", "2024-07-12 11:00:00");
                    jsonObject.put("financeCreditStatus", "1");
                    jsonObject.put("financeCreditFinishTime", "2024-07-12 11:00:00");
                    jsonObject.put("debtTime", "2024-07-12 11:00:00");
                    jsonObject.put("debtPassTime", "2024-07-12 11:00:00");
                    jsonObject.put("loanAmt", "2000");
                    jsonObject.put("field1", "field1");
                    jsonObject.put("field2", "field2");
                    jsonObject.put("field3", "field3");
                }
                array.add(jsonObject);
            }

            HashMap<String, Object> contentMock = new HashMap<>();
            contentMock.put("code", 0);
            contentMock.put("msg", "成功");
            contentMock.put("data", array);

            resMock.put("httpcode", "200");
            resMock.put("content", JSON.toJSONString(contentMock));
            return resMock;
        } else {
            return resMap;
        }
    }
}
