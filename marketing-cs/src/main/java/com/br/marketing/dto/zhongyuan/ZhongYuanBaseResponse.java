package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;

/**
 * 中原消金公共响应基类
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class ZhongYuanBaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码（0000000表示成功）
     */
    private String code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     */
    public static <T> ZhongYuanBaseResponse<T> success(T data) {
        ZhongYuanBaseResponse<T> response = new ZhongYuanBaseResponse<>();
        response.setCode("0000000");
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }

    /**
     * 失败响应
     */
    public static <T> ZhongYuanBaseResponse<T> fail(String code, String message) {
        ZhongYuanBaseResponse<T> response = new ZhongYuanBaseResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}

