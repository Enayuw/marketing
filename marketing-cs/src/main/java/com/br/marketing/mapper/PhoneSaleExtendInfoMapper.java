package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.vo.PhoneSaleInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;



public  interface PhoneSaleExtendInfoMapper extends PhoneSaleExtendInfoMapperBase{

    List<PhoneSaleInfoVO> getDxRecordByTransferType(PhoneSaleRecordInfoDTO saleRecordInfoDTO);

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
}
