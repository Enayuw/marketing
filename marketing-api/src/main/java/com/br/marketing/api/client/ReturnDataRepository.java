package com.br.marketing.api.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entity.MerchantParam;
import com.br.marketing.api.entity.Strategy;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by BR on 2018/9/13.
 */
@Service
@Slf4j
public class ReturnDataRepository {

    @Resource
    private IceClient iceClient;
    /**
     *需要返回的画像产品详情
     * @param hxResult 画像
     * @param apiCode 客户编号
     * @param strategyResult 结果
     * @param strategy 策略
     */
    public void hxReturnDataDetail(JSONObject hxResult, String apiCode, StrategyResult strategyResult, Strategy strategy){
        Set<String> returnProduct=new HashSet<>();
        Set<String> strategyProduct=new HashSet<>();
        Set<String> removeKey=new HashSet<>();
        needReturnProduct(returnProduct,apiCode);
        String ruleType = strategy.getRuleType();
        String behaviorScore = strategy.getBehaviorScore();
        if(StringUtils.isNotEmpty(ruleType)){
            JSONArray arrays = JSONArray.parseArray(ruleType);
            for(int i=0;i<arrays.size();i++){
                JSONObject jsonObject = arrays.getJSONObject(i);
                if(jsonObject!=null&&!jsonObject.isEmpty()){
                    JSONObject productor = jsonObject.getJSONObject("productor");
                    if(productor!=null&&!productor.isEmpty()){
                        Set<String> strings = productor.keySet();
                        for(String key:strings){
                            key=key.toLowerCase();
                            strategyProduct.add(key);
                        }
                    }
                }
            }
        }

        if(StringUtils.isNotEmpty(behaviorScore)){
            JSONObject jsonObject = JSONObject.parseObject(behaviorScore);
            if(jsonObject!=null&&!jsonObject.isEmpty()){
                JSONObject productor = jsonObject.getJSONObject("productor");
                if(productor!=null&&!productor.isEmpty()){
                    Set<String> strings = productor.keySet();
                    for(String key:strings){
                        key=key.toLowerCase();
                        strategyProduct.add(key);
                    }
                }
            }
        }

         log.info("需要返回的数据产品：{},strategyProduct:{}"
                 ,returnProduct,strategyProduct);
        Set<String> keySet = hxResult.keySet();
         log.info("画像返回的数据产品：{}",keySet);
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            if(!"swift_number".equals(key)&&!"Flag".equals(key)&&!"code".equals(key)){
                //比较前转换成小写
               String loweraKey=key.toLowerCase();
                if(!returnProduct.contains(loweraKey)||!strategyProduct.contains(loweraKey)){
                    removeKey.add(key);
                }
            }
        }
        if(removeKey!=null&&removeKey.size()>0){
            log.info("移除掉这些数据产品：{}",removeKey);
            for(String key:removeKey){
                if("speciallist_c".equals(key)){
                    hxResult.getJSONObject("Flag").remove("specialList_c");
                    strategyResult.getFlag().remove("specialList_c");
                }
                hxResult.remove(key);
                String loweraKey=key.toLowerCase();
                hxResult.getJSONObject("Flag").remove(loweraKey);
                strategyResult.getFlag().remove(loweraKey);
            }
        }
         log.info("最终返回给客户的画像结果：{}",hxResult);
    }

    /**
     * Need return product.
     *
     * @param returnProduct the return product
     * @param apiCode       the api code
     */
    public  void needReturnProduct(Set<String> returnProduct, String apiCode){
        MerchantParam merchantParam=null;
        try{
            merchantParam = iceClient.getMerchantParam(apiCode);
        }catch (Exception e){
            log.warn("Exception",e);
            log.error("从用户中心请求用户信息出错--{}--apiCode:{}",e.getMessage(),apiCode);
        }
        if(merchantParam!=null){
            String meal = merchantParam.getMeal();
            if(StringUtils.isNotEmpty(meal)){
                JSONObject mealJson= JSON.parseObject(meal);
                Set<String> keySet = mealJson.keySet();
                for(String key :keySet){
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
