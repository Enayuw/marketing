package com.br.marketing.mapper;

import com.br.marketing.entity.QueryUserRealMessage;
import com.br.marketing.entity.QueryUserRealMessageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface QueryUserRealMessageMapperBase {
    long countByExample(QueryUserRealMessageExample example);

    int deleteByExample(QueryUserRealMessageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(QueryUserRealMessage record);

    int insertSelective(QueryUserRealMessage record);

    List<QueryUserRealMessage> selectByExample(QueryUserRealMessageExample example);

    QueryUserRealMessage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") QueryUserRealMessage record, @Param("example") QueryUserRealMessageExample example);

    int updateByExample(@Param("record") QueryUserRealMessage record, @Param("example") QueryUserRealMessageExample example);

    int updateByPrimaryKeySelective(QueryUserRealMessage record);

    int updateByPrimaryKey(QueryUserRealMessage record);
}