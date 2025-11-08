package com.br.marketing.dto.tag;

import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName TagEffectiveDTO
 * @Author kongbx
 * @Date 2025/3/21 16:41
 */
@Data
public class TagEffectiveDTO {

    @ApiModelProperty("标签编码")
    private String tagCode;

    @ApiModelProperty("标签名称")
    private String tagName;

}
