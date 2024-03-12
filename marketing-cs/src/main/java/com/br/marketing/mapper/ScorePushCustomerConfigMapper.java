package com.br.marketing.mapper;

import com.br.marketing.entity.ScorePushCustomerConfig;

import java.util.List;

public interface ScorePushCustomerConfigMapper extends ScorePushCustomerConfigMapperBase{
    List<ScorePushCustomerConfig> getConfigByFileId(Long fileId);
}