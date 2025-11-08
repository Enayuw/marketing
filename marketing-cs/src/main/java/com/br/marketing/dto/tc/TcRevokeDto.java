package com.br.marketing.dto.tc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

@Data
public class TcRevokeDto extends TcDataDto{

    @Schema(description = "userKeyList")
    private List<String> userKeyList;

    @Override
    public String validate() {
        if (StringUtils.isEmpty(batchNo)) {
            return "缺少必输字段data.batchNo";
        }
        return null;
    }
}
