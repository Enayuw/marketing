package com.br.marketing.service.halo;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;
import org.springframework.stereotype.Service;

@Service
public interface HaloRuleCenterCallbackService {

    Result saveHaloCallbackTask(PushCustomerDTO dto);

    Result canPushCallback(String apiCode);
}
