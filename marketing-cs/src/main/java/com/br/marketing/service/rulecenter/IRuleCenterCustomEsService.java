package com.br.marketing.service.rulecenter;

import com.br.marketing.common.commondto.ApiResult;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface IRuleCenterCustomEsService {

    ApiResult<Map<String, Object>> queryEsData(Long id);

}
