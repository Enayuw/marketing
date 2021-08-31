package com.br.marketing.mapper;

import com.br.marketing.vo.ScoreRuleConfigPageVO;

import java.util.List;

public interface ScoreRuleConfigMapper extends ScoreRuleConfigMapperBase {
    /**
     * 根据查询条件获取分页数据
     *
     * @param search 搜索 跑分规则/CID/APIcode
     * @param status 使用状态
     * @param cts    创建时间开始
     * @param cte    创建时间结束
     * @param uts    更新时间开始
     * @param ute    更新时间结束
     * @return PageResult {@link ScoreRuleConfigPageVO}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/8/31 14:38
     */
    List<ScoreRuleConfigPageVO> findList(String search, int status, String cts, String cte, String uts, String ute);
}