package com.br.marketing.mapper;

import com.br.marketing.entity.ThirdPartnerUploadDataClean;

import java.util.List;

public interface ThirdPartnerUploadDataCleanMapper extends ThirdPartnerUploadDataCleanMapperBase {
    void batchSaveByTaskId(List<ThirdPartnerUploadDataClean> list);
}
