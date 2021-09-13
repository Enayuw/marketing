package com.br.marketing.mapper;

import com.br.marketing.entity.SoleOptLog;

import java.util.List;

public interface SoleOptLogMapper extends SoleOptLogMapperBase{

    /**
     * 查看去重规则变更记录
     * @param parseLong
     * @return
     */
    List<SoleOptLog> selectListById(long parseLong);

}