package com.br.marketing.tools.rpcclient.rpcclientImpl;


import com.alibaba.fastjson.JSONObject;
import com.br.common.util.StringUtils;
import com.br.grpc.encodemapping.EncodeRequest;
import com.br.grpc.encodemapping.ResultBean;
import com.br.marketing.tools.rpcclient.GrpcClientInitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 解密客户端
 * Created by Bairong on 2019/12/3.
 */
@Service
@Slf4j
public class DecodeGrpcClient {

    private static String appName;

    private static String appSecretKey;

    @Value("${otherConfig.encodeMapping.appName:00}")
    public void setAppName(String appName) {
        DecodeGrpcClient.appName = appName;
    }

    @Value("${otherConfig.encodeMapping.appSecretKey:00}")
    public void setAppSecretKey(String appSecretKey) {
        DecodeGrpcClient.appSecretKey = appSecretKey;
    }

    private static String reg = "^([a-fA-F0-9]{32})$";

    public static String query(String param, String type, String alogrithm, String swiftNumber) {
        if (!"id".equals(type) && !"cell".equals(type) && !"name".equals(type)) {
            log.warn("query type is not exist !!!");
            return null;
        }
        if (StringUtils.isEmpty(swiftNumber)) {
            swiftNumber = UUID.randomUUID().toString();
        }
        String result = "";
        ResultBean resultBean = null;
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("swift_number", swiftNumber);
            jsonParam.put("appName", appName);
            jsonParam.put("appSecretKey", appSecretKey);
            jsonParam.put("key", param);
            jsonParam.put("alogrithm", alogrithm);
            jsonParam.put("type", type);
            EncodeRequest request = EncodeRequest.newBuilder().setParam(jsonParam.toJSONString()).build();
            resultBean = GrpcClientInitConfig.grpcEncode().query(request);
            String str = resultBean.getData();
            if (!StringUtils.isEmpty(str)) {
                result = str;
            }
        } catch (Exception e) {
            log.warn("type:{} Value:{}", type, param);
            log.error("query result--{}", JSONObject.toJSONString(resultBean));
            log.error("获取解密数据失败----", e);
        }
        return result;
    }

    /**
     * 校验是否位md5字符串
     *
     * @param md5
     * @return
     */
    public static boolean isMd5(String md5) {
        if (null == md5 || md5.isEmpty()) {
            return false;
        } else {
            Pattern pattern = Pattern.compile(reg);
            return pattern.matcher(md5).find();
        }
    }

}
