package com.br.marketing.rpcclient.rpcclientImpl;

import com.alibaba.fastjson.JSONObject;
import com.br.grpc.service.usercenterrely.UserCenterRequest;
import com.br.grpc.service.usercenterrely.UserCenterResponse;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.rpcclient.GrpcClientInitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserCenterGrpcClient {

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

    /**
     * 从用户中心查取客服信息
     */
    private static final String CUSTOMER_COLUMNS = "account_type,account_status";
    private static final String CUSTOMER = "customer";


    @Value("${otherConfig.userCenter.appName:00}")
    public void setAppName(String appName) {
        UserCenterGrpcClient.appName = appName;
    }

    @Value("${otherConfig.userCenter.appSecretKey:00}")
    public void setAppSecretKey(String appSecretKey) {
        UserCenterGrpcClient.appSecretKey = appSecretKey;
    }

    public static MerchantParam getMerchantParam(String apiCode) {
        JSONObject baseJson = new JSONObject();
        baseJson.put("appName", appName);
        baseJson.put("appSecretKey", appSecretKey);
        UserCenterRequest request = UserCenterRequest.newBuilder().setApiCode(apiCode).setBaseJson(baseJson.toJSONString())
                .setColumns(COLUMNS).setTypeName(DB).setCache(true).build();
        UserCenterResponse info = GrpcClientInitConfig.grpcUserCenter().getInfo(request);
        if (200 != info.getCode()) {
            log.warn("MerchantParam-apiCode:[{}]从用户中心[{}]查询的商户信息失败，返回错误码:[{}]，返回错误信息:[{}]",
                    apiCode, info.getResult(), info.getCode(), info.getMessage());
        }
        MerchantParam merchantParam = JSONObject.parseObject(info.getResult(), MerchantParam.class);
        return merchantParam;
    }

    /**
     * 查询商户名称
     *
     * @param apiCode
     * @return
     */
    public static String getCompanyMsg(String apiCode) {
        JSONObject baseJson = new JSONObject();
        baseJson.put("appName", appName);
        baseJson.put("appSecretKey", appSecretKey);
        UserCenterRequest request = UserCenterRequest.newBuilder().setApiCode(apiCode).setBaseJson(baseJson.toJSONString())
                .setColumns(BASE_COLUMNS).setTypeName(BASE).setCache(true).build();
        UserCenterResponse company = GrpcClientInitConfig.grpcUserCenter().getInfo(request);
        if (200 != company.getCode()) {
            log.warn("apiCode:[{}]从用户中心[{}]查询的商户信息失败，返回错误码:[{}]，返回错误信息:[{}]",
                    apiCode, company.getResult(), company.getCode(), company.getMessage());
        }
        return company.getResult();
    }

    /**
     * 查询客服信息
     *
     * @param apiCode
     * @return
     */
    public static String getCustomerMsg(String apiCode) {
        JSONObject baseJson = new JSONObject();
        baseJson.put("appName", appName);
        baseJson.put("appSecretKey", appSecretKey);
        UserCenterRequest request = UserCenterRequest.newBuilder().setApiCode(apiCode).setBaseJson(baseJson.toJSONString())
                .setColumns(CUSTOMER_COLUMNS).setTypeName(CUSTOMER).setCache(true).build();
        UserCenterResponse company = GrpcClientInitConfig.grpcUserCenter().getInfo(request);
        if (200 != company.getCode()) {
            log.warn("apiCode:[{}]从用户中心[{}]查询的商户信息失败，返回错误码:[{}]，返回错误信息:[{}]",
                    apiCode, company.getResult(), company.getCode(), company.getMessage());
        }
        return company.getResult();
    }

}
