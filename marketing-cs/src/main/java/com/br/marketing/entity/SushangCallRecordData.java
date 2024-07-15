package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_sushang_call_record_data
 * @author 
 */
@Data
public class SushangCallRecordData implements Serializable {
    private Long id;

    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 类型
     */
    private String type;

    /**
     * 批次号
     */
    private String taskId;

    /**
     * 客户号
     */
    private String custNum;

    /**
     * 外呼渠道
     */
    private String touchType;

    /**
     * 外呼日期
     */
    private String callTime;

    /**
     * 数据日期	
     */
    private String pushTime;

    /**
     * 状态 1-未推送；2-推送
     */
    private Integer pushStatus;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 日期
     */
    private Integer createDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}