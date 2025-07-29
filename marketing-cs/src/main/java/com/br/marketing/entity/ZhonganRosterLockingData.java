package com.br.marketing.entity;

import java.util.Date;
import lombok.Data;

@Data
public class ZhonganRosterLockingData {
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
    private Long localId;

    /**
     * 类型
     */
    private String type;

    /**
     * 案件编号
     */
    private String caseNum;

    /**
     * MD5手机号
     */
    private String mobileMd5;

    /**
     * 营销日期,yyyy-MM-dd
     */
    private String bizDate;

    /**
     * 枚举,CG/MG
     */
    private String tag;

    /**
     * 状态 1-未推送；2-推送成功；3-已推送,未成功,需要重试；4-已推送,未成功,无需重试
     */
    private Integer pushStatus;

    /**
     * 状态 1-正常；2-非正常；3-未获取到上传数据；4-不在有效期内；5-命中黑名单；6-重复数据；7-不营销
     */
    private Integer status;

    /**
     * 运营场景
     */
    private String userType;

    /**
     * 是否接通(0-否;1-是)
     */
    private Integer isConnect;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 日期,yyyyMMdd
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

    /**
     * 状态 1-sftp文件;2-拨打明细
     */
    private Integer dataSource;

}