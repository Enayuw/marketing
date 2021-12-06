package com.br.marketing.service;

import com.br.marketing.client.haier.output.Response2Entity;
import com.br.marketing.common.commondto.Result;

public interface PushDataService {
    Result pushDassData(Long id);

    Result pushSevenTransferData(Long id);

    Result pushHaierData(Long id);


    Result<Response2Entity> pushHaierTransferData(Long id);
}
