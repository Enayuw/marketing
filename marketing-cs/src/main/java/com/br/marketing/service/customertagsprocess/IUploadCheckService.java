package com.br.marketing.service.customertagsprocess;

import com.br.marketing.dto.MarketingPreUserDetailDTO;

import java.util.Map;

public interface IUploadCheckService {
    void check3key(MarketingPreUserDetailDTO user, Integer isCheck);
}
