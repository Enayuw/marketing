package com.br.marketing.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

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
 * @Description : 商户参数配置
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/5/4 10:31
 */
@Data
public class MerchantParam {

    private Integer id;

    //客户apiCode
    private String apiCode;

    //公司名字
    private String companyName;

    //(0测试账号，1 正式账号，-1 停用)
    @JSONField(name = "account_type")
    private Integer accountType;

    //请求编码(00/md5)
    @JSONField(name = "request_code")
    private String requestCode;

    //响应编码（1004）
    @JSONField(name = "response_code")
    private String responseCode;

    //是否返回数据详情(1 返回，0 不返回)
    @JSONField(name = "return_data")
    private Integer returnData;

    //测试开始时间
    @JSONField(name = "start_time")
    private String startTime;

    //测试结束时间
    @JSONField(name = "end_time")
    private String endTime;

     //(1 在线查询，0 定期监控)
     @JSONField(name = "service_mode")
    private String serviceMode;

    //对接方式(1,网页 0 api)
    @JSONField(name = "link_type")
    private String linkType;

    //电话虫优先级
    private Integer dhcpriority;

    //贷前策略重审
    private Integer recheck;

    //转正时间
    @JSONField(name = "formal_time")
    private String formalTime;

    //日访问次数
    @JSONField(name = "day_limit")
    private String dayLimit;

    //是否校验key
    @JSONField(name = "is_check")
    private Integer isCheck;

    //商户的产品套餐
    private String meal;
    @JSONField(name = "encryption_key")
    private String encryptionKey;
    @JSONField(name = "decrypt_key")
    private String decryptKey;
    //流水号版本
    @JSONField(name = "sn_ver")
    private String snVer;

    //是否增量监控 调用方式：1 动态监控  2  风险扫描
    @JSONField(name = "call_method")
    private String callMethod;

    //file_encryption_methods,文件加密方式 0 不加密 ，1 流加密 ，2 压缩加密
    @JSONField(name = "file_encryption_methods")
    private String fileEncryptionMethods;
    //file_encryption_algorithm int(2) DEFAULT NULL COMMENT '文件加密算法 0 AES-128-CBC ，1 AES-256-CBC'
    @JSONField(name = "file_encryption_algorithm")
    private String fileEncryptionAlgorithm;
    //file_encryption_key varchar(256) DEFAULT NULL COMMENT '文件加密key'
    @JSONField(name = "file_encryption_key")
    private String fileEncryptionKey;
}
