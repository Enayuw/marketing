package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_rongshu_cycle_data
 * @author :lizhen
 */
@Data
public class RongshuCycleData implements Serializable {
    private Long id;

    /**
     * 电销扩展表id
     */
    private Long phoneExtendId;

    /**
     * apicode
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 电话（加密数据）
     */
    private String cell;

    /**
     * 推送电销日期
     */
    private String pushDaasDate;

    /**
     * 推送电销时间
     */
    private String pushDaasTime;

    /**
     * 0-不推送；1-正常推送
     */
    private Integer pStatus;

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