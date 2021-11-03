package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.PropertiesUtil;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * Created by Bairong on 2020/5/8.
 */
@Slf4j
public class ProductResultUtil {

    public static void dealProResult(JSONObject hxJson, Set<String> products,StringBuilder sb,String sep){
        if(!hxJson.isEmpty()){
            StringBuilder result=new StringBuilder();
            if(products.contains("scorencashonszyxxy")){
                Integer age =hxJson.getInteger("sd_scorencashonszyxxy_pd_id_apply_age");
                if(age ==null || age<20||age>=50){
                    return;
                }
                String[] deleteArea="新疆,西藏,广西,贵州".split(",");
                String cellProvince=hxJson.getString("sd_scorencashonszyxxy_pd_cell_province");
                String idWhere=hxJson.getString("sd_scorencashonszyxxy_pd_id_where");
                if(StringUtils.isEmpty(cellProvince) ||StringUtils.isEmpty(idWhere)){
                    return;
                }
                for (String area : deleteArea) {
                    if(cellProvince.contains(area)||idWhere.contains(area)){
                        return;
                    }
                }
                if(StringUtils.isEmpty(hxJson.get("scorencashonszyxxy"))){
                    return;
                }
                String fields = PropertiesUtil.getProperty("scorencashonszyxxy");
                String[] split = fields.split(",");
                for (int i=0;i<split.length;i++){
                    result.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    result.append(sep);
                }
            }
            if(products.contains("scoremcashonxhqbdzcd")){
                if(StringUtils.isEmpty(hxJson.get("scoremcashonxhqbdzcd"))){
                    return;
                }
                String fields = PropertiesUtil.getProperty("scoremcashonxhqbdzcd");
                String[] split = fields.split(",");
                for (int i=0;i<split.length;i++){
                    result.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    result.append(sep);
                }
            }
            if(products.contains("scoremcashon360xktwo")){
                String fields = PropertiesUtil.getProperty("scoremcashon360xktwo");
                String[] split = fields.split(",");
                for (int i=0;i<split.length;i++){
                    result.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    result.append(sep);
                }
            }
            if(products.contains("scorebrevoloanmszd3")){
                String fields = PropertiesUtil.getProperty("scorebrevoloanmszd3");
                String[] split = fields.split(",");
                for (int i=0;i<split.length;i++){
                    result.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    result.append(sep);
                }
                result.append("zd3").append(sep);
                result.append(sep).append(sep).append(sep).append(sep).append(sep).append(sep);
            }
            sb.append(result);
        }
    }
}
