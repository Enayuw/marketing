package com.br.marketing.dto.autocheck;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class CheckUploadSyncDataDto {

    /**
     * id
     */
    private Long id;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * 客户批次号
     */
    private String cusBatch;

    /**
     * 请求批次
     */
    private String requestBatch;

    /**
     * 用户唯一编号
     */
    private String custNum;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电话
     */
    private String cell;

    /**
     * 手机号md5
     */
    private String cellMd5;

    /**
     * 手机号sha256
     */
    private String cellSha256;

    /**
     * 手机号原始值
     */
    private String cellOriginal;

    /**
     * 身份证号原始值
     */
    private String idCardOriginal;

    /**
     * 姓名原始值
     */
    private String nameOriginal;

    /**
     * 场景
     */
    private String groupType;

    /**
     * 新场景-替代group_type
     */
    private String userType;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 预留字段1
     */
    private String reserveField1;

    /**
     * 预留字段2
     */
    private String reserveField2;

    /**
     * 预留剔除状态字段 1：正常，2：剔除
     */
    private Integer status;

    /**
     * 类型 MD5、Sha256
     */
    private String failType;

    /**
     * 是否导入任务数据 1-未导入;2-导入
     */
    private Integer isTask;

    /**
     * 是否重复 1-未去重; 2-不重复;3-重复;
     */
    private Integer isRepeat;

    /**
     * 数据指纹，数据唯一标识
     */
    private Long fingerprint;

    /**
     * 快照时间：最新快照的生成时间
     */
    private String snapTime;
}
