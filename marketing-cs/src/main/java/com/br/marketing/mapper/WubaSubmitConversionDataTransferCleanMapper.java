package com.br.marketing.mapper;

import com.br.marketing.entity.WubaSubmitConversionDataTransferClean;

import java.util.List;

public interface WubaSubmitConversionDataTransferCleanMapper extends WubaSubmitConversionDataTransferCleanMapperBase{

    int batchAdd(List<WubaSubmitConversionDataTransferClean> list);

}