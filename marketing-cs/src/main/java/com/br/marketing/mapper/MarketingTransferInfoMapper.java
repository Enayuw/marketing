package com.br.marketing.mapper;

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
    @Select("select api_code, request_id, last, total, status, create_time from b_marketing_transfer_info where id=#{id}")
    List<MarketingTransferInfo> findApiCodeRequestIdByIdList(@Param("id") Long id);

    /**
     * 根据ApiCode createTime 统计当天数据量
     *
     * @param apiCode    客户编码
     * @param createTime 创建时间
     * @return int
     * @author Guo Zeqiang
     * @dateTime 2021/10/13 14:05
     */
    @Select("SELECT count(*) FROM b_marketing_transfer_info WHERE `status` in(2,4) AND api_code=#{apiCode} and date_format(create_time,'%Y-%m-%d') = str_to_date(#{createTime},'%Y-%m-%d')")
    Integer countByApiCodAnd(@Param("apiCode") String apiCode, @Param("createTime") Date createTime);
}