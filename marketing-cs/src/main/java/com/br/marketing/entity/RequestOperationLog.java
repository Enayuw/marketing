package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author kongbx
 * @date 2024/4/18
 */
@Data
public class RequestOperationLog {

    /**
     * 自增主键
     */
    private Long id;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 业务id
     */
    private String bizNo;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 返回结果
     */
    private String result;

    /**
     * 方法路径
     */
    private String url;

    /**
     * 操作记录
     */
    private String extendInfo;

    /**
     * 入库时间
     */
    private Date createTime;
}
