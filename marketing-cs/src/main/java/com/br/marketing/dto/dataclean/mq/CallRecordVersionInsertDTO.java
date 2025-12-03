package com.br.marketing.dto.dataclean.mq;

import lombok.Data;

/**
 * 通话记录版本明细表异步入库消息DTO
 * 
 * @author kongbx
 * @date 2025/11/26
 */
@Data
public class CallRecordVersionInsertDTO {

    /**
     * 版本明细表名
     */
    private String tableName;

    /**
     * 版本明细表插入后的主键ID
     */
    private Long dataId;
}

