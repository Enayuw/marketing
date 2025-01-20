package com.br.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiRecordLogDTO {
    private Boolean isDbLog;
    private Boolean isFileLog;
}
