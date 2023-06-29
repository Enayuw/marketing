package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengSmsQuitData;
import com.br.marketing.entity.YiqianbaoData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XiechengSmsQuitDataMapper extends XiechengSmsQuitDataMapperBase{

    List<XiechengSmsQuitData> getSmsQuitData(@Param("localId") Long localId, @Param("dataId")  Long dataId);

    Integer getCountSmsQuitDataByMobile(@Param("mobile") String mobile);
}
