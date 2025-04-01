package com.br.marketing.entity.tag;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * t_tag_data_detail
 * @author 
 */
@Data
public class TagDataDetail implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标签编码
     */
    private String tagCode;

    /**
     * 计算日期
     */
    private String calculateDate;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 1-有效；0-时效
     */
    private Integer status;

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