package com.br.marketing.dto;

import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ExecuteCarClueDTO
 * @Description 车线索手动执行记录
 * @Author kongbx
 * @Date 2025/5/6 14:51
 */
@Data
public class ExecuteCarClueDTO {

    @Schema(description = "需要执行的id集合")
    private List<Long> clueIds;
    @Schema(description = "待执行线索范围条件")
    private Object clueRange;
    @Schema(description = "执行类型 0-清洗 1-推送")
    private int executeType;
    @Schema(description = "执行状态 0-待执行 1-执行完成")
    private int executeStatus;

}
