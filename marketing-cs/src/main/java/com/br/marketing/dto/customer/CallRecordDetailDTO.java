package com.br.marketing.dto.customer;

import lombok.Data;

@Data
public class CallRecordDetailDTO {

    /**
     * 通话记录编号
     */
    private String sessionId;

    /**
     * 开始外呼时间
     */
    private Long callStartTime;

    /**
     * 外呼接通时间
     */
    private Long callConnectTime;

    /**
     * 外呼结束时间
     */
    private Long callEndTime;

    /**
     * 对话轮次
     */
    private Integer dialogTurn;

    /**
     * 通话状态(1-已接听;2-空号;3-关机;4-停机;5-无人接听;6-无法接通;7-通话中;8-呼叫失败;9-来电提醒;10-用户挂断;11-号码有误/不存在;12-黑名单;13-呼叫限制;14-无需拨打;15-接通限制;16-敏感;17-已转化;18-已失效;)
     */
    private Integer callStatus;

    /**
     * 是否接通(0-否;1-是)
     */
    private Integer isConnect;

    /**
     * 交互文本
     */
    private String callDialog;

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
     * 意向等级 A级(有明确意向）;B级(可能有意向);C级(明确拒绝);D级(用户忙);E级(拨打失败);F级(无效客户)
     */
    private String intentionGrade;

    /**
     * 标签列表
     */
    private String tagList;
    /**
     * 线路名称
     */
    private String lineName;

}
