package com.br.marketing.client.guomei.base;


import com.alibaba.fastjson.JSONObject;


/**
 * 列表详情数据
 *
 * @author Hua Qiang
 * @date 2024-08-20 15:59
 */
public abstract class AbstractUserListBase {

    /**
     * 2024-08-20 16:19
     * 用户 id
     * <p>
     * 必填
     */
    private String userId;

    /**
     * 2024-08-20 16:18
     * 对应推送接口中的参数
     * <p>
     * 必填
     */
    private JSONObject properties;

    public AbstractUserListBase() {
    }

    public AbstractUserListBase(String userId, JSONObject properties) {
        this.userId = userId;
        this.properties = properties;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public JSONObject getProperties() {
        return properties;
    }

    public void setProperties(JSONObject properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "AbstractUserList{" +
                "userId='" + userId + '\'' +
                ", properties=" + properties +
                '}';
    }
}
