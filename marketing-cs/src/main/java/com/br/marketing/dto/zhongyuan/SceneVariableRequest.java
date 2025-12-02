package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;

/**
 * 场景变量查询请求DTO
 *
 * @author kongbx
 * @date 2025/11/14
 */
@Data
public class SceneVariableRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景代码
     */
    private String sceneCode;
}

