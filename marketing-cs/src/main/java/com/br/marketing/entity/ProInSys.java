package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 *
 * @Description : 从配置中心获取到所有的贷前产品信息
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/7/31 15:29
 */

@Data
public class ProInSys {
    /**
     * 产品英文名称
     */
    private String productionName;
    /**
     * 产品中文名称
     */
    private String productionChineseName;
    /**
     * 大版本号
     */
    private String bigVersion;
    /**
     * 版本
     */
    private String version;
    /**
     * 兼容版本
     */
    private String compatibleVersion;
    /**
     * 推广状态
     */
    private String spreadStatus;
    /**
     * 前置条件：0无 1部码 2爬取
     */
    private String prerequisite;
    /**
     * 接口类别
     */
    private String interfaceType;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 定价方式 : 1支持查得和查询 2只支持查询
     */
    private String priceWay;
    /**
     * 对接形式 : 1web 2api 3全部
     */
    private String abutmentWay;
    /**
     * 成本： 0无 1有
     */
    private String cost;
    /**
     * 平台代码
     */
    private String platformCode;
    /**
     * 场景代码
     */
    private String sceneCode;
    /**
     * crm客户类型 1全部 2正式 3测试
     */
    private String crmCustomer;
    /**
     * crm状态  0不支持 1支持
     */
    private String crmStatus;
    /**
     * dts 支持线程数
     */
    private String dtsThread;
    /**
     * dts状态  0不支持 1支持
     */
    private String dtsStatus;
    /**
     * 依赖的数据产品的版本
     */
    private String dependDataVersion;
    /**
     * 业务类型代码
     */
    private String businessTypeCode;
    /**
     * 产品类型代码
     */
    private String productionTypeCode;
    /**
     * 第一业务类型代码
     */
    private String firstBusinessCode;
    /**
     * 第一产品类型代码
     */
    private String firstProductionCode;
    /**
     * 客群代码
     */
    private String customerGroupCode;
    /**
     * 推广日期
     */
    private Date spreadDate;
    /**
     * 下线日期
     */
    private Date offlineDate;
    /**
     * 产品介绍
     */
    private String introduction;
    /**
     * 产品说明
     */
    private String description;
    /**
     * 数据描述
     */
    private String dataDescription;
    /**
     * 补充信息
     */
    private String additionInfo;
    /**
     * 创建人
     */
    private String createUser;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 依赖的数据产品的版本
     */
    private String dependDataProduction;
}
