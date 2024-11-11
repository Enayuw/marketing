package com.br.marketing.dto.qifu;

import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import lombok.Data;

@Data
public class UpLoadCleanDTO extends UploadDataDTO {
    private Long dataId;
}
