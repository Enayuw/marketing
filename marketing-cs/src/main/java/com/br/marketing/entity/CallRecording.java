package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_call_recording
 * @author 
 */
@Data
public class CallRecording implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 合作客户ID
     */
    private String cid;

    private String apiCode;

    /**
     * 回调参数类型(1:拨打结果 2:短信发送结果)
     */
    private Integer callBackType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务编号
     */
    private Integer taskId;

    /**
     * 案件编号
     */
    private String custNum;

    /**
     * 拨打明细详情
     */
    private Object detail;

    /**
     * 通话记录编号
     */
    private String sessionId;

    /**
     * 开始外呼时间(时间戳)
     */
    private Long callStartTime;

    /**
     * 外呼接通时间(时间戳)
     */
    private Long callConnectTime;

    /**
     * 外呼结束时间(时间戳)
     */
    private Long callEndTime;

    /**
     * 对话轮次
     */
    private Integer dialogTurn;

    /**
     * 通话状态(1:已接听;2:空号;3:关机;4:停机;5:无人接听;6:无法接通;7:通话中;8:呼叫失败;9:来电提醒;10:用户挂断;11:号码有误/不存在;12:黑名单;13:呼叫限制;15:接通限制;16:敏感;17:已转化;18:已失效)
     */
    private Integer callStatus;

    /**
     * 是否接通(0:否;1:是)
     */
    private Integer isConnect;

    /**
     * 交互文本
     */
    private String callDialog;

    /**
     * 录音地址
     */
    private String recordingPath;

    /**
     * 意向等级(A,B,C,D,E,F)
     */
    private String intentionGrade;

    /**
     * 标签列表
     */
    private String tagList;

    /**
     * 预留字段1
     */
    private String reserveField1;

    /**
     * 大模型总结
     */
    private String returnResult;

    /**
     * 版本号
     */
    private String version;

    /**
     * 状态 0:待推送1:推送中2:推送成功3:推送失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 接收日期
     */
    private String receiveDate;

    private static final long serialVersionUID = 1L;
}