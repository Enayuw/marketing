package com.br.marketing.es.util;

import java.util.UUID;

/**
 * uuid生成
 *
 * @Author linquan.guo
 * @CreateDate 2020/12/31 10:22
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/12/31 10:22
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
public class UuidUtils {

    private UuidUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * uuid生成
     *
     * @param
     * @return
     */
    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
