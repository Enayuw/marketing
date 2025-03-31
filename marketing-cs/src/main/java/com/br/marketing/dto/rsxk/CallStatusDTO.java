package com.br.marketing.dto.rsxk;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class CallStatusDTO {
    /* 1.外呼 2.不外呼 */
    private int callFlag;

    private List<String> recommendList;
}
