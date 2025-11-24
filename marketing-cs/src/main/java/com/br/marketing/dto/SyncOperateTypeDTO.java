package com.br.marketing.dto;

import lombok.Data;

@Data
public class SyncOperateTypeDTO {


    /**
     * 新场景-替代group_type
     */
    private String userType;

    /**
     * 操作类型
     */
    private String operateType;
    /**
     * 执行日期
     */
    private String appletDate;


    private String apiCode;



}
