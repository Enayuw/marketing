package com.br.marketing.service.Impl.guomei;

import java.time.LocalDate;

/**
 * @author Hua Qiang
 * @date 2024-10-29 17:57
 */
public interface IGuoMeiDataCallbackService {
    /**
     * 用户数据回传
     * 2024-10-29 18:03
     *
     * @param apiCode   接口码值
     * @param localDate 执行时间
     */
    void pushDataCallback(String apiCode, LocalDate localDate);

}
