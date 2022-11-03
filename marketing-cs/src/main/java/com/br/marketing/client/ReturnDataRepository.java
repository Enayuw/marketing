package com.br.marketing.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.rpcclient.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

/**
 * Created by BR on 2018/9/13.
 */
@Slf4j
public class ReturnDataRepository {

    /**
     * 从用户中心获取当前api_code需要返回的数据产品的set集合
     * 将returnProduct中的值统一处理成小写，方便处理三相之力的返回
     * @param returnProduct
     */
    public static void needReturnProduct(Set<String> returnProduct, String apiCode,List<String> list){
        MerchantParam merchantParam=null;
        try{
            merchantParam = RpcClientProxy.getMerchantParam(apiCode);
        }catch (Exception e){
            log.warn("Exception",e);
            log.error("从用户中心请求用户信息出错--{}--apiCode:{}",e.getMessage(),apiCode);
        }
        if(merchantParam!=null&&merchantParam.getIsOutputDataProduct()!=null&&merchantParam.getIsOutputDataProduct()==1){
            log.info("merchantParam {}",merchantParam);
            String meal = merchantParam.getMealJson();
            if(StringUtils.isNotEmpty(meal)){
                JSONObject mealJson= JSON.parseObject(meal);
                Set<String> keySet = mealJson.keySet();
                for(String key :keySet){
                    if(list.contains(key)){
                        JSONObject jsonObject = mealJson.getJSONObject(key);
                        JSONArray returnArray = jsonObject.getJSONArray("return_data_product");
                        //log.info("{}--子产品需要返回的数据产品：{}",key,returnArray);
                        if(returnArray!=null&&returnArray.size()>0){
                            for(int i=0;i<returnArray.size();i++){
                                JSONObject productJson = returnArray.getJSONObject(i);
                                String code = productJson.getString("code");
                                //转成小写
                                code= code.toLowerCase();
                                returnProduct.add(code);
                            }

                        }
                    }
                }
            }
        }
    }




}
