package com.br.marketing.mapper;

import com.br.marketing.vo.CarClueInfoVo;

import java.util.List;
import java.util.Map;

public interface CarClueInfoMapper extends CarClueInfoMapperBase {

    List<CarClueInfoVo> selectList(Map<String, Object> params);

}