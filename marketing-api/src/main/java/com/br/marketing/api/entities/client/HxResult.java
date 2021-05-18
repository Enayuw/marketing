package com.br.marketing.api.entities.client;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.Result;
import lombok.extern.slf4j.Slf4j;

/** 画像返回结果操作类
 * @author Wang Weiwei
 * @since 2018/3/16
 */
@Slf4j
public class HxResult extends Result {
    /**
     * Instantiates a new Hx result.
     */
    public HxResult() {
        super();
    }

    /**
     * Instantiates a new Hx result.
     *
     * @param json the json
     */
    public HxResult(JSONObject json){
        super(json);
    }

    /**
     * Instantiates a new Hx result.
     *
     * @param json the json
     */
    public HxResult(String json) {
        super(json);
        if (getData() == null){
            if (json != null){
                log.error("画像原始返回信息  --- {}", json);
            }
            throw new IllegalArgumentException("画像返回结果为空 ");
        }
    }

    /**
     * Gets flag.
     *
     * @return the flag
     */
    public JSONObject getFlag() {
        return getDefaultObject("Flag", new JSONObject());
    }



    /**
     * Set behavior result.
     *
     * @param behaviorResult the behavior result
     */
    public void setBehaviorResult(String behaviorResult){
        data.put("behaviorResult",behaviorResult);
    }

    /**
     * Get behavior result string.
     *
     * @return the string
     */
    public String getBehaviorResult(){
      return  data.getString("behaviorResult");
    }

    /**
     * Remove behavior result.
     */
    public void removeBehaviorResult(){
        data.remove("behaviorResult");
    }


}
