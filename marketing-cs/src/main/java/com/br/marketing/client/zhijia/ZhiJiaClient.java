package com.br.marketing.client.zhijia;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName ZhiJiaClient
 * @Description TODO
 * @Author kongbx
 * @Date 2024/7/10 16:49
 */
@Component
@Slf4j
public class ZhiJiaClient {

    @Value("${api.zhijia.addC1HiqClueUrl:}")
    private String addC1HiqClue;

    @Value("${api.zhijia.zhiJiaClientId:}")
    private String zhiJiaClientId;

    @Value("${api.zhijia.zhiJiaClientSecret:}")
    private String zhiJiaClientSecret;

    @Value("${api.zhijia.zhiJiaClientAppid:}")
    private String zhiJiaClientAppid;


}
