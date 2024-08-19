package com.br.marketing.service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreOptLog;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.vo.ScoreRuleVO;

import java.util.HashMap;
import java.util.List;

/**
 * 跑分配置记录
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/6 13:30
 */
public interface ScoreOptLogService {

    /**
     * 列表数据
     *
     * @param page     页号
     * @param pageSize 页大小
     * @param rid      配置主键
     * @param cid      客户id
     * @param apiCode  接口编号
     * @return {@link PageResultReturn} {@link List< ScoreOptLog >}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/6 13:42
     */
    PageResultReturn findListPage(int page, int pageSize, Long rid, String cid, String apiCode);

    /**
     * 保存变更记录
     *
     * @param scoreRuleVO 记录pojo
     * @return 影响的记录数
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/9/10 18:03
     */
    int save(ScoreRuleVO scoreRuleVO, int status, MarketingUserDetail userDetail,String conditionInfo);
}
