package com.br.marketing.dto.tc;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TcRevokeDto {

    @ApiModelProperty(value = "batchNo")
    @NotNull(message = "batchNo必传")
    @NotEmpty(message = "batchNo必传")
    private String batchNo;

    @ApiModelProperty(value = "userKeyList")
    private List<String> userKeyList;

    public String validate() {
        if (StringUtils.isEmpty(batchNo)) {
            return "缺少必输字段data.batchNo";
        }
        return null;
    }
}
