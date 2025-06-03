package com.br.marketing.service.Impl.umeng;


import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.UMengData;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IUMengDataCallbackService {

    Result marketingCallback(String requestBody, HttpServletRequest request);

    Result callPolicyData(Long localId, String apiCode, String strategyCode, List<UMengData> uMengDataList);
}
