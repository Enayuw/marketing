package com.br.marketing.client;

import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.marketing.entity.MerchantParam;
import com.br.usernew.CompanyServicePrx;
import com.br.usernew.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 *
 * @Description : Ice服务远程调用客户端
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/7/26 11:31
 */
@Component
@Slf4j
public class IceClient {
    private static String appName;
    private static String appSecretKey;
    //用户中心-商户套餐表详细字段
    private static final String COLUMNS = "api_code,is_charging,is_check,request_code," +
            "response_code,account_type,account_status,start_time,end_time,transport,meal_json,remarks,encryption_key," +
            "decrypt_key,sn_ver,call_method,file_encryption_methods,file_encryption_algorithm,file_encryption_key,encryption_key,is_output_data_product";
    //用户中心-商户套餐数据库标识
    private static final String DB = "YXdb";


    /**
     * 从用户中心查取商户信息
     */
    private static final String BASE_COLUMNS = "API_CODE,REMARK,COMP_NAME,COMP_SHORT_NAME,COMP_ID,APPLY_LOAN_TYPE";
    private static final String BASE = "base";

    @Value("${otherConfig.userCenter.appName:00}")
    public void setAppName(String appName) {
        IceClient.appName = appName;
    }
    @Value("${otherConfig.userCenter.appSecretKey:00}")
    public void setAppSecretKey(String appSecretKey) {
        IceClient.appSecretKey = appSecretKey;
    }

    /**
     * 查询商户信息
     * @param apiCode
     * @return
     */
    public static MerchantParam getMerchantParam(String apiCode){
        CompanyServicePrx service = (CompanyServicePrx) Ice1BSFConsumerBean.getServiceProxy(CompanyServicePrx.class,"V2.0.0");
        service = (CompanyServicePrx) service.ice_connectionCached(false);
        JSONObject baseJson=new JSONObject();
        baseJson.put("appName",appName);
        baseJson.put("appSecretKey",appSecretKey);
       // log.warn("从用户中心查询的商户信息-:"+baseJson.toString());
        ResponseDto dZdb = service.getInfo(baseJson.toString(),apiCode, DB, COLUMNS, true);
        if (200 != dZdb.getCode()){
            log.warn("从用户中心查询的商户信息-{}--{}----{}",baseJson.toString(),JSONObject.toJSONString(dZdb),apiCode);
        }
        //log.info("从用户中心查询的商户信息---{}----{}",JSONObject.toJSONString(dZdb),apiCode);
        MerchantParam merchantParam = JSONObject.parseObject(dZdb.getResult(), MerchantParam.class);
        return merchantParam;
    }

    /**
     * 查询商户名称
     * @param apiCode
     * @return
     */
    public static String getCompanyMsg(String apiCode) {
        CompanyServicePrx service = (CompanyServicePrx) Ice1BSFConsumerBean.getServiceProxy(CompanyServicePrx.class,"V2.0.0");
        service = (CompanyServicePrx) service.ice_connectionCached(true);
        JSONObject baseJson=new JSONObject();
        baseJson.put("appName",appName);
        baseJson.put("appSecretKey",appSecretKey);
        ResponseDto company = service.getInfo(baseJson.toString(),apiCode, BASE, BASE_COLUMNS, true);
        if (200 != company.getCode()){
            log.warn("从用户中心base查询的商户信息----{}",JSONObject.toJSONString(company));
        }
        return company.getResult();
    }
}
