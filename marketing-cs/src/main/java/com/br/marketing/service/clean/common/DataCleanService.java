package com.br.marketing.service.clean.common;

import com.br.marketing.common.commondto.Result;

public interface DataCleanService {
    Result<Boolean> customerDataJsonParse(String  t);
}
