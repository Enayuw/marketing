package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingCleanDataFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MarketingCleanDataFileMapper extends MarketingCleanDataFileMapperBase {

    /**
     * 获取近一个月有数据的日期集合
     *
     * @param apiCode API编码
     * @param sftpPath sftp地址
     * @return 日期列表，格式：yyyy-MM-dd
     */
    List<String> getLastMonthDataDates(@Param("apiCode") String apiCode, @Param("sftpPath") String sftpPath);

    MarketingCleanDataFile getCleanDataFileByDate(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate, @Param("sftpPath") String sftpPath);

}
