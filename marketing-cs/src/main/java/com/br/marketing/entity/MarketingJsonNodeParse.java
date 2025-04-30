package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_json_node_parse
 * @author 
 */
@Data
public class MarketingJsonNodeParse implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * API编码
     */
    private String apiCode;

    /**
     * 数据类型：0上传，1转化
     */
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    private Integer acceptType;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点值
     */
    private String nodeValue;

    /**
     * 父节点完整路径
     */
    private String parentPath;

    /**
     * 节点类型: object, array, primitive
     */
    private String nodeType;

    /**
     * 是否为数组元素
     */
    private Boolean isArrayItem;

    /**
     * 节点层级
     */
    private Integer level;

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