package com.br.marketing.common.customizedassert;


import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.exception.KnowException;

/**
 * 基于result的定制断言
 */
public class AssertResult<T> {

    /**
     * 对result结果非正常进行断言
     *
     * @param result
     * @param <T>
     */
    public static <T> void assertResult(Result<T> result) {
        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            throw new KnowException(result.getMessage());
        }
    }

}
