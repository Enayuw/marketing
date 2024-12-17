package com.br.marketing.client.robotaiapi.output;

import lombok.Data;

@Data
public class RobotOutboundVo {

    /**
     * 返回标识：00-成功；900001-内部错误；900006-参数异常
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应流水号
     */
    private String swiftNumber;

    /**
     * 响应描述
     */
    private String description;
}
