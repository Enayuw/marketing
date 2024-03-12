package com.br.marketing.mapper;

import com.br.marketing.dto.PhoneSaleRecordInfoDTO;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
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
    List<PhoneSaleExtendInfo> getPPDToDxData(@Param("apiCode")String apiCode, @Param("startDate")String startDate,
                                             @Param("endDate")String endDate , @Param("limitStart") Integer limitStart);

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

    /**
     * 获取桔子转化a+a1+b+b1场景7天内推送3次记录
     * @param apiCode       apiCode
     * @param recordDate  T-7
     * @param custNums      案件编号
     * @return list
     */
    List<String> getJuziPushThreeRecordtikv_(
            @Param("apiCode") String apiCode,
            @Param("recordDate") String recordDate,
            @Param("custNums") List<String> custNums);

    /**
     * 2022/10/21 10:36
     * 获取不同情况下的案件编号
     *
     * @param apiCode    apiCode
     * @param custNumSet 案件编号集合
     * @param statusList 状态集合
     * @param appletDate 日期字符串
     * @return Set<String>
     */
    Set<String> getCustNumByCustNumAndStatusAndDateSet(
            @Param("apiCode") String apiCode,
            @Param("custNumSet") Set<String> custNumSet,
            @Param("statusList") List<String> statusList,
            @Param("appletDate") String appletDate);

    /**
     * 2022/10/20 10:36
     * 获取周期性推送dass的转化数据
     *
     * @param apiCode  apiCode
     * @param dateSet  日期集合
     * @param status   情况
     * @param pageNum  页号
     * @param pageSize 页大小
     * @return list
     */
    List<PhoneSaleExtendInfo> findOrangeCyclicalPage(
            @Param("apiCode") String apiCode,
            @Param("dateSet") Set<String> dateSet,
            @Param("status") String status,
            @Param("pageNum") int pageNum,
            @Param("pageSize") int pageSize);


    /**
     * 2023/02/14 10:36
     * 获取分页数据
     *
     * @param example  检索条件
     * @param pageNum  页号
     * @param pageSize 页大小
     * @return PhoneSaleExtendInfo list
     */
    List<PhoneSaleExtendInfo> findListPageByExample(@Param("example") PhoneSaleExtendInfoExample example, @Param("pageNum") int pageNum,
                                                    @Param("pageSize") int pageSize);

    /**
     * 2023/02/14 10:36
     * 获取分页数据
     *
     * @param example  检索条件
     * @param pageNum  页号
     * @param pageSize 页大小
     * @return PhoneSaleExtendInfo list
     */
    List<PhoneSaleExtendInfo> findListPageByExampleSqlStr(@Param("example") PhoneSaleExtendInfoExample example
            , @Param("sqlWhereStr") String sqlWhereStr
            , @Param("pageNum") int pageNum
            , @Param("pageSize") int pageSize);

    /**
     * 2023/02/14 10:36
     * 获取分页数据
     *
     * @param example 检索条件
     * @return set CustNum
     */
    Set<String> getCustNumSettikv_(@Param("example") PhoneSaleExtendInfoExample example);

    Set<String> getToDassLogInfoList(@Param("apiCode") String apiCode, @Param("cells") Set<String> cells);

    /**
     * 2023-06-26 17:34
     * 根据条件获取案件编号集合
     */
    List<String> selectCustNumByExampletikv_(@Param("example") PhoneSaleExtendInfoExample example);

    /**
     * 2023-08-25 17:34
     * 近推送的记录
     */
    List<PhoneSaleExtendInfo> findInfoByMaxPushDxTimeAndCellList(@Param("example") PhoneSaleExtendInfoExample example);

}
