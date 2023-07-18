package com.br.marketing.check.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.halo.EncryptUtil;
import com.br.marketing.client.halo.send.HaloApiParam;
import com.br.marketing.client.halo.send.PublicParamsConstants;
import com.br.marketing.common.utils.StringUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author guangchao.zhang
 * @Classname CallingUtil
 * @Description 拨打记录工具
 * @Date 2022/2/21 11:24 AM
 */
public class CallingUtil {
    public static JSONObject getJsonObject(String extendConfigInfo) {
        JSONObject extendConfigInfoJson = new JSONObject();
        if (StringUtils.isNotBlank(extendConfigInfo)) {
            extendConfigInfoJson = JSONObject.parseObject(extendConfigInfo);
        }
        return extendConfigInfoJson;
    }


    public static void main(String[] args) {
        /**
        // * 入参
        // */
        //JSONObject param = new JSONObject();
        //param.put("openSerialNo", "123123");
        //param.put("batchNo", "123");
        //
        ///**
        // * 秘钥sign
        // */
        //Map paramsMap = new HashMap();
        //paramsMap.put("appKey", "brgroup-pnPnzgxf");
        //paramsMap.put("method", "hello.finance.loan.marketing.callback.end");
        //paramsMap.put("timestamp", System.currentTimeMillis());
        //paramsMap.put("token", null);
        //Map<String, String> itemMap = JSONObject.toJavaObject(param, Map.class);
        //paramsMap.put("data",itemMap);
        //paramsMap.put("channelNo","BR");
        //String sign = EncryptUtil.signTopRequest(paramsMap, "b3392af427b6449e94269433bdf5fb58");
        //paramsMap.put(PublicParamsConstants.SIGN, sign);
        //


        //===================//===================//===================//===================//===================//===================

        //
        //String openUrl = "https://fat-hello-openapi.hellobike.com/openapi";
        //String appKey = "hellobikefinance-bwYuIVvO";
        //String secret = "b3392af427b6449e94269433bdf5fb58";
        //String method = "hello.finance.loan.marketing.callback.end";

        String openUrl = "https://open.hellobike.com/openapi";
        String appKey = "brgroup-pnPnzgxf";
        String secret = "9c07d3b9923d4269bb8da6a6963fcd27";
        String method = "hello.finance.loan.marketing.callback.end";
        String openSerialNo = "12312333221333";
        String batchNo = "3002734748280815629";

        JSONObject paramTest = new JSONObject();
        paramTest.put("openSerialNo",openSerialNo);
        paramTest.put("batchNo", batchNo);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("appKey", appKey);
        jsonObject.put("method", method);
        Timestamp t = new Timestamp(System.currentTimeMillis());
        jsonObject.put("timestamp", t.toString());
        jsonObject.put("token", null);
        jsonObject.put("data",paramTest);
        jsonObject.put("channelNo","BR");
        String signTest = EncryptUtil.signTopRequest(jsonObject, secret);
        jsonObject.put(PublicParamsConstants.SIGN, signTest);
        System.out.println("curl -x http_proxy://bairong:Proxy_br@squid-proxy2.brapp.com:3128 -H 'Content-Type: application/json' -d '"+jsonObject+"'   "+   openUrl+"  -X POST");
        /**
         * 测试
         */
        //$ curl -x http_proxy://bairong:Proxy_br@squid-proxy2.brapp.com:3128 -H 'Content-Type: application/json' -d '{"method":"hello.finance.loan.marketing.callback.end","data":{"batchNo":"123","openSerialNo":"123123"},"channelNo":"BR","sign":"e2287b4f776a5593bd2aec5a19769cda","appKey":"brgroup-pnPnzgxf","timestamp":1651895334842}' https://fat-hello-openapi.hellobike.com/openapi -X POST
    }

}
