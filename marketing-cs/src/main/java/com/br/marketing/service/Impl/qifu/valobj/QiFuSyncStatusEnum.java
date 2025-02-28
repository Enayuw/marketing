package com.br.marketing.service.Impl.qifu.valobj;


import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * QiFuSyncStatusEnum
 * 360AI同步状态枚举
 *
 * @Author zhen.Li1
 * @Date 2025-02-27
 */
@Getter
@AllArgsConstructor
public enum QiFuSyncStatusEnum {
    READY(0, "待清洗"),
    QUERY_COMPLETE(1, "查询完成"),
    SUCCESS(2, "清洗成功");


    private Integer value;
    private String desc;


    }
