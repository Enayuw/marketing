package com.br.marketing.origin;

import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

/**
 * @Description MQ消息基类
 * @Author hong.chen
 * @CreateTime 2025/12/04
 */
@Data
public class BaseMqFact extends InterfaceParams {
    private Long idempotentKey;
}
