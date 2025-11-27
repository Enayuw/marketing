package com.br.marketing.dto.tag;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 标签详情DTO
 */
@Data
@Schema(description = "标签详情DTO")
public class TagDetailDTO {
    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 标签编码
     */
    private String tagCode;

    /**
     * 数据源编码
     */
    private String sourceCode;

    /**
     * 条件树
     */
    private JSONObject conditionTree;

    /**
     * 时间范围
     */
    private String timeRange;

    /**
     * 规则总结
     */
    private String summary;

    /**
     * 用户范围API编码列表
     */
    private List<String> scopeApiCodes;

    /**
     * 授权范围API编码列表
     */
    private List<String> authorizedApiCodes;

    /**
     * 操作人ID
     */
    private Long optUserId;

    /**
     * 操作人名称
     */
    private String optUserName;

    /**
     * 标签状态
     */
    private Integer status;
}