package com.br.marketing.client.tag.dto;

import lombok.Data;

/**
 * @ClassName AntaiosResourceDTO
 * @Author kongbx
 * @Date 2025/3/19 15:08
 */
@Data
public class AntaiosResourceDTO<T> {

    private String apiCode;

    private T jsonData;

}
