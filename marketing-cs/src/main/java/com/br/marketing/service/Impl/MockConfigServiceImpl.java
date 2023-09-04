package com.br.marketing.service.Impl;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
public class MockConfigServiceImpl {
    /**
     * 消失的时间（耗时）
     *
     * @param maxTimeConsuming 最大耗时 单位：毫秒
     * @param minTimeConsuming 最少耗时 单位：毫秒
     * @author Guo Zeqiang
     * @dateTime 2023-08-09 22:13
     */
    @SneakyThrows
    public void disappearTime(int maxTimeConsuming, int minTimeConsuming) {
        if (minTimeConsuming <= 0 || minTimeConsuming > maxTimeConsuming) {
            throw new IllegalArgumentException();
        }
        SecureRandom secureRandom = new SecureRandom();
        int tt = (secureRandom.nextInt(maxTimeConsuming - minTimeConsuming) + minTimeConsuming);
        TimeUnit.MILLISECONDS.sleep(tt);
    }
}
