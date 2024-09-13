package com.br.marketing.enums;

import lombok.Getter;

/**
 * @ClassName InterfaceOperationsEnum
 * @Description 接口操作枚举
 * @Author kongbx
 * @Date 2024/4/22 18:04
 */
@Getter
public enum InterfaceOperationsEnum {

    XIECHENG_INSERT_DATA("700001","携程生成数据接口"),
    XIECHENG_SAVE_COLLIDING_RULE("100000","创建数据包"),
    XIECHENG_UPDATE_PRIORITY("100001","修改数据包优先级"),
    XIECHENG_UPDATE_COLLIDING_SWITCH("100002","规则启用禁用"),
    XIECHENG_UPDATE_COLLIDING_RULE("100003","修改撞库规则"),
    XIECHENG_MAKE_COLLIDING_PACKAGE("100004","生成数据包"),
    XIECHENG_DELETE_COLLIDING_PACKAGE("100005","删除数据包"),
    XIECHENG_UPDATE_ROUND("100007","修改数据包轮次"),

    BI_ADD_DISTRIBUTED_REPORT("200000","新增跑分分布报表"),
    BI_DOWNLOAD_DISTRIBUTED_REPORT("200001","下载评分分布报表"),
    BI_DOWNLOAD_REPORT("200002","下载BI报表"),
    ;
    /**
     * 接口状态码
     */
    private final String code;

    /**
     * 信息
     */
    private final String message;

    InterfaceOperationsEnum(String code, String message) {
        this.code = code;
        this.message=message;
    }

}
