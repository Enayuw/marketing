package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_shunfeng_company_data
 * @author zhen.Li1
 */
@Data
public class ShunfengCompanyData implements Serializable {
    private Long id;

    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 拓展字段间隔符
     */
    private String extend;

    /**
     * 状态 1-未推送；2-推送成功；3-推送失败；4-匹配失败
     */
    private Integer pushStatus;

    /**
     * 状态 1-正常2-非正常
     */
    private Integer status;

    /**
     * 描述-推送异常会记录异常信息
     */
    private String dataMessage;

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