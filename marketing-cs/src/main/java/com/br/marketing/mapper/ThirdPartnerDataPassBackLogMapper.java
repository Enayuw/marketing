package com.br.marketing.mapper;

import com.br.marketing.entity.ThirdPartnerDataPassBackLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ThirdPartnerDataPassBackLogMapper extends ThirdPartnerDataPassBackLogMapperBase{

    int saveBatch(List<ThirdPartnerDataPassBackLog> ThirdPartnerDataPassBackLogList);

    int updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Integer status);
}
