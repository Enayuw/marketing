package com.br.marketing.api.client;

import cn.hutool.core.util.IdcardUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.common.util.AESAlgorithmUtil;
import com.br.common.util.StringUtils;
import com.br.ice.service.encodemapping.EncodeMappingServicePrx;
import com.br.ice.service.encodemapping.ResultBean;
import com.br.marketing.common.utils.ThreeDes;
import com.br.marketing.common.validators.user.UserValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by Bairong on 2019/12/3.
 */
@Service
@Slf4j
public class DecodeClient {
    @Value("${otherConfig.encodeMapping.appName:00}")
    private  String appName;
    @Value("${otherConfig.encodeMapping.appSecretKey:00}")
    private  String appSecretKey;
    private static String reg = "^([a-fA-F0-9]{32})$";
    private  String query(String param,String type,String alogrithm,String swiftNumber){
        if(!"id".equals(type) && !"cell".equals(type) && !"name".equals(type)){
            log.warn("query type is not exist !!!");
            return null;
        }
        if(StringUtils.isEmpty(swiftNumber)){
            swiftNumber= UUID.randomUUID().toString();
        }
        String result = "";
        ResultBean resultBean=null;
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("swift_number",swiftNumber);
            jsonParam.put("appName",appName);
            jsonParam.put("appSecretKey",appSecretKey);
            jsonParam.put("key",param);
            jsonParam.put("alogrithm",alogrithm);
            jsonParam.put("type",type);
            EncodeMappingServicePrx service = (EncodeMappingServicePrx) Ice1BSFConsumerBean.getServiceProxy(EncodeMappingServicePrx.class);
            service= (EncodeMappingServicePrx) service.ice_connectionCached(false);
             resultBean = service.query(jsonParam.toJSONString());
            String str = resultBean.getData();
            if(!str.isEmpty()){
                result = str;
            }
        }catch (Exception e){
            log.warn("type:----{},Value:----{}",type,param);
            log.error("query result--{}",JSONObject.toJSONString(resultBean));
            log.error("获取解密数据失败",e);
        }
        return result;
    }


    /**
     * Decode string.
     *
     * 00 不加密
     * 1001 MD5
     * 1002 SH256
     * 1003 SM3
     * 1004
     * 1005
     *
     *
     * @param type        the type
     * @param param       the param
     * @param requestCode the request code
     * @param decryptKey  the decrypt key
     * @param isCheck     the is check
     * @return the string
     */
    public   String decode(String type, String param, String requestCode, String decryptKey,Integer isCheck ){
        UserValidator userValidator = new UserValidator(isCheck);
        String result="";
        if(isMD5(param)){
            result=query(param,type,"md5","");
            return result;
        }
        if("00".equals(requestCode)){
            return param;
        }
        if("id".equals(type)&& userValidator.validateId(param)){
            return param;
        }
        if("cell".equals(type)&& userValidator.validatePhone(param)){
            return param;
        }
        if("name".equals(type)&& userValidator.validateName(param)){
            return param;
        }
        if("1002".equals(requestCode)){
            result=query(param,type,"sha","");
        }
        if("1003".equals(requestCode)){
            result=query(param,type,"sm3","");
            log.info("SM3---{}",result);
        }
        //AES
        if("1006".equals(requestCode)){
            result= AESAlgorithmUtil.decrypt(param,decryptKey);
            log.info("AES解密结果---{}",result);
        }
        //3DES
        if("1011".equals(requestCode)){
            try {
                result= ThreeDes.decryptByEcb(param,decryptKey);
                log.info("3DES解密结果---{}",result);
            } catch (Exception e) {
                log.error("param--{} decrypt_key--{} 3DES解密出错---{}",param,decryptKey,e);
                return result;
            }
        }
        if(!"name".equals(type)&&StringUtils.isEmpty(result)){
            return param;
        }
        return result;
    }

    /**
     * Is md 5 boolean.
     *
     * @param md5 the md 5
     * @return the boolean
     */
    public static boolean isMD5(String md5){
        if(null == md5 || md5.isEmpty()) {
            return false;
        } else{
            Pattern pattern = Pattern.compile(reg);
            return pattern.matcher(md5).find();
        }
    }
}
