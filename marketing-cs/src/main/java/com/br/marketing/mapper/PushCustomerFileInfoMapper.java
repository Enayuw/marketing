package com.br.marketing.mapper;

import com.br.marketing.entity.PushCustomerFileInfo;
import com.br.marketing.entity.ZhongbangVoiceFileDetail;
import org.apache.ibatis.annotations.Param;

public interface PushCustomerFileInfoMapper extends PushCustomerFileInfoMapperBase {

    int updateFileInfoAndFileDetailtikv_(@Param("fileInfo") PushCustomerFileInfo fileInfo
            , @Param("fileDetail") ZhongbangVoiceFileDetail fileDetail
    );

}