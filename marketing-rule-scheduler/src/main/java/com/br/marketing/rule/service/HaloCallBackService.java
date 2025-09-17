package com.br.marketing.rule.service;

import com.br.marketing.common.commondto.Result;
import org.springframework.stereotype.Service;

@Service
public interface HaloCallBackService {

    Result<Boolean> callBack(Long id);

}
