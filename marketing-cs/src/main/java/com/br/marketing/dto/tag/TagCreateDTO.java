package com.br.marketing.dto.tag;


import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 标签创建DTO
 */
@Data
@Schema(description = "标签创建请求DTO")
public class TagCreateDTO {
    @NotBlank(message = "标签名称不能为空")
    @Schema(description = "标签名称", required = true)
    private String tagName;

    @NotNull(message = "条件树不能为空")
    @Schema(description = "条件树配置", required = true)
    private JSONObject conditionTree;

    @NotBlank(message = "数据源编码不能为空")
    @Schema(description = "数据源编码,多个用,分割", required = true)
    private String sourceCode;

    @NotBlank(message = "时间范围不能为空")
    @Schema(description = "时间范围(YESTERDAY-昨天,LAST_THREE_DAYS-最近三天,LAST_WEEK-最近一周,LAST_MONTH-最近一月,LAST_THREE_MONTHS-最近三月)", required = true)
    private String timeRange;

    @NotEmpty(message = "标签范围不能为空")
    @Schema(description = "标签统计范围APICode列表", required = true)
    private List<String> scopeApiCodes;

    @NotEmpty(message = "授权APICode不能为空")
    @Schema(description = "标签授权APICode列表", required = true)
    private List<String> authorizedApiCodes;

    @NotEmpty(message = "标签内容总结不能为空")
    @Schema(description = "标签内容总结", required = true)
    private String summary;

    @NotNull(message = "操作人id不能为空")
    @Schema(description = "操作人id", required = true)
    private Long optUserId;

    @NotEmpty(message = "操作人账户名不能为空")
    @Schema(description = "操作人账户名", required = true)
    private String optUserName;
}