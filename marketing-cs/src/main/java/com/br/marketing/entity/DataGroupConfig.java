package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_data_group_config
 * @author 
 */
@Data
public class DataGroupConfig implements Serializable {
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 分组规则，json类型
     */
    private String groupRules;

    /**
     * 上传记录统计Id集合
     */
    private String uploadReportId;

    /**
     * 1-有效；9-无效
     */
    private Integer isDel;

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