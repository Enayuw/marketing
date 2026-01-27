package com.br.marketing.client.didi;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class DidiCallBackDataDTO extends InterfaceParams {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 回调类型，1-通话 2-短信
     */
    private Integer callbackType;

    /**
     * log加密电话
     */
    private String cell;

    /**
     * 上传表中用户为一编号 md5手机号
     */
    private String custNum;

    /**
     * API代码
     */
    private String apiCode;

    /**
     * 推送状态 0-待推送 1-成功 2-异常
     */
    private Integer status;

    /**
     * 推送状态 0-待推送 1-成功 2-异常
     */
    private Integer pushStatus;

    /**
     * 创建日期
     */
    private String createDate;

    /**
     * 推送类型
     */
    private Integer pushType;

    /**
     * 1-代表TRUE，0-代表FALSE
     */
    private Boolean result;

    /**
     * 是否接通 0-失败 1-成功
     */
    private Integer isConnect;

    /**
     * 短信发送状态 0-失败 1-成功
     */
    private Integer smsSendStatus;

    /**
     * 返回错误码
     */
    private String errorCode;

    /**
     * 返回错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 系统逻辑记录，失效或者重复
     */
    private String sysMessage;

    /**
     * 编码
     */
    private String scas;

    /**
     * 媒体名称
     */
    private String mediaName;

    /**
     * 本地文件记录id
     */
    private Long localId;

    /**
     * 数据描述
     */
    private String dataMessage;

    /**
     * 扩展字段
     */
    private String extend;
}