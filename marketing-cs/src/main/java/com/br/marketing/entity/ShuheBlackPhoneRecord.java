package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_shuhe_black_phone_record
 * @author :lizhen
 */
@Data
public class ShuheBlackPhoneRecord implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 场景
     */
    private String userType;

    /**
     * 转化节点
     */
    private String type;

    /**
     * 机构名称
     */
    private String orgname;

    /**
     * 机构名称
     */
    private String source;

    /**
     * 数据推送日期
     */
    private String pushDate;

    /**
     * 1-未推送；2-推送成功；3-推送失败
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