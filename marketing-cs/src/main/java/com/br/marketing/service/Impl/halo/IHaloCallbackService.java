package com.br.marketing.service.Impl.halo;

import java.time.LocalDate;

/**
 * @author xiong luo
 * @date 2025-09-01 17:57
 */
public interface IHaloCallbackService {
    /**
     * 用户数据回传
     * 2025-09-01 17:57
     *
     * @param batchNumber 批次号
     */
    void pushDataCallback(String batchNumber, String whereSql);
}
