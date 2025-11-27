package com.br.marketing.service;

import cn.hutool.core.lang.Pair;
import com.br.marketing.common.commondto.ApiNoDataResult;

/**
 * 描述：： 根据有效期框定数据范围
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ValidityPeriodDataService
 * @author: it-yml
 * @create: 2023-08-25 21:22
 * @Version 1.0
 * --------------------------------------
 **/
public interface ValidityPeriodDataService {

    /**
     * 根据apiCode custNum 查询有效期并返回转化数据。
     * 返回 ture则剔除 false 则不剔除
     */
    Boolean judgmentMarketingTransferDataInvalidWithValidityPeriod(String apiCode,String custNum);

    /**
     * 查询有效期范围区间，用于匹配转化数据request_data，初步框定有效期范围内的数据（左右分别扩大一天，返回值需要判空！）
     */
    Pair<String, String> getMarketingTransferDataWithValidityRange(String apiCode);


    /**
     * 有效期变更--marketing-api 专用
     */
    ApiNoDataResult marketingValidityPeriod(String apiCode ,String jsonData);
}
