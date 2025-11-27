package com.br.marketing.vo.bi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Y轴数据
 *
 * @author senyang.zheng
 * @date 2024/08/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrapDataVO {
    @Schema(description = "Y轴名称")
    private String name;
    @Schema(description = "Y轴数据")
    private List<String> data;
}
