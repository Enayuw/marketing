package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import com.br.marketing.vo.ScoreRuleVO;

import java.util.List;

/**
 * 跑分配置接口
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/31 14:35
 */
public interface ScoreRuleConfigService {

    /**
     * 根据查询条件获取分页数据
     *
     * @param page     页号
     * @param pageSize 页大小
     * @param search   搜索 跑分规则/CID/APIcode
     * @param status   使用状态
     * @param cts      创建时间开始
     * @param cte      创建时间结束
     * @param uts      更新时间开始
     * @param ute      更新时间结束
     * @return {@link PageResultReturn} {@link List<ScoreRuleConfigPageVO>}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/8/31 14:38
     */
    PageResultReturn findListPage(int page, int pageSize, String search, Integer status, String cts, String cte, String uts, String ute);

    /**
     * 保存规则
     *
     * @param scoreRuleVO 规则数据
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/2 14:15
     */
    void save(ScoreRuleVO scoreRuleVO);
}
