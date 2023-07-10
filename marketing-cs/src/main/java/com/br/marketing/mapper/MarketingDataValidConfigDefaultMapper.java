package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfigDefault;
import com.br.marketing.entity.MarketingDataValidConfigDefaultExample;

import java.util.List;

public interface MarketingDataValidConfigDefaultMapper extends MarketingDataValidConfigDefaultMapperBase {

    List<MarketingDataValidConfigDefault> selectValidDaysByExample(MarketingDataValidConfigDefaultExample example);

}