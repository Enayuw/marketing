package com.br.marketing.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Created by Bairong on 2019/8/20.
 * 流失预警任务
 */
@Data
public class MarketingTask {
    private Integer id;
    @JSONField(name = "api_code")
    private String apiCode;
    @JSONField(name = "batch_number")
    private String batchNumber;
    @JSONField(name = "file_name")
    private String fileName;
    @JSONField(name = "strategy_id")
    private String strategyId;
    private String frequency;
    @JSONField(name = "create_time")
    private String createTime;
    @JSONField(name = "update_time")
    private String updateTime;
    @JSONField(name = "monitor_status")
    private Integer monitorStatus;
    private Integer status;
    @JSONField(name = "task_number")
    private Integer taskNumber;
    @JSONField(name = "actual_number")
    private Integer actualNumber;
    private Integer increment;

    private Long begin;
    private Long end;
    private String tableName;

    /**
     * 策略中文名称
     */
    @JSONField(name = "strategy_name")
    private String strategyName;
    @JSONField(name = "start_date")
    private String startDate;
    @JSONField(name = "close_date")
    private String closeDate;
    /**
     * 监控模式   1：一次性、2：周期性监控
     */
    @JSONField(name = "monitor_model")
    private Integer monitorModel;
    @JSONField(name = "is_check")
    private Integer isCheck;
    @JSONField(name = "hit_date")
    private String hitDate;
    /**
     * {"key":"","value":""}
     * key为具体字段名称，value为错误提示
     */
    @JSONField(name = "error_essage")
    private String errorMessage;
    @JSONField(name = "cus_batch")
    private String cusBatch;
    /**
     * 1.一次性查询
     * 2.首次全量，在定期监控存量变动数据
     * 3.严格按变动数据监控
     * 4.存量数据全量按周期监控
     */
    private Integer monitorType;

    private String queryBeginDate;
    private String queryEndDate;

    private Integer start;
    private Integer limit;

    @JSONField(name = "strategy_type")
    private String strategyType;

    /**
     *  1：cell修复id  2：id修复cell
     */
    private String isRepair;

    @JSONField(name = "context_id")
    private Long contextId;

    private Integer dataVolume;

    @Override
    public String toString() {
        return "LoanTask{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", fileName='" + fileName + '\'' +
                ", strategyId='" + strategyId + '\'' +
                ", frequency='" + frequency + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", monitorStatus=" + monitorStatus +
                ", status=" + status +
                ", taskNumber=" + taskNumber +
                ", actualNumber=" + actualNumber +
                ", increment=" + increment +
                ", begin=" + begin +
                ", end=" + end +
                ", tableName='" + tableName + '\'' +
                ", strategyName='" + strategyName + '\'' +
                ", startDate='" + startDate + '\'' +
                ", closeDate='" + closeDate + '\'' +
                ", monitorModel=" + monitorModel +
                ", isCheck=" + isCheck +
                ", hitDate='" + hitDate + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", cusBatch='" + cusBatch + '\'' +
                ", monitorType=" + monitorType +
                ", queryBeginDate='" + queryBeginDate + '\'' +
                ", queryEndDate='" + queryEndDate + '\'' +
                ", start=" + start +
                ", limit=" + limit +
                ", strategyType='" + strategyType + '\'' +
                ", isRepair='" + isRepair + '\'' +
                ", dataVolume=" + dataVolume +'\'' +
                ", context_id=" + contextId +
                '}';
    }
}