package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

public interface TaikangLeadTransferService {
    Result<Boolean> transferData(String id);
}
