package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @Company:百融（北京）金融信息服务股份有限公司
 * @Author: shaobin.fu
 * @Date: 2018/6/22 10:35
 * @Description:画像返回的变量打平工具
 */
public class FieldEvenUtils {

    public  static JSONObject convertJson(Map<String,Object> map, String parent, JSONObject json){
        if(json==null||"{}".equals(json.toString())){
            return null;
        }
        Set<String> strings = json.keySet();
        for (String s:strings ) {
            String child=json.getString(s);
            if(child.startsWith("[\"")){
                JSONArray array=json.getJSONArray(s);
                for(int i = 0; i < array.size(); i++){
                    JSONObject item = array.getJSONObject(i);
                    if(StringUtils.isNotEmpty(parent)){
                        convertJson(map,parent+"_"+s,item);
                    }else {
                        convertJson(map,s, item);
                    }
                }
            }else  if(child.startsWith("{")){
                JSONObject item= JSON.parseObject(json.getString(s));
                if(StringUtils.isNotEmpty(parent)){
                    convertJson(map,parent+"_"+s,item);
                }else {
                    convertJson(map,s, item);
                }
            }else{
                if(StringUtils.isNotEmpty(parent)){
                    map.put(parent+"_"+s,json.getString(s));
                }else{
                    map.put(s,json.getString(s));
                }
            }
        }
        return JSON.parseObject(JSON.toJSONString(map));
    }
}
