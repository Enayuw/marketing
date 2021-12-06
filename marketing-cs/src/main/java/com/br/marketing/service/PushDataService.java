package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

public interface PushDataService {
    Result pushDassData(Long id);

    Result pushSevenTransferData(Long id);

    Result pushHaierData(Long id);


    Result<Boolean> pushHaierTransferData(Long id);
}
