package com.br.marketing.bo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 调度任务推送决策参数
 *
 * @author Guo Zeqiang
 * @dateTime 2023-04-11 16:40
 */
public class JobPushDecisionParameterBO implements Serializable {
    private static final long serialVersionUID = -2287448289554015234L;
    /**
     * 2023-04-12 11:22
     * 客户编号
     */
    private String apiCode;
    /**
     * 2023-04-12 11:22
     * 调度时间字符串
     */
    private String timeStr;
    /**
     * 2023-04-12 11:28
     * 自定义参数
     */
    private Map<String, Object> paramMap;
    /**
     * 2023-04-12 11:28
     * 自定义参数
     */
    private List<Object> paramList;
    /**
     * 2023-04-12 11:28
     * 自定义参数
     */
    private Object param;

    public JobPushDecisionParameterBO() {
    }

    public JobPushDecisionParameterBO(String apiCode, String timeStr, Map<String, Object> paramMap, List<Object> paramList, Object param) {
        this.apiCode = apiCode;
        this.timeStr = timeStr;
        this.paramMap = paramMap;
        this.paramList = paramList;
        this.param = param;
    }


    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public List<Object> getParamList() {
        return paramList;
    }

    public void setParamList(List<Object> paramList) {
        this.paramList = paramList;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "JobPushDecisionParameterBO{" +
                "apiCode='" + apiCode + '\'' +
                ", timeStr='" + timeStr + '\'' +
                ", paramMap=" + paramMap +
                ", paramList=" + paramList +
                ", param=" + param +
                '}';
    }
}
