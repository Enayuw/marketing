package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CustomizeUploadData implements Serializable {

    private static final long serialVersionUID = 5689959414776817749L;

    /**
     * 主键id
     */
    private Long id;

    /**
     * 用户编号
     */
    private String apiCode;

    /**
     * 分表cid
     */
    private String tCid;

    /**
     * 请求流水号
     */
    private String requestId;

    /**
     * 请求数据
     */
    private String requestJsonData;

    /**
     * 数据量
     */
    private Integer bizDataNumber;

    /**
     * 响应码
     */
    private String responseCode;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-无效、1-有效
     */
    private Integer status;

    /**
     * 同步状态 0-未同步、1-已同步
     */
    private Integer syncStatus;

    /**
     * 接入日期yyyy-MM-dd
     */
    private String receiveDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
