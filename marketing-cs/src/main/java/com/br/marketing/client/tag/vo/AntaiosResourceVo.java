package com.br.marketing.client.tag.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName AntaiosResourceVo
 * @Author kongbx
 * @Date 2025/3/19 15:11
 */
@Data
public class AntaiosResourceVo {
    /**
     * 响应状态码，"000000"表示成功
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 交易流水号
     */
    private String swiftNumber;

    /**
     * 返回的数据数组
     */
    private String data;

}
