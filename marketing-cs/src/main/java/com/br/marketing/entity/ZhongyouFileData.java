package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
@Data
public class ZhongyouFileData implements Serializable {

//    private static final long serialVersionUID = -2287448289554015234L;

    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long fileId;

    /**
     * 类型 1：总行数  2 :数据内容
     */
    private String type;

    /**
     * 状态 1-正常2-非正常3-删除
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 
     */
    private Integer createDate;

    /**
     * 内容
     */
    private String fileData;

}