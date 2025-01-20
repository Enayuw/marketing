package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;
@Data
public class CarClueInfo {
    /**
     * 
     */
    private Long id;

    /**
     * 
     */
    private String cid;

    /**
     * 
     */
    private String apiCode;

    /**
     * 案件
     */
    private String custNum;

    /**
     * 手机号
     */
    private String cell;

    /**
     * 意向
     */
    private String intention;

    /**
     * 原品牌信息
     */
    private String brand;

    /**
     * 姓氏
     */
    private String member;

    /**
     * 原车系信息
     */
    private String series;

    /**
     * 线索匹配品牌id
     */
    private String clueMatchBrandId;

    /**
     * 线索匹配品牌
     */
    private String clueMatchBrand;

    /**
     * 线索匹配车系id
     */
    private String clueMatchSeriesId;

    /**
     * 线索匹配车系
     */
    private String clueMatchSeries;

    /**
     * 匹配品牌车系类型:1:精确匹配,2:模糊匹配
     */
    private Integer matchBrandSeriesType;

    /**
     * 省份
     */
    private String province;

    /**
     * 线索匹配省份id
     */
    private String clueMatchProvinceId;

    /**
     * 线索匹配省份
     */
    private String clueMatchProvince;

    /**
     * 城市
     */
    private String city;

    /**
     * 线索匹配城市id
     */
    private String clueMatchCityId;

    /**
     * 线索匹配城市城市
     */
    private String clueMatchCity;

    /**
     * 录音地址
     */
    private String recordingPath;

    /**
     * 线索id
     */
    private String clueId;

    /**
     * 线索推送渠道
     */
    private String cluePushChannel;

    /**
     * 线索状态：0-待清洗；1-有效线索；2-异常线索；3-缺失线索；4-无效线索；
     */
    private Integer clueDataStatus;

    /**
     * 线索补全状态：0-无需补全；1-系统补全；2-缺失线索手动补全；3-异常线索手动补全
     */
    private Integer clueCompleteStatus;

    /**
     * 线索推送状态：0-待推送；1-推送成功；2-推送失败
     */
    private Integer cluePushStatus;

    /**
     * 回调状态：0-待回调；1-回调成功；2-回调失败
     */
    private Integer clueCallbackStatus;

    /**
     * 线索异常原因
     */
    private String clueErrorReason;

    /**
     * 回调结果
     */
    private String clueCallbackResult;

    /**
     * 扩展信息字段
     */
    private String extendInfo;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 清洗时间
     */
    private Date cleanTime;

    /**
     * 推送时间
     */
    private Date pushTime;

    /**
     * 回调时间
     */
    private Date callBackTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}