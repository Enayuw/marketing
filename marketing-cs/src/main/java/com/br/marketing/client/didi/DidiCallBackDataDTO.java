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
     * 回调参数类型:1-拨打结果,2-短信发送结果
     */
    private Integer callbackType;

    /**
     * 公司ID
     */
    private String cid;

    /**
     * API代码
     */
    private String apiCode;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编号
     */
    private String taskId;

    /**
     * 案件编号/客户号码
     */
    private String cell;

    /**
     * 案件状态
     */
    private String caseStatus;

    /**
     * 案件拨打次数
     */
    private Integer dialCount;

    /**
     * 拨打明细详情
     */
    private String detail;

    /**
     * 通话记录编号
     */
    private String callRecordId;

    /**
     * 开始外呼时间
     */
    private Date callStartTime;

    /**
     * 外呼接通时间
     */
    private Date callConnectTime;

    /**
     * 外呼结束时间
     */
    private Date callEndTime;

    /**
     * 对话轮次
     */
    private Integer dialogTurn;

    /**
     * 通话状态
     */
    private String callStatus;

    /**
     * 是否接通:0-否,1-是
     */
    private Integer isConnect;

    /**
     * 短信发送状态:0-否,1-是
     */
    private Integer smsSendStatus;

    /**
     * 用户信息
     */
    private String userProperties;

    /**
     * 第n次拨打
     */
    private Integer dialRounds;

    /**
     * 录音地址
     */
    private String recordingPath;

    /**
     * 意向等级:A级-有明确意向,B级-可能有意向,C级-明确拒绝,D级-用户忙,E级-拨打失败,F级-无效客户
     */
    private String intentionGrade;

    /**
     * 标签列表
     */
    private String tagList;

    /**
     * 0-待上报，1已上报
     */
    private Byte pushStatus;

    /**
     * 0-正常，1-数据重复
     */
    private Integer status;

    /**
     * 扩展字段，存储其他额外信息
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 交互文本
     */
    private String callDialog;
}