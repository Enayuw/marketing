package com.br.marketing.mapper;

import com.br.marketing.vo.SoleOptLogVO;

import java.util.List;

public interface SoleOptLogMapper extends SoleOptLogMapperBase{

    /**
     * 查看去重规则变更记录
     * @param parseLong
     * @return
     */
    List<SoleOptLogVO> selectListById(long parseLong);

}