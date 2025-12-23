package com.br.marketing.dto.tc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcyrCpaDeleteRuleInfo {
    /**
     * 
     */
    private Long id;

    /**
     * 剔除规则名称
     */
    private String ruleName;
}