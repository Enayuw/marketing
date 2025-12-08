package com.br.marketing.monkey.service.syj;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface CustomerApiService {

    Map<String, String> callCustomerApi(Object reqMap, String url);
}
