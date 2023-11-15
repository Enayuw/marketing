package com.br.marketing.mapper;

import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.PullCustomerFileDataExample;

import java.util.List;

public interface PullCustomerFileDataMapper extends PullCustomerFileDataMapperBase{
    List<PullCustomerFileData> selectPageListByExampletikv_(PullCustomerFileDataExample example);
}