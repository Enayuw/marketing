package com.br.marketing.service.halo;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushMain;
import org.springframework.stereotype.Service;

@Service
public interface HaloRuleCenterCallbackService {

    Result saveHaloCallbackTask(PushCustomerDTO dto);

    Result canPushCallback(String apiCode);

    Integer queryExistError(Long id, Integer filterType);

    void makeUpCallbackData(CustomerInfoPushMain customerInfoPushMain);
}
