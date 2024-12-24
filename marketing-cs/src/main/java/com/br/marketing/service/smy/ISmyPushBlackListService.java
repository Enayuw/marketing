package com.br.marketing.service.smy;

import java.time.LocalDate;

/**
 * @Author bin.li1
 * @date 2024-12-23
 */
public interface ISmyPushBlackListService {
    /**
     * 推送黑名单数据给萨摩耶
     * 2024-10-29 18:03
     *
     * @param apiCode   接口码值
     * @param localDate 执行时间
     */
    void pushBlackList(String apiCode, LocalDate localDate,int pushStatus);

}
