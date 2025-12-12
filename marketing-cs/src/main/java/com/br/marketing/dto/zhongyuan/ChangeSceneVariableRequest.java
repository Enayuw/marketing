package com.br.marketing.dto.zhongyuan;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 外呼任务场景变量修改请求DTO
 *
 * @author kongbx
 * @date 2025/11/28
 */
@Data
public class ChangeSceneVariableRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 场景代码
     */
    private String sceneCode;

    /**
     * 任务ID
     */
    private String taskUid;

    /**
     * 变量列表
     */
    private List<Variable> variableList;

    /**
     * 变量对象
     */
    @Data
    public static class Variable implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 变量代码
         */
        private String code;

        /**
         * 变量值
         */
        private String value;
    }
}

