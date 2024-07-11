package com.br.marketing.mapper;

import com.br.marketing.entity.WubaSubmitConversionDataLog;

import java.util.List;

public interface WubaSubmitConversionDataLogMapper extends WubaSubmitConversionDataLogMapperBase {

    int batchAdd(List<WubaSubmitConversionDataLog> list);

}