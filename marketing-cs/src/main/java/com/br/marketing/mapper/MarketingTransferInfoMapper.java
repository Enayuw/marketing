package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTransferCell;
import com.br.marketing.entity.MarketingTransferInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface MarketingTransferInfoMapper extends MarketingTransferInfoMapperBase {

    /**
     * 根据主键查询记录对应的ApiCode、RequestId
     *
     * @param id 主键
     * @return List<MarketingTransferInfo>
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 14:05
     */
    @Select("select api_code, request_id, last, total, status, create_time, actual_num from b_marketing_transfer_info where id=#{id}")
    List<MarketingTransferInfo> findApiCodeRequestIdByIdList(@Param("id") Long id);

    /**
     * 根据ApiCode createTime last统计当天数据量
     *
     * @param apiCode    客户编码
     * @param createTime 创建时间
     * @param last       传输标记
     * @return int
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 14:05
     */
    @Select("SELECT id FROM b_marketing_transfer_info WHERE `status` in(2,4) AND api_code=#{apiCode} and date_format(create_time,'%Y-%m-%d') = str_to_date(#{createTime},'%Y-%m-%d') and last=#{last}")
    List<Long> countByApiCodAndLast(@Param("apiCode") String apiCode, @Param("createTime") Date createTime, @Param("last") String last);


    @Select("select bt.*,bs.cell from b_marketing_transfer_${apiCode} bt left join b_marketing_sync_${apiCode} bs on bt.cust_num = bs.cust_num where TO_DAYS(bt.create_time) = TO_DAYS(NOW()) and  bt.group_type = #{groupType} and bt.reserve_field1 =1")
    List<MarketingTransferCell> getSmyTransferDataByGroupType(@Param("apiCode") String apiCode, @Param("groupType") String groupType);



    @Select("SELECT request_id FROM b_marketing_transfer_info WHERE  api_code=#{apiCode} and create_time >= #{createTime} and last=#{last} limit 1")
    String  countByApiCodAndLastOne(@Param("apiCode") String apiCode, @Param("createTime") String createTime, @Param("last") String last);

    List<MarketingTransferInfo> getMarketingTransferInfoIdByValidPeriodRange(@Param("apiCode") String apiCode,
                                                                             @Param("validStartDate") String validStartDate,
                                                                             @Param("validEndDate") String validEndDate,
                                                                             @Param("page") int page,
                                                                             @Param("pageSize") int pageSize);


    int getTransferUnresolvedCount(@Param("apiCode") String apiCode, @Param("startDate") String startDate,@Param("endDate") String endDate);

//    @Select("SELECT request_id FROM b_marketing_transfer_info WHERE `status` in(2,4) and api_code=#{apiCode} and date_format(create_time,'%Y-%m-%d') = #{bizDate} and last=#{last} limit 1")
    String queryByApiCodAndLasttikv_(@Param("apiCode") String apiCode, @Param("bizDate") String bizDate, @Param("last") String last);

    /**
     * 根据apiCode和requestId查询转化数据
     * @param apiCode
     * @param requestId
     * @return
     */
    MarketingTransferInfo getByApiCodeAndRequestId(@Param("apiCode") String apiCode, @Param("requestId") String requestId);

}