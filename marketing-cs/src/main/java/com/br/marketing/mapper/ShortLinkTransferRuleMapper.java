package com.br.marketing.mapper;

import com.br.marketing.entity.ShortLinkTransferRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 短链转化规则 Mapper 接口
 * @author system
 * @date 2025/01/17
 */
public interface ShortLinkTransferRuleMapper {

    /**
     * 根据规则名称和时间范围查询规则代码列表
     * @param linkRuleName 规则名称
     * @param queryStartTime 开始时间
     * @param queryEndTime 结束时间
     * @return 规则代码列表
     */
    List<String> selectRuleCodeListByRule(@Param("linkRuleName") String linkRuleName,
                                          @Param("queryStartTime") String queryStartTime,
                                          @Param("queryEndTime") String queryEndTime);

    /**
     * 根据规则代码查询统计数量
     * @param linkRuleCode 规则代码
     * @return 统计数量
     */
    Long selectCountByRuleCode(@Param("linkRuleCode") String linkRuleCode);

    /**
     * 根据主键查询
     * @param id 主键
     * @return 短链规则
     */
    ShortLinkTransferRule selectByPrimaryKey(Long id);

    /**
     * 插入记录
     * @param record 记录
     * @return 影响行数
     */
    int insert(ShortLinkTransferRule record);

    /**
     * 选择性插入记录
     * @param record 记录
     * @return 影响行数
     */
    int insertSelective(ShortLinkTransferRule record);

    /**
     * 根据主键选择性更新
     * @param record 记录
     * @return 影响行数
     */
    int updateByPrimaryKeySelective(ShortLinkTransferRule record);

    /**
     * 根据主键更新
     * @param record 记录
     * @return 影响行数
     */
    int updateByPrimaryKey(ShortLinkTransferRule record);

    /**
     * 根据主键删除
     * @param id 主键
     * @return 影响行数
     */
    int deleteByPrimaryKey(Long id);
}
