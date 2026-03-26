package com.br.marketing.dto.derived;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 客户衍生信息查询请求
 */
@Data
public class CustDerivedQueryRequest {

    /**
     * 接口编码
     */
    @NotNull(message = "apiCode必传")
    @NotEmpty(message = "apiCode必传")
    private String apiCode;

    /**
     * 客户编号集合，最多50条
     */
    @NotNull(message = "custNumList必传")
    @NotEmpty(message = "custNumList不能为空")
    @Size(max = 50, message = "custNumList最多50条")
    private List<String> custNumList;
}
