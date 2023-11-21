package com.br.marketing.mapper;

import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.PullCustomerFileDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface PullCustomerFileDataMapper extends PullCustomerFileDataMapperBase {
    List<PullCustomerFileData> selectPageListByExampletikv_(PullCustomerFileDataExample example);

    int insertBatchSelective(@Param("recordList") List<PullCustomerFileData> recordList);

    Set<String> getDataFingerprintSet(@Param("localFileId") Long localFileId, @Param("recordList") List<PullCustomerFileData> recordList);
}