package com.br.marketing.service.tccpa;

import org.springframework.stereotype.Service;

/**
 * @Description TcSampleDataDownService
 * @Author xiong.luo
 * @CreateTime 2025/08/13
 */
@Service
public interface TcCpaSampleDataDownService {

    void process(String apiCode);
}
