package com.br.marketing.mapper.shunfeng;

import com.br.marketing.entity.ShunfengCompanyData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShunfengCompanyDataMapper extends ShunfengCompanyDataMapperBase{


    List<ShunfengCompanyData> getCompanyNameList(@Param("localId") Long id, @Param("indexId")Long indexId);

    void updateDataStatus(@Param("ids")List<Long> ids, @Param("status")int status);
}
