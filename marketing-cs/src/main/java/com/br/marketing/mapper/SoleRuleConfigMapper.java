package com.br.marketing.mapper;

import com.br.marketing.entity.SoleRuleConfig;
import com.br.marketing.vo.SoleRuleVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SoleRuleConfigMapper extends SoleRuleConfigMapperBase {
    /**
     * 去重规则列表
     * @param soleName
     * @param status
     * @param createTimeStart
     * @param createTimeEnd
     * @param updateTimeStart
     * @param updateTimeEnd
     * @return
     */
    List<SoleRuleVO> selectList(@Param("soleName") String soleName, @Param("status") Integer status,
                                @Param("createTimeStart") String createTimeStart, @Param("createTimeEnd") String createTimeEnd,
                                @Param("updateTimeStart") String updateTimeStart, @Param("updateTimeEnd") String updateTimeEnd);

    /**
     * 是否有重复的去重规则
     * @return
     */
    int getRuleOfSoleOnly(@Param("soleId") Long soleId, @Param("soleFields") String soleFields,
                          @Param("soleCycleTimes") Integer soleCycleTimes, @Param("cid") Long cid,
                          @Param("conditionInfo") String conditionInfo,
                          @Param("allUserType") Integer allUserType);

    /**
     * 修改去重规则
     * @param soleRuleConfig
     */
    void updateById(SoleRuleConfig soleRuleConfig);
}