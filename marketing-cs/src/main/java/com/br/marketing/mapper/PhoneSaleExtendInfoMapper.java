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
     *
     * @param phone
     * @param dxType
     * @param date
     */
    int countByPhoneAndType(@Param("phone") String phone, @Param("dxType") String dxType, @Param("date") String date);

    /**
     * 2022/7/13 17:36
     * 获取推送电销人工电销的记录
     *
     * @param apiCode       apiCode
     * @param userType      场景
     * @param startDateTime 开始时间 闭
     * @param endDateTime   结束时间 开
     * @param pageNum       页号
     * @param pageSize      页大小
     * @return list
     */
    List<PhoneSaleExtendInfo> findPushPhoneSaleListPage(
            @Param("apiCode") String apiCode,
            @Param("userType") String userType,
            @Param("startDateTime") String startDateTime,
            @Param("endDateTime") String endDateTime,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize);
}
