package com.br.marketing.service.tc.impl;

import com.br.marketing.client.middleheaven.MiddleHeavenTcyrApiCodeMatchClient;
import com.br.marketing.config.biz.LingxiaoTcyrProperties;
import com.br.marketing.service.tc.TcyrDataCleanNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TcyrDataCleanNotifyServiceImpl implements TcyrDataCleanNotifyService {

    @Resource
    private LingxiaoTcyrProperties lingxiaoTcyrProperties;

    @Resource
    private MiddleHeavenTcyrApiCodeMatchClient middleHeavenTcyrApiCodeMatchClient;

    @Override
    public void notifyAsync(String batchNo) {
        if (!lingxiaoTcyrProperties.isCleanNotifyEnabled() || StringUtils.isBlank(batchNo)) {
            return;
        }
        String batch = batchNo.trim();
        CompletableFuture.runAsync(() -> {
            try {
                boolean ok = middleHeavenTcyrApiCodeMatchClient.postTcDataCleanNotify(
                        lingxiaoTcyrProperties.getBaseUrl(),
                        lingxiaoTcyrProperties.getCleanNotifyPath(),
                        lingxiaoTcyrProperties.getBearerToken(),
                        lingxiaoTcyrProperties.getConnectTimeoutMs(),
                        lingxiaoTcyrProperties.getReadTimeoutMs(),
                        batch);
                if (ok) {
                    log.info("tcDataCleanNotify 已调用 batchNo={}", batch);
                }
            } catch (Exception e) {
                log.warn("tcDataCleanNotify 异步调用异常 batchNo={} err={}", batch, e.getMessage(), e);
            }
        });
    }
}
