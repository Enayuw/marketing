package com.br.marketing.dto.customer;

import lombok.Data;

import java.util.Date;

@Data
public class CallRecordBO{

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 0:正常队列 1:延迟队列
     */
    private Integer dataSource;

    /**
     * 公司ID
     */
    private Integer cid;

    /**
     *
     */
    private String apiCode;

    /**
     * userType场景
     */
    private String userType;

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
    private String caseNum;

    /**
     * 案件状态(0-导入中;1-等待外呼/导入成功;2-导入失败;3-上传失败;4-正在外呼;5-已完成;6-黑名单;7-案件受限;8-暂停;9-取消;10-中原银行-账户余额已足;11-自有tts准备中;12-外呼失败;13-接通限制;14-敏感;15-已转化;16-已失效;)
     */
    private Integer caseStatus;

    /**
     * 案件拨打次数
     */
    private Integer dialCount;


    /**
     * 拨打明细详情
     */
    private CallRecordDetailBO detail;

    /**
     * 入库时间
     */
    private Date createTime;

    @Override
    public String toString() {
        return "CallRecordBO{" +
                "id=" + id +
                ", dataSource=" + dataSource +
                ", cid=" + cid +
                ", apiCode='" + apiCode + '\'' +
                ", callBackType=" + callBackType +
                ", taskName='" + taskName + '\'' +
                ", taskId=" + taskId +
                ", caseNum='" + caseNum + '\'' +
                ", caseStatus=" + caseStatus +
                ", dialCount=" + dialCount +
                ", detail=" + detail +
                ", createTime=" + createTime +
                '}';
    }
}
