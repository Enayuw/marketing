package com.br.marketing.mapper;

import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.vo.PhoneSaleInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;



public  interface PhoneSaleExtendInfoMapper extends PhoneSaleExtendInfoMapperBase{

    List<PhoneSaleInfoVO> getDxRecordByTransferType(PhoneSaleRecordInfoDTO saleRecordInfoDTO);

    List<PhoneSaleInfoVO> getDxRecordLastOne(PhoneSaleRecordInfoDTO saleRecordInfoDTO);

    List<PhoneSaleInfoVO> getDxRecordLastTwo(PhoneSaleRecordInfoDTO saleRecordInfoDTO);

    List<String> getDxRecordCustByTransferType(PhoneSaleRecordInfoDTO saleRecordInfoDTO);
    /**
     * 批量插入
     * @param list
     */
    void saveBatch(@Param("list") List<PhoneSaleExtendInfo> list);

    /**
     * 批量更新
     * @param set
     */
    void updateBatch(@Param("set") Set<String> set);

    /**
     * 手机号是否重复
     * @param phone
     * @param dxType
     * @param date
     */
    int countByPhoneAndType(@Param("phone")String phone,@Param("dxType")String dxType,@Param("date")String date);

    /**
     * 拍拍贷新客转人工数据提取
     * @param apiCode
     * @param startDate
     * @param endDate
     * @param limitStart
     * @return
     */
    List<PhoneSaleExtendInfo> getPPDToDxData(@Param("apiCode")String apiCode,@Param("startDate")String startDate,
                                             @Param("endDate")String endDate ,@Param("limitStart") Integer limitStart);
}
