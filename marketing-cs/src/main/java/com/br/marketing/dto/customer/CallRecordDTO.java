package com.br.marketing.dto.customer;

import lombok.Data;


@Data
public class CallRecordDTO {

    /**
     * 公司ID
     */
    private Integer cid;

    /**
     *
     */
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
    private CallRecordDetailDTO detail;

    @Override
    public String toString() {
        return "CallRecordDTO{" +
                "cid=" + cid +
                ", apiCode='" + apiCode + '\'' +
                ", callBackType=" + callBackType +
                ", taskName='" + taskName + '\'' +
                ", taskId=" + taskId +
                ", caseNum='" + caseNum + '\'' +
                ", caseStatus=" + caseStatus +
                ", dialCount=" + dialCount +
                ", detail=" + detail +
                '}';
    }
}
