package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * b_robotai_black_phone_mark
 * @author  lizhen
 */
@Data
public class RoboAIBlackPhoneMark implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 推送结束时间(yyyy-MM-dd)
     */
    private String pushEndDate;

    /**
     * 客服推送黑名单结束标识时间
     */
    private String pushEndTime;

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