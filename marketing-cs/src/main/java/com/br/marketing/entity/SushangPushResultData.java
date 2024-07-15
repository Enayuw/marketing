package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_sushang_push_result_data
 * @author 
 */
@Data
public class SushangPushResultData implements Serializable {
    private Long id;

    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

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
     * 规则标识1,2
     */
    private Integer rule;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 上传日期
     */
    private String uploadDate;

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