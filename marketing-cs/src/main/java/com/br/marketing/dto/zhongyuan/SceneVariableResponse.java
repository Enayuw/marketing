package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;

/**
 * 场景变量响应DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class SceneVariableResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 变量代码
     */
    private String code;

    /**
     * 变量名称
     */
    private String name;

    /**
     * 默认值
     */
    private String defValue;
}

