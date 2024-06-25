package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QueryUserRealMessageMapper extends QueryUserRealMessageMapperBase{

    /**
     * 更新状态
     * @param status status
     * @param idList 待更新id集合
     */
    void updateStatusByIdList(@Param("status") Integer status, @Param("idList") List<Long> idList);

}
