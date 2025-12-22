package com.br.marketing.client.rulecleaning;

import lombok.Data;

/**
 * @ClassName DataCleanDTO
 * @Author hang.zhou
 * @Date 2025/11/11
 */
@Data
public class DataCleanDTO {

    private String apiCode;

    private Integer systemType;

    private Integer dataType;

    private Integer acceptType;

    private String jsonData;

}
