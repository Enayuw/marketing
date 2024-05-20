package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingEmailSendConfig;
import com.br.marketing.entity.MarketingEmailSendConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingEmailSendConfigMapperBase {
    long countByExample(MarketingEmailSendConfigExample example);

    int deleteByExample(MarketingEmailSendConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingEmailSendConfig record);

    int insertSelective(MarketingEmailSendConfig record);

    List<MarketingEmailSendConfig> selectByExample(MarketingEmailSendConfigExample example);

    MarketingEmailSendConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingEmailSendConfig record, @Param("example") MarketingEmailSendConfigExample example);

    int updateByExample(@Param("record") MarketingEmailSendConfig record, @Param("example") MarketingEmailSendConfigExample example);

    int updateByPrimaryKeySelective(MarketingEmailSendConfig record);

    int updateByPrimaryKey(MarketingEmailSendConfig record);
}