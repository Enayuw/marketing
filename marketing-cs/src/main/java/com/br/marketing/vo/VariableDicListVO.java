package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;


/**
 * 场景字段配置列表返回
 *
 * @author songjuanjuan
 * @dateTime 2021/10/21 17:49
 */
@Data
@Schema(description = "场景字段配置表")
public class VariableDicListVO{

    /**
     * 主键id
     */
    @Schema(description = "主键id")
    private Long id;

    /**
     * 商户
     */
    @Schema(description = "商户")
    @NotEmpty
    private String cid;

    /**
     * 用户编号
     */
    @Schema(description = "用户编号")
    @NotEmpty
    private String apiCode;

    /**
     * 字段名称
     */
    @Schema(description = "字段名称")
    @NotEmpty
    private String fieldName;

    /**
     * 字段值
     */
    @Schema(description = "字段值")
    @NotEmpty
    private String fieldValue;

    /**
     * 字段描述
     */
    @Schema(description = "字段描述")
    private String fieldDesc;

    /**
     * 默认有效期是N，代表T+N范围
     */
    @Schema(description = "默认有效期是N，代表T+N范围")
    private String validDaysDefault;

    /**
     * 有效期类型：0按日维度,1按月维度
     */
    @Schema(description = "有效期类型：0按日维度,1按月维度")
    private Integer validType;

    /**
     * 删除标志；1-正常；9-删除；
     */
    @Schema(description = "删除标志(1:正常;9:删除,默认1)")
    private Integer isDel;

    /**
     * 入库时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private String updateTime;
}
