package com.br.marketing.mapper.datagroup;

import com.br.marketing.vo.datagroup.DataGroupConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DataGroupConfigMapper extends DataGroupConfigMapperBase{


    List<DataGroupConfigVO> selectGroupConfigList(@Param("ids") String ids,@Param("exactmatch") String exactmatch,@Param("apiCode") String apiCode);
}
