package com.br.marketing.mapper;


import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExample;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface PhoneSaleMapper extends PhoneSaleMapperBase {

    List<DassImportDataDTO> getPushDassData(@Param("localId") Long localId, @Param("dataId") Long dataId);

    /**
     * 2023-06-26 17:40
     * 根据条件获取uid集合
     */
    List<String> selectUidByExampletikv_(PhoneSaleExample example);



    List<PhoneSale> getZhongYuanSaleByPage(@Param("apiCode")String apiCode, @Param("dxUserTypeList")List<String> dxUserTypeList,
                                           @Param("startDate")Date startDateFormat,@Param("endDate") Date endDateFormat, @Param("pageNum")int pageNum,
                                           @Param("pageSize")int pageSize);
}