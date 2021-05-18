package com.br.marketing.api.entities.client;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by Bairong on 2018/10/12.
 */
@Slf4j
public class SanxiangzhiliResult extends Result {

    /**
     * Gets flag.
     *
     * @return the flag
     */
    public JSONObject getFlag() {
        return getDefaultObject("Flag", new JSONObject());
    }

    /**
     * Remove others.
     */
    public void removeOthers() {
        getData().remove("Flag");
        getData().remove("swift_number");
        getData().remove("code");
    }

}
