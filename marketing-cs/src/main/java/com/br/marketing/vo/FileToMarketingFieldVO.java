package com.br.marketing.vo;

import lombok.Data;

@Data
public class FileToMarketingFieldVO {
    /**
     * 文件头字段
     */
    private String headField;
    /**
     * 接口字段
     */
    private String interfaceField;
    /**
     * 必填字段标识: true/false
     */
    private Boolean isMust;
    /**
     * 默认值
     * 赋值优先级： 初始数据 > 动态赋值 > 默认值
     */
    private String defaultValue;
    /**
     * 是否属于扩展字段标识: true/false
     */
    private Boolean isExtend;
    /**
     * 动态字段赋值
     * 例如 值为name，则将获取该行数据name字段的值赋值给当前字段上
     */
    private String dynamicData;
    /**
     * 字典项配置
     * [{"男":"0","女":"1"}]
     */
    private String conversion;
    /**
     * 选填字段分组
     * 例如id和gender都配置上"groupOptional": "group"，默认会校验这两个字段的值是否有填其中一个（二选一有值即可）
     */
    private String groupOptional;

    /**
     * 是否针对时间格式进行转换
     */
    private Boolean isDateTransform;

    /**
     * 是否针对时间格式进行转换
     */
    private String dateTransformPattern;

}
