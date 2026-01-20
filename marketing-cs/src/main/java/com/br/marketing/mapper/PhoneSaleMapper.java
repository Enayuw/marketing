package com.br.marketing.mapper;

import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExample;
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

    /**
     * 获取手机号分组总数
     */
    Integer getGroupByPhoneCount(@Param("localId") String localId);

    /**
     * 分页获取手机号分组
     */
    List<String> getGroupByPhoneWithPaging(@Param("localId") String localId,
                                          @Param("offset") Integer offset, 
                                          @Param("pageSize") Integer pageSize);

    List<DassImportDataDTO> getWeiZhongData(@Param("localId") String localId, @Param("phone") String phone, @Param("dataId") Long dataId);

    /**
     * 获取特殊文件数据（按手机号分组，按创建时间升序）
     */
    List<DassImportDataDTO> getSpecialData(@Param("localId") String localId, @Param("phone") String phone);
}