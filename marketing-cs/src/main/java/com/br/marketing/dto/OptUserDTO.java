package com.br.marketing.dto;

import com.br.marketing.entity.auth.MarketingUserDetail;
import io.swagger.v3.oas.annotations.media.Schema;

public class OptUserDTO {
    @Schema(description = "用户上下文信息",hidden = true)
    private MarketingUserDetail user;

    public MarketingUserDetail getUser() {
        return user;
    }

    public void setUser(MarketingUserDetail user) {
        this.user = user;
    }
}
