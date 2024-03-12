package com.br.marketing.entity;

import lombok.Data;

/**
 * @Description XieChengJudgeConvTypeValue
 * @Author hong.chen
 * @CreateTime 2023/09/16
 */
@Data
public class XieChengJudgeConvTypeValue {
    private String custNum;
    // 转化数据convType有110
    private Boolean hasRiskControl;
    // 转化数据convType有106
    private Boolean hasApplySuccess;
    // 转化数据convType有107
    private Boolean hasInputSuccess;
}
