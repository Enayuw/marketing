package com.br.marketing.rpcclient.rpcclientImpl;


import com.alibaba.fastjson.JSONObject;
import com.br.common.util.AESAlgorithmUtil;
import com.br.common.util.StringUtils;
import com.br.grpc.encodemapping.EncodeRequest;
import com.br.grpc.encodemapping.ResultBean;
import com.br.marketing.common.utils.ThreeDes;
import com.br.marketing.rpcclient.GrpcClientInitConfig;
import com.br.marketing.common.validators.user.UserValidator;
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
            log.error("query result--{}", (resultBean!=null ? resultBean.toString():"结果为null"));
            log.error("获取解密数据失败----", e);
        }
        return result;
    }

    /**
     * 00 不加密
     * 1001 MD5
     * 1002 SH256
     * 1003 SM3
     * 1004
     * 1005
     *
     * @param type
     * @param param
     * @param requestCode
     * @return
     */
    public static String decode(String type, String param, String requestCode, String decryptKey, int isCheck) {
        UserValidator userValidator = new UserValidator(isCheck);
        String result = "";
        if (isMd5(param)) {
            result = query(param, type, "md5", "");
            return result;
        }
        if ("00".equals(requestCode)) {
            return param;
        }
        if ("id".equals(type) && userValidator.validateId(param)) {
            return param;
        }
        if ("cell".equals(type) && userValidator.validatePhone(param)) {
            return param;
        }
        if ("name".equals(type) && userValidator.validateName(param)) {
            return param;
        }
        if ("1002".equals(requestCode)) {
            result = query(param, type, "sha", "");
        }
        if ("1003".equals(requestCode)) {
            result = query(param, type, "sm3", "");
            log.debug("SM3---{}", result);
        }
        //AES
        if ("1006".equals(requestCode)) {
            result = AESAlgorithmUtil.decrypt(param, decryptKey);
            log.debug("AES解密结果---{}", result);
        }
        //3DES
        if ("1011".equals(requestCode)) {
            try {
                result = ThreeDes.decryptByECB(param, decryptKey);
                log.debug("3DES解密结果---{}", result);
            } catch (Exception e) {
                log.error("param--{} decrypt_key--{} 3DES解密出错---{}", param, decryptKey, e);
                return result;
            }
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
