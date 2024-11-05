package com.br.marketing.mapper;

import com.br.marketing.entity.Log360ai;
import com.br.marketing.entity.Log360aiExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface Log360aiMapper {
    int countByExample(Log360aiExample example);

    int deleteByExample(Log360aiExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Log360ai record);

    int insertSelective(Log360ai record);

    List<Log360ai> selectByExample(Log360aiExample example);

    Log360ai selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Log360ai record, @Param("example") Log360aiExample example);

    int updateByExample(@Param("record") Log360ai record, @Param("example") Log360aiExample example);

    int updateByPrimaryKeySelective(Log360ai record);

    int updateByPrimaryKey(Log360ai record);
}