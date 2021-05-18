package com.br.marketing.api.entities.api;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.JsonData;
import com.br.marketing.common.utils.transaction.SwiftNumberManager;

/** 信息推送接口的bean参数
 * 在设计上该类采用访问者模式，将数据结构与操作相互分离
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/12/31
 */
public class PutParam extends JsonData {
    /**
     * Instantiates a new Put param.
     *
     * @param strData the str data
     */
    public PutParam(String strData){
        data = JSONObject.parseObject(strData);
    }

    /**
     * Instantiates a new Put param.
     *
     * @param jsonObject the json object
     */
    public PutParam(JSONObject jsonObject) {
        super();
        this.data = jsonObject;
        setSwiftNumber(SwiftNumberManager.generate());
    }
}
