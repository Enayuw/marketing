package com.br.marketing.mapper;

import com.br.marketing.entity.DidiCallBackData;

import java.util.List;

public interface DidiCallBackDataMapper extends DidiCallBackDataMapperBase {

    void batchAdd(List<DidiCallBackData> list);

}