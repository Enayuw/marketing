package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_qifu_batch_query_user_real_message
 * @author 
 */
@Data
public class QueryUserRealMessage implements Serializable {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户编号
     */
    private String apiCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 响应结果
     */
    private String respData;

    /**
     * 唯一号
     */
    private String uniqueReqNo;

    /**
     * log加密手机号
     */
    private String cell;

    /**
     * 手机号
     */
    private String mobileMd5;

    /**
     * 营销信号 Y 停止营销 N 可营销
     */
    private String stopMarketingSign;

    /**
     * 用户完件信息
     */
    private String userMessage;

    /**
     * 授信信息
     */
    private String riskMessage;

    /**
     * 交易信息
     */
    private String tradeMessage;

    /**
     * 推送状态：0- 待推送, 1-推送中，2推送成功，3-推送失败
     */
    private Integer status;

    /**
     * 错误原因
     */
    private String errorMsg;

    /**
     * 创建日期
     */
    private String createDate;

    /**
     * 上传数据时间
     */
    private String appletDate;

    /**
     * 场景
     */
    private String userType;

    /**
     * 上传数据更新状态0- 待推送, 1-推送中，2推送成功，3-推送失败
     */
    private Integer uploadUpdateStatus;

    /**
     * es更新状态0- 待推送, 1-推送中，2推送成功，3-推送失败
     */
    private Integer esUpdateStatus;

    /**
     * 是否删除 0:否;1:是;
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}