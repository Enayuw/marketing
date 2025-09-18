package com.br.marketing.service.rulecenter;

import com.br.marketing.common.commondto.Result;
import org.springframework.stereotype.Service;

@Service
public interface IRuleCenterHaloCallbackService {

    Result<Boolean> callBack(Long id);


}
