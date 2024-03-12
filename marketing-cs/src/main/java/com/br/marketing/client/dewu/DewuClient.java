package com.br.marketing.client.dewu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.xiecheng.intput.AdReqDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class DewuClient {


    @Autowired
    HttpProxyClient httpProxyClient;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @RetryMethod(retryNowNum = 3)
    public Result pushCollidingData(List<String> mobileList) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appId",marketingCommonConfig.getDeWuAppId());
        jsonObject.put("mobile",mobileList);

        HashMap<String, String> resMap = new HashMap<>();;
        if("false".equalsIgnoreCase(marketingCommonConfig.getDeWuCollidingMockSwitch().get(0))){
            resMap = httpProxyClient.sendByCodeWithLog(jsonObject,
                    marketingCommonConfig.getDeWuCollidingUrl(),
                    true,
                    MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(jsonObject),
                    true,
                    false);
        }else {
            resMap = buildMockResultMap(mobileList);
        }
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(JSON.toJSONString(resMap));
        }
        String content = resMap.get("content");

        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");
        if (code == 200) {
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(content);
        }
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setDate(JSON.toJSONString(resMap));
    }

    /**
     * 构造挡板测试数据
     * @param mobileList 本地要返回的cell数据
     * @return java.util.HashMap<String,String> 构造好的数据
     */
    private HashMap<String,String> buildMockResultMap(List<String> mobileList){
        JSONObject map = new JSONObject();
        JSONArray cellArray = new JSONArray();
        map.put("domain","risk");
        map.put("code",marketingCommonConfig.getDeWuCollidingMockSwitch().get(2));
        map.put("msg","成功");
        map.put("requestId","a8b4cd35-f29a-4bf6-9087-13e1d56b5e1a");
        map.put("extra",null);
        map.put("data",cellArray);
        for(int i=0; i<mobileList.size(); i++){
            JSONObject cellData = new JSONObject();
            cellData.put("status",marketingCommonConfig.getDeWuCollidingMockSwitch().get(3));
            cellData.put("mobile",mobileList.get(i));
            cellData.put("userId",null);
            cellArray.add(cellData);
        }
        HashMap<String, String> resMap = new HashMap<>();
        resMap.put("httpcode",marketingCommonConfig.getDeWuCollidingMockSwitch().get(1));
        resMap.put("content",JSONObject.toJSONString(map, SerializerFeature.WriteMapNullValue));
        return resMap;
    }

}
