package com.br.marketing.common.utils.net;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/** API接口请求日志对象
 * @author Wang Weiwei
 * @since 2018/1/10
 */
@Data
public class InterfaceLog {
    private Long id;
    @JSONField(name = "api_code")
    private String apiCode;
    @JSONField(name = "swift_number")
    private String swiftNumber;
    @JSONField(name = "request_time")
    private Date requestTime = new Date();
    @JSONField(name = "response_time")
    private Date responseTime;
    /**
     * 接口请求参数
     * */
    @JSONField(name = "request_str")
    private String requestStr;
    /**
     * 接口响应参数
     * */
    @JSONField(name = "reponse_str")
    private String responseStr;
    /**
     * 接口响应吗
     * */
    private String code;

    /**
     * 接口地址
     * */
    private String url;

    /**
     * 请求ip
     * */
    private String ip;

    /**
     * 错误栈信息
     * */
    private String error;

    /***
     * 多余列，暂时无用
     */
    private String info;

    @JSONField(name = "cost_time")
    private long costTime;

    @JSONField(name="param1")
    private String costTimeParam;
}
