package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * @author peng.kang
 * @description: 转化文件按提取到marketingBI
 * @date 2025/4/22 20:01
 */
public interface TransferFileExtractToDorisMapper {
    Integer insertDataToMarketingBiTable(@Param("insertDorisSql") String insertDorisSql);

    void deleteDataFromMarketingBiTable(@Param("deleteDorisSqlByDate") String deleteDorisSqlByDate);

    Integer countDataFromMarketingBiTable(@Param("countSql") String countSql);
}
