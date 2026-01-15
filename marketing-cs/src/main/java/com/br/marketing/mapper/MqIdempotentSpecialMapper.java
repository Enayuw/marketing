package com.br.marketing.mapper;

import com.br.marketing.entity.IdempotentRecordInfo;
import com.br.marketing.entity.MqIdempotentSpecial;
import com.br.marketing.entity.MqIdempotentSpecialExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MqIdempotentSpecialMapper {
    int countByExample(MqIdempotentSpecialExample example);

    int deleteByExample(MqIdempotentSpecialExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MqIdempotentSpecial record);

    int insertSelective(MqIdempotentSpecial record);

    List<MqIdempotentSpecial> selectByExample(MqIdempotentSpecialExample example);

    MqIdempotentSpecial selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MqIdempotentSpecial record, @Param("example") MqIdempotentSpecialExample example);

    int updateByExample(@Param("record") MqIdempotentSpecial record, @Param("example") MqIdempotentSpecialExample example);

    int updateByPrimaryKeySelective(MqIdempotentSpecial record);

    int updateByPrimaryKey(MqIdempotentSpecial record);

    /**
     * 根据幂等键删除记录
     * @param idempotentKey 幂等键
     * @return 删除的记录数
     */
    int deleteByIdempotentKey(@Param("idempotentKey") Long idempotentKey);

    /**
     * 根据幂等键查询记录
     * @param idempotentKey 幂等键
     * @return 幂等记录信息（包含id和apiCode），如果不存在返回null
     */
    IdempotentRecordInfo selectByIdempotentKey(@Param("idempotentKey") Long idempotentKey);
}