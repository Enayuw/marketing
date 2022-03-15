package com.br.marketing.client.halo.send;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.HttpProxyClient;

/**
 * -----------------------------
 *
 * @author guangchao.zhang
 * @Date 2022/3/7 11:46 AM
 * -----------------------------
 * @Description 哈啰接口api数据组装
 */
public class HaloApiParam extends JSONObject {
    public void openUrl(String openUrl){
        put("openUrl",openUrl);
    }
    public void appKey(String appKey){
        put("appKey",appKey);
    }
    public void secret(String  secret){
        put("secret",secret);
    }
    public void method(String  method){
        put("method",method);
    }
    public void param(JSONObject param){
        put("param",param);
    }
    public void httpProxyClient(HttpProxyClient httpProxyClient){
        put("httpProxyClient",httpProxyClient);
    }

    public void isProxy(boolean isProxy){
        put("isProxy",isProxy);
    }
    public HttpProxyClient getHttpProxyClient(){
        return (HttpProxyClient)get("httpProxyClient");
    }

    public String getAppKey(){
        return getString("appKey");
    }
    public String getSecret(){
        return getString("secret");
    }
    public String getMethod(){
        return getString("method");
    }

    public JSONObject getParam(){
        return getJSONObject("param");
    }
    public String gerOpenUrl(){
        return getString("openUrl");
    }
    public boolean getIsProxy(){
        return getBoolean("isProxy");
    }
}
