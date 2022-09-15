package com.br.marketing.service;

import com.br.marketing.common.commondto.ApiResult;

/**
 * halo洗数接口
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.service
 * @Description: halo洗数接口
 * @CreateTime: 2022-07-01 10 :33
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
public interface HaloHistoryCleanService {
    /**
     * 清洗数据
     * @param
     */
    ApiResult<Boolean> cleanHistory(String jsonData );


    Integer handlerCleanHistory(String jsonData);
}
