package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

public interface RsTransferService {
    Result getRsToPolicy(String apiCode,String date);
}
