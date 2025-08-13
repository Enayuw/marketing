package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrCpaFailFile;
import com.br.marketing.entity.MarketingTcyrCpaFailFileExample;
import com.br.marketing.entity.MarketingTcyrCpaSuccessFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrCpaFailFileMapper extends MarketingTcyrCpaFailFileMapperBase {
    MarketingTcyrCpaFailFile selectFileByFilePath(
            @Param("apiCode") String apiCode,
            @Param("filePath") String filePath);
}