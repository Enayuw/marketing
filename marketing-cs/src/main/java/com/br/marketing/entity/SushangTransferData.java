package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_sushang_transfer_data
 * @author 
 */
@Data
public class SushangTransferData implements Serializable {
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
     * 案件编号
     */
    private String custNum;

    /**
     * 最近一次登录日期
     */
    private String extend01;

    /**
     * 首开日
     */
    private String extend02;

    /**
     * 购买日
     */
    private String extend03;

    /**
     * 购买金额区间
     */
    private String extend04;

    /**
     * 支取日
     */
    private String extend05;

    /**
     * 支取区间
     */
    private String extend06;

    /**
     * extend07
     */
    private String extend07;

    /**
     * extend08
     */
    private String extend08;

    /**
     * extend09
     */
    private String extend09;

    /**
     * extend10
     */
    private String extend10;

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