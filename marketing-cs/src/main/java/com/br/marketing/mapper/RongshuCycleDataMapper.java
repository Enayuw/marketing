package com.br.marketing.mapper;

import com.br.marketing.entity.RongshuCycleData;

import java.util.List;

public interface RongshuCycleDataMapper extends RongshuCycleDataMapperBase {


    List<RongshuCycleData> getCycleData(List<String> pushDates);







}
