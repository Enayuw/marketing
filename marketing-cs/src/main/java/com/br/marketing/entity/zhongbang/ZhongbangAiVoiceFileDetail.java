package com.br.marketing.entity.zhongbang;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_zhongbang_ai_voice_file_detail
 * @author 
 */
@Data
public class ZhongbangAiVoiceFileDetail implements Serializable {
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     * 通话日期 yyyy-mm-dd
     */
    private String callStartTime;

    /**
     * 通话类型 0-人工，1-ai
     */
    private String callType;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 通话记录编号
     */
    private String sessionId;

    /**
     * 录音文件id
     */
    private Long fileInfoId;

    /**
     * 客户文件id
     */
    private String customerFileId;

    /**
     * 推送日期
     */
    private String pushDate;

    /**
     * 推送状态 0- 待推送, 1-推送中，2推送成功，3-推送失败
     */
    private Integer pushStatus;

    /**
     * 状态 1-正常数据 2-重复数据
     */
    private Integer status;

    /**
     * 是否删除 0-正常，1-删除
     */
    private Integer isDeleted;

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