package com.br.marketing.vo.bi.param;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.vo.BaseAuthPermissionData;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import lombok.EqualsAndHashCode;

/**
 * BI报表查询参数
 *
 * @author senyang.zheng
 * @date 2024/08/28
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "BI报表请求参数")
public class BiReportParam extends BaseAuthPermissionData {

    private static final long serialVersionUID = 2749250551270549133L;

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @NotNull(message = "报表类型不能为空")
    @ApiModelProperty(value = "报表类型，必填字段")
    private String reportTypeName;

    @ApiModelProperty(value = "自定义查询条件")
    private JSONObject condition;
}
