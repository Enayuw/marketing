package com.br.marketing.api.entities;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/** Json操作父类
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/12/31
 */
@Slf4j
public class JsonData {
    /**
     * The Data.
     */
    protected JSONObject data;

    /**
     * Gets data string.
     *
     * @param key the key
     * @return the data string
     */
    public String getDataString(String key) {
        return getDefaultString(key, "");
    }

    /**
     * Set data string.
     *
     * @param key   the key
     * @param value the value
     */
    public void setDataString(String key, String value){
        data.put(key, value);
    }

    /**
     * Gets default string.
     *
     * @param key      the key
     * @param defaults the defaults
     * @return the default string
     */
    public String getDefaultString(String key, String defaults) {
        if (data.containsKey(key)){
            return data.getString(key);
        }
        data.put(key, defaults);
        return defaults;
    }

    /**
     * Gets default array.
     *
     * @param key     the key
     * @param objects the objects
     * @return the default array
     */
    public JSONArray getDefaultArray(String key, JSONArray objects) {
        JSONArray array;
        try {
            if (data.getJSONArray(key) == null){
                array = objects;
                data.put(key, objects);
            }else {
                array = data.getJSONArray(key);
            }
            return array;
        }catch (Exception e){
            log.warn("Exception",e);
            return objects;
        }
    }

    /**
     * Gets default object.
     *
     * @param key        the key
     * @param jsonObject the json object
     * @return the default object
     */
    public JSONObject getDefaultObject(String key, JSONObject jsonObject) {
            if (data.containsKey(key)){
                return data.getJSONObject(key);
            }else {
                data.put(key, jsonObject);
            }
        return jsonObject;
    }

    /**
     * Gets swift number.
     *
     * @return the swift number
     */
    public String getSwiftNumber() {
        return getDataString("swift_number");
    }


    /**
     * Gets data.
     *
     * @return the data
     */
    public JSONObject getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(JSONObject data) {
        this.data = data;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }

    @Override
    public String toString() {
        return data.toJSONString();
    }

    /**
     * Sets swift number.
     *
     * @param swiftNumber the swift number
     */
    public void setSwiftNumber(String swiftNumber) {
        data.put("swift_number", swiftNumber);
    }
}
