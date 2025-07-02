package com.br.marketing.service.Impl.umeng;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;

/***
 *
 */
public interface IUMengApiService {

    /**
     * uMeng-智能时机注册
     * @param localId
     * @param apiCode
     * @param requestParam
     * @return
     */
    Result createTimingTask(Long localId,String apiCode, String requestParam,Boolean isProxy);

    /**
     *
     * @param localId
     * @param apiCode
     * @param requestParam
     * @return
     */
    Result deviceAdd(Long localId,String apiCode, String requestParam,Boolean isProxy);
}
