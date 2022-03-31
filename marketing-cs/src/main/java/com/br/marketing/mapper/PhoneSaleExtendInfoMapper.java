package com.br.marketing.mapper;

import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.vo.PhoneSaleInfoVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public  interface PhoneSaleExtendInfoMapper extends PhoneSaleExtendInfoMapperBase{

    List<PhoneSaleInfoVO> getDxRecordByTransferType(PhoneSaleRecordInfoDTO saleRecordInfoDTO);
}
