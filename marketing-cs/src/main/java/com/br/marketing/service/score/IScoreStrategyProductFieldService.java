package com.br.marketing.service.score;

import java.util.List;

/**
 * 评分产品字段
 *
 * @author Guo Zeqiang
 * @date 2024-08-15 15:32
 */
public interface IScoreStrategyProductFieldService {

    /**
     * 2024-08-15 15:28
     * 分页获取跑分文件析出评分产品字段集合
     */
    List<String> getFieldNamePage(String apiCode, int current, int size);
}
