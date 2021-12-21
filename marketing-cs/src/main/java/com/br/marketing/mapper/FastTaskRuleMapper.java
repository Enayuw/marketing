package com.br.marketing.mapper;

import com.br.marketing.vo.FastTaskRuleListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FastTaskRuleMapper extends FastTaskRuleMapperBase {

    /**
     * 跑分记录列表
     * @param search
     * @param status
     * @param createTimeStart
     * @param createTimeEnd
     * @param updateTimeStart
     * @param updateTimeEnd
     * @param taskStatus
     * @return
     */
    List<FastTaskRuleListVO> selectList(@Param("search")String search, @Param("status")Integer status,
                                        @Param("createTimeStart")String createTimeStart, @Param("createTimeEnd")String createTimeEnd,
                                        @Param("updateTimeStart")String updateTimeStart, @Param("updateTimeEnd")String updateTimeEnd,
                                        @Param("taskStatus")Integer taskStatus);

    /**
     * 获取未跑分数据量
     * @param appletDate
     * @param userType
     * @param apiCode
     * @return
     */
    Integer getUnScoreNum(@Param("apiCode")String apiCode,@Param("appletDate")String appletDate, @Param("userType")String userType);
}