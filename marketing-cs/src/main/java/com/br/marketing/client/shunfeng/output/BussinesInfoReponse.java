package com.br.marketing.client.shunfeng.output;

import lombok.Data;

import java.util.List;

/**
 * @author zhen.li1
 */
@Data
public class BussinesInfoReponse {

    /**
     * 企业名称
     */
    private String company_name;


    /**
     * 企业信用代码
     */
    private String credit_code;

    /**
     * 成立时间，格式
     * yyyy-MM-dd
     */
    private String establish_time;

    /**
     * 经营状态：吊销、营业
     */
    private String business_status;

    /**
     * 注册地址 加密字段
     */
    private String registered_address;

    /**
     * 注册资本(单位元)
     */
    private String registered_capital;

    /**
     * 实缴资本(单位元)
     */
    private String paid_in_capital;

    /**
     * 归属省份
     */
    private String province;

    /**
     * 归属城市
     */
    private String city;

    /**
     * 归属区县
     */
    private String district;

    /**
     * aoi 类型
     */
    private String aoi_type;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 一类行业
     */
    private String industry1;

    /**
     * 二类行业
     */
    private String industry2;

    /**
     * 三类行业
     */
    private String industry3;

    /**
     * 公司类型（股份有限公
     * 司）
     */
    private String company_type;

    /**
     * 公司联系号码 加密字段
     */
    private String contact_info;

    /**
     * 姓名
     */
    private String legal_person_name;

    /**
     * 股东监事信息
     */
    private String shareholders_supervisors_info;


    /**
     * 更多联系电话  加密字段
     */
    private List<String> more_contact;

    /**
     * 地址验证时间，格式
     * yyyy-MM-dd
     */
    private String address_verification_time;

    /**
     * 联系方式验证时间，格式
     * yyyy-MM-dd
     */
    private String contact_verification_time;

    /**
     * 地址是否完成验证 字段
     */
    private String address_verification_flag;

    /**
     * 联系方式是否完成验证 字段
     */
    private String contact_verification_flag;


    /**
     * 经营地址加密 字段
     */
    private String active_addr;


}
