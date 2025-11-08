package com.br.marketing.vo.yunke;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author peng.kang
 * @date 2025/5/26 18:28
 */
@Data
@Schema(description = "机型获取")
public class DeviceTypeVO {
    @ApiModelProperty("log加密手机号")
    private String cell;

    @ApiModelProperty("log加密手机号对应的机型")
    private Integer deviceType;
}
