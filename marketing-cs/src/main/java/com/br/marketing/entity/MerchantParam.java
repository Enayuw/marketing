package com.br.marketing.entity;

import com.alibaba.fastjson.annotation.JSONField;
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
 * @Description : 商户参数配置
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/5/4 10:31
 */
@Data
public class MerchantParam {

    /**
     * 客户apiCode
     */
    private String apiCode;

    /**
     * 是否需要校验请求key值（0:不需要,1:需要,2:不需要(通用强校验),3:需要(通用强校验),4:不需要(通用弱校验),5:需要(通用弱校验)）
     */
    @JSONField(name = "is_check")
    private Integer isCheck;

    /**
     * 是否计费（不计：0，计费：1）
     */
    @JSONField(name = "is_charging")
    private Integer isCharging;

    /**
     * (0测试账号，1 正式账号，-1 停用)
     */
    @JSONField(name = "account_type")
    private Integer accountType;

    /**
     * 请求编码(00/md5)
     */
    @JSONField(name = "request_code")
    private String requestCode;

    /**
     * 响应编码（1004）
     */
    @JSONField(name = "response_code")
    private String responseCode;

    /**
     * 账号状态：0禁用 1启用
     */
    @JSONField(name = "account_status")
    private String accountStatus;

    /**
     * 测试开始时间
     */
    @JSONField(name = "start_time")
    private Date startTime;

    /**
     * 测试结束时间
     */
    @JSONField(name = "end_time")
    private Date endTime;

    /**
     * 数据交互方式(0:API 1:SFTP)
     */
    @JSONField(name = "transport")
    private String transport;

    /**
     * 是否返回数据详情(1 返回，0 不返回)
     */
    @JSONField(name = "is_output_data_product")
    private Integer isOutputDataProduct;

    /**
     * 产品配置信息
     */
    @JSONField(name = "meal_json")
    private String mealJson;

    /**
     * 备注信息
     */
    @JSONField(name = "remarks")
    private String remarks;

    /**
     * 加密key
     */
    @JSONField(name = "encryption_key")
    private String encryptionKey;

    /**
     * 解密key
     */
    @JSONField(name = "decrypt_key")
    private String decryptKey;

    /**
     * 流水号版本
     */
    @JSONField(name = "sn_ver")
    private String snVer;

    /**
     * 调用方式：1动态监控 2风险扫描与动态监控 3一次性查询 4定期全量监控
     */
    @JSONField(name = "call_method")
    private String callMethod;

    /**
     * 文件加密方式 0 不加密 ，1 流加密 ，2 压缩加密
     */
    @JSONField(name = "file_encryption_methods")
    private String fileEncryptionMethods;

    /**
     * 文件加密算法 0 AES-128-CBC ，1 AES-256-CBC
     */
    @JSONField(name = "file_encryption_algorithm")
    private String fileEncryptionAlgorithm;

    /**
     * 文件加密key
     */
    @JSONField(name = "file_encryption_key")
    private String fileEncryptionKey;


}
