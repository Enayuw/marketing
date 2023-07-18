package com.br.marketing.vo;

import lombok.Data;

@Data
public class ConfigByApiCodeVO {

    /**
     * 是否是上传统计文件
     */
    private Integer isFast;

    /**
     * 执行环境
     */
    private String actionEnv;
}
