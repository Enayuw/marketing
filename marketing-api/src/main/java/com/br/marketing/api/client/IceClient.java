package com.br.marketing.api.client;

import com.alibaba.fastjson.JSONObject;
import com.br.bsf.ext.app.util.Ice1BSFConsumerBean;
import com.br.marketing.api.entity.MerchantParam;
import com.br.usernew.CompanyServicePrx;
import com.br.usernew.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    @Value("${otherConfig.userCenter.appName:00}")
    private  String appName;
    @Value("${otherConfig.userCenter.appSecretKey:00}")
    private  String appSecretKey;
    //用户中心-商户套餐表详细字段
    private static final String COLUMNS = "id,api_code,day_limit,is_check,request_code," +
            "response_code,account_type,update_time,update_user,remark,meal,return_data," +
            "start_time,end_time,service_mode,recheck,dhcpriority,link_type,encryption_key,decrypt_key,sn_ver";
    //用户中心-商户套餐数据库标识
    private static final String DB = "DZdb";

    /**
     * Get merchant param merchant param.
     *
     * @param apiCode the api code
     * @return the merchant param
     */
    public MerchantParam getMerchantParam(String apiCode){
        CompanyServicePrx service = (CompanyServicePrx) Ice1BSFConsumerBean.getServiceProxy(CompanyServicePrx.class,"V2.0.0");
        service = (CompanyServicePrx) service.ice_connectionCached(false);
        JSONObject baseJson=new JSONObject();
        baseJson.put("appName",appName);
        baseJson.put("appSecretKey",appSecretKey);
        ResponseDto dZdb = service.getInfo(baseJson.toString(),apiCode, DB, COLUMNS, true);
        if (200 != dZdb.getCode()){
            log.warn("从用户中心查询的商户信息---{}----{}",JSONObject.toJSONString(dZdb),apiCode);
        }
        MerchantParam merchantParam = JSONObject.parseObject(dZdb.getResult(), MerchantParam.class);
        return merchantParam;
    }

    /**
     * Get pro info map.
     *
     * @param apiCode the api code
     * @param proType the pro type
     * @return the map
     */
    public  Map<String,String> getProInfo(String apiCode, String proType){
        Map<String,String> map = new HashMap<>();
        MerchantParam merchantParam = getMerchantParam(apiCode);
        if (!StringUtils.isEmpty(merchantParam)&&StringUtils.hasText(merchantParam.getMeal())){
            JSONObject jsonObject = JSONObject.parseObject(merchantParam.getMeal());
            Set<String> set = jsonObject.keySet();
            for (String key:set) {
                JSONObject json = JSONObject.parseObject(jsonObject.getString(key));
                String version = json.getString("version");
                String typeCode = json.getString("productionTypeCode");
                if (StringUtils.hasText(proType)) {
                    switch (proType){
                        case "rulew": if (typeCode.startsWith("B2")){
                            map.put(key.trim(), version.trim());
                        }
                            break;
                        case "baseData": if ("B101".equals(typeCode)){
                            map.put(key.trim(), version.trim());

                        }
                            break;
                        case "scoreml": if ("B303".equals(typeCode)){
                            map.put(key.trim(), version.trim());
                        }
                            break;
                        case "phoneCheck": if ("phone_relation".equals(key) || "phoneStatusCheck".equals(key)){
                            map.put(key.trim(), version.trim());
                        }
                            break;
                        case "reviewStr": if ("reviewStrA".equals(key)){
                            map.put(key.trim(), version.trim());

                        }
                            break;
                        default:
                    }
                }else{
                    map.put(key.trim(), version.trim());
                }
            }
        }
        return map;
    }

}
