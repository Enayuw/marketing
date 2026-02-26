package com.br.marketing.entity;

import java.util.Date;
import lombok.Data;

@Data
public class TaikangTransferDataLog {
    /**
     * 
     */
    private Long id;

    /**
     * 拨打明细主键id
     */
    private Long callRecordId;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 投保人手机号
     */
    private String cell;

    /**
     * 投保人姓名
     */
    private String name;

    /**
     * 请求码
     */
    private String httpCode;

    /**
     * 业务异常码
     */
    private String businessCode;

    /**
     * 接口返回内容
     */
    private String returnContent;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-正常1 删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 数据类型 1-通话回调  2-钉钉录入
     */
    private Integer dataType;

    /**
     * 钉钉明细主键id
     */
    private Long ddRecordId;
}