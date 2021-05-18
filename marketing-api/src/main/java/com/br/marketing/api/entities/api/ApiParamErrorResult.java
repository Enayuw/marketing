package com.br.marketing.api.entities.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/** api用户参数错误结果
 * @author Wang Weiwei
 * @since 2018/3/15
 */
public class ApiParamErrorResult extends Result{
    /**
     * Instantiates a new Api param error result.
     *
     * @param result the result
     */
    public ApiParamErrorResult(Result result){
        this.data = result.getData();
    }


    /**
     * Add error.
     *
     * @param id    the id
     * @param error the error
     */
    public void addError(String id, List<JSONObject> error) {
        JSONArray errorArray = getDefaultArray("errorArray", new JSONArray());
        JSONObject errorObject = new JSONObject();
        errorObject.put("id", id);
        errorObject.put("error", error);
        errorArray.add(errorObject);
    }
}
