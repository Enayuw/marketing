package com.br.marketing.es.bean;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 产品
 *
 * @Author linquan.guo
 * @CreateDate 2021/5/26 13:48
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/5/26 13:48
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {
    private static final long serialVersionUID = 1;
    /**
     * 模型code
     */
    private String code;
    /**
     * 模型版本
     */
    private String version;
    /**
     * 模型组合
     */
    @JSONField(name = "code_version")
    private String codeVersion;
    /**
     * 是否命中
     */
    private String flag;
    /**
     * 结果
     */
    private Double score;
}
