package com.br.marketing.client.marketingapi.input;

import lombok.Data;

@Data
public class UploadDataUrlDTO {
    private UploadDataDTO uploadDataDTO;
    private String url;

}
