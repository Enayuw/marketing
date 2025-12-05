package com.br.marketing.monkey.service.suiyiji;

import com.alibaba.fastjson2.JSON;
import com.br.marketing.aspect.Mockable;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.constants.MockConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @ClassName CustomerApiServiceImpl
 * @Author hang.zhou
 * @Date 2025/12/4
 */
@Service
public class CustomerApiServiceImpl implements CustomerApiService{

    @Resource
    private HttpProxyClient httpProxyClient;

    @Value("${api.syj.isProxy:false}")
    private Boolean isProxy;

    @Override
    @RetryMethod()
    @Mockable(mockName = MockConstants.TEST_OBJECT_RETURN)
    public Map<String, String> callCustomerApi(Object reqMap, String url) {
        return httpProxyClient.sendByCodeWithLog(
                reqMap,
                url,
                isProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE,
                JSON.toJSONString(reqMap),
                true,
                true
        );
    }
}
