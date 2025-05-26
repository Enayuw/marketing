package com.br.marketing.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author peng.kang
 * @date 2025/5/26 18:16
 */
@Data
public class LogEncryptionCellsDto {
    @NotNull(message = "logCell cannot be null")
    private String logCell;
}
