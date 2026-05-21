package com.br.marketing.vo;

import lombok.Data;

import java.util.Date;

@Data
public class CustomerListVo {

    /**
     *
     */
    private Long id;

    /**
     * 合作客户ID
     */
    private String cid;

    /**
     *
     */
    private String apiCode;

    /**
     * 备注
     */
    private String message;

    /**
     * incr 增量、all 全量、once 一次
     */
    private String type;

    /**
     * 并发数
     */
    private Integer threadNum;

    /**
     * 跑数时间 1实时跑，2 T+1
     */
    private Byte taskTime;

    /**
     * finish或success文件生成时间，默认为1实时,2表示定时,
     */
    private Byte finishDate;

    /**
     * 是否推送客服,1推送,0不推送
     */
    private Byte pushCustomer;

    /**
     * 是否校验黑名单,1校验,0不校验
     */
    private Byte checkBlackList;

    /**
     * 是否校验条数,1校验,0不校验
     */
    private Byte checkRedisNumber;

    /**
     * 是否记录日志,1记录,0不记录
     */
    private Byte saveLog;

    /**
     * 跑分顺序根据此字段倒序排序
     */
    private Byte sort;

    /**
     * 状态 1正常，0删除
     */
    private Byte status;

    /**
     * 扩展字段
     */
    private String extendConfigInfo;

    /**
     * 跑分批次进度 Redis 保留天数（varchar 存正整数）
     */
    private String expireDay;

    /**
     * api推送并发数
     */
    private Integer pushThreadNum;

    /**
     * 跑分结果推送类型，0文件，1 api，默认支持文件推送
     */
    private Integer pushType;

    /**
     * 推送地址
     */
    private String pushUrl;

    /**
     * 合作客户名称
     */
    private String name;

    /**
     * 合作客户简称
     */
    private String shortName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否需要校验请求key值（0:不需要,1:需要,2:不需要(通用强校验),3:需要(通用强校验),4:不需要(通用弱校验),5:需要(通用弱校验)）
     */
    private Integer isCheck;

    /**
     * 是否计费（不计：0，计费：1）
     */
    private Integer isCharging;

    /**
     * 请求处理编码
     */
    private String requestCode;

    /**
     * 响应处理编码
     */
    private String responseCode;

    /**
     * 账号类型 0 测试 1 正式
     */
    private Integer accountType;

    /**
     * 账号状态：0禁用 1启用)
     */
    private String accountStatus;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 0:API 1:WEB 2:SFTP
     */
    private String transport;

    /**
     *
     */
    private Date officialTime;

    /**
     *
     */
    private String modifyUser;

    /**
     * 加密key
     */
    private String encryptionKey;

    /**
     * 解密key
     */
    private String decryptKey;

    /**
     * 流水号：全部默认v2
     */
    private String snVer;

    /**
     * 调用方式：1动态监控 2风险扫描与动态监控 3一次性查询 4定期全量监控
     */
    private String callMethod;

    /**
     * 文件加密方式 0 不加密 1 流加密 2 压缩加密
     */
    private String fileEncryptionMethods;

    /**
     * 文件加密算法 0 AES-128-CBC 1 AES-256-CBC
     */
    private String fileEncryptionAlgorithm;

    /**
     * 文件加密key
     */
    private String fileEncryptionKey;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 是否输出数据产品 0 否 1 是
     */
    private Integer isOutputDataProduct;

    /**
     * 无id关联的产品json
     */
    private String mealJson;

    /**
     * 0 内部用户,1 银行,2 非银行,3 催收用户,4 保险用户,5 其他
     */
    private String applyLoanType;

    /**
     * 客户类型
     */
    private String apiType;

    private Integer checkType;
    /**
     * 跑分分隔符,默认,
     */
    private String scoreSeparator;
    /**
     * 客户3k的加密类型 1-MD5;2-sha256
     */
    private Integer threeKEncryptType;

    /**
     * 短信类别
     */
    private String smsCategory;

    /**
     * 一级部门
     */
    private String firstDepartment;

    /**
     * 二级部门
     */
    private String secondDepartment;

    /**
     * AES通用-加密模式
     */
    private String cipherMode;

    /**
     * AES通用-填充模式
     */
    private String paddingScheme;

    /**
     * AES通用-字符编码
     */
    private String charset;

    /**
     * 初始化向量
     */
    private String iv;

    /**
     * AES通用-密钥
     */
    private String dynamicKeys;

    /**
     * 项目对应开发组
     */
    private String assignedGroup;

}
