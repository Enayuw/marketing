package com.br.marketing.mapper;

import com.br.marketing.entity.ZhonganMarketingBan;
import com.br.marketing.monkeydata.query.ZhongAnCellZkDateQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhonganMarketingBanMapper extends ZhonganMarketingBanMapperBase{

    List<ZhonganMarketingBan> getByZKData(@Param("apiCode")String apiCode,@Param("date")String date,@Param("limitStart") Integer limitStart);

    /**
     * 2022-12-12 16:17
     * 获取不营销手机号
     *
     * @param apiCode code
     * @param queries 众安名单锁定列表
     * @return list
     */
    List<ZhongAnCellZkDateQuery> getNotMarketingCell(@Param("apiCode") String apiCode
            , @Param("queries") List<ZhongAnCellZkDateQuery> queries);
}