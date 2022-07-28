package com.br.marketing.service;

import com.br.marketing.common.enums.TableCodeEnum;
import com.br.marketing.entity.auth.MarketingUserDetail;

public interface EntityOptService {
    <T> void saveOpt(T entity, MarketingUserDetail user, TableCodeEnum tableCodeEnum);

    <T> void updateOpt(T newEntity, T oleEntity, MarketingUserDetail user, TableCodeEnum tableCodeEnum);
}
