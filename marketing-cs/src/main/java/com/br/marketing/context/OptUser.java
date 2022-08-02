package com.br.marketing.context;

import com.br.marketing.entity.auth.MarketingUserDetail;
import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Data
@Component
public class OptUser {
    private MarketingUserDetail userDetail;
}
