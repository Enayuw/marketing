package com.br.marketing.service.mark;

import com.br.marketing.common.commondto.Result;

/**
 * @ClassName PpRonShuMarkService
 * @Author hong.chen
 * @Date 2025/2/19 18:14
 */
public interface PpRonShuMarkService {

    Result<Boolean> createCleanTask(Long localId);

}
