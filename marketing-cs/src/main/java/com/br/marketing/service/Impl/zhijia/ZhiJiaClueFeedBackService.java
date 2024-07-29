package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.common.commondto.Result;

/**
 * @ClassName ZhiJiaClueFeedBackService
 * @Description TODO
 * @Author kongbx
 * @Date 2024/7/10 15:44
 */
public interface ZhiJiaClueFeedBackService {
    Result<Boolean> process(Long id);
}
