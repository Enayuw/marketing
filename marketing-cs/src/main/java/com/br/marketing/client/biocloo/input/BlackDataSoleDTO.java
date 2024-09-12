package com.br.marketing.client.biocloo.input;

import com.br.marketing.dto.DataDistributeLogBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BlackDataSoleDTO extends DataDistributeLogBase<DataDTO> {
    private Long transferInfoId;
    private String apiCode;
    private String last;
}
