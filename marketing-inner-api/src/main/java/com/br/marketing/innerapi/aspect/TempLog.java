package com.br.marketing.innerapi.aspect;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
@Data
public class TempLog {
    private String contextId;
}
