package com.br.marketing.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class MarketingTransfer {
    /**
     * 自增主键
     */
    private Long id;

    private String apiCode;

    /**
     * 请求id
     */
    @JSONField(name = "request_id")
    private String requestId;

    /**
     * 任务id
     */
    @JSONField(name = "task_id")
    private String taskId;

    /**
     * 客户编号
     */
    @JSONField(name = "cust_num")
    private String custNum;

    /**
     * 转化时间
     */
    @JSONField(name = "transform_time")
    private String transformTime;

    /**
     * 入库时间
     */
    @JSONField(name = "create_time")
    private Date createTime;

    /**
     * 场景
     */
    @JSONField(name = "group_type")
    private String groupType;

    @JSONField(name = "reserve_field1")
    private String reserveField1;

    @JSONField(name = "reserve_field2")
    private String reserveField2;

}