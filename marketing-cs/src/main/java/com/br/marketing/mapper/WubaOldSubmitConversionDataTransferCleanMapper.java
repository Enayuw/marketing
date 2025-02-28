package com.br.marketing.mapper;

import com.br.marketing.entity.WubaOldSubmitConversionDataTransferClean;

import java.util.List;

public interface WubaOldSubmitConversionDataTransferCleanMapper extends WubaOldSubmitConversionDataTransferCleanMapperBase{

    int batchAdd(List<WubaOldSubmitConversionDataTransferClean> list);

}