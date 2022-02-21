package com.br.marketing.check.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;

/**
 * @author guangchao.zhang
 * @Classname CallingUtil
 * @Description 拨打记录工具
 * @Date 2022/2/21 11:24 AM
 */
public class CallingUtil {
    public static JSONObject getJsonObject(String extendConfigInfo) {
        JSONObject extendConfigInfoJson = new JSONObject();
        if (StringUtils.isNotBlank(extendConfigInfo)) {
            extendConfigInfoJson = JSONObject.parseObject(extendConfigInfo);
        }
        return extendConfigInfoJson;
    }

}
