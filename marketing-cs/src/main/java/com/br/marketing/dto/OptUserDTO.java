package com.br.marketing.dto;

import com.br.marketing.entity.auth.MarketingUserDetail;
import io.swagger.annotations.ApiModelProperty;

public class OptUserDTO {
    @ApiModelProperty(value = "用户上下文信息",hidden = true)
    private MarketingUserDetail user;

    public MarketingUserDetail getUser() {
        return user;
    }

    public void setUser(MarketingUserDetail user) {
        this.user = user;
    }
}
