package com.br.marketing.mapper;

import com.br.marketing.dto.CustomerScoreRuleDTO;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.CustomerScoreRuleVO;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import org.apache.ibatis.annotations.Param;

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
     * @param execType    周期类型
     * @return {@link List<ScoreRuleConfigPageVO>}
     * @author zeqiang.guo@brgroup.com
     * @dateTime 2021/8/31 14:38
     */
    @AddDataAuth
    List<ScoreRuleConfigPageVO> findList(@Param("search") String search
            , @Param("status") Integer status
            , @Param("cts") String cts
            , @Param("cte") String cte
            , @Param("uts") String uts
            , @Param("ute") String ute
            , @Param("execType") Integer execType);

    /**
     * 跑分规则下拉列表
     * @param apiCodeList
     * @return
     */
    List<ScoreRuleConfig> getScoreRules(@Param("apiCodeList")List<String> apiCodeList);

    List<CustomerScoreRuleVO> getScoreRuleVoList(
            @Param("ruleIdList")List<Long> ruleIdList,
            @Param("apiCodeList")List<String> apiCodeList);

    List<CustomerScoreRuleDTO> getScoreRuleDtoList(
            @Param("ruleIdList")List<Long> ruleIdList,
            @Param("apiCodeList")List<String> apiCodeList);
}