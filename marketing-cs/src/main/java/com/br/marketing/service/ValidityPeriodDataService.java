package com.br.marketing.service;

import javafx.util.Pair;

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
    Pair<String, String> getMarketingTransferDataWithValidityRange(String apiCode);
}
