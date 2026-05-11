package com.br.marketing.mapper;

import com.br.marketing.dto.linkgo.ShortLinkRuleApiCodeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ShortLinkTransferRuleMapper {

    List<ShortLinkRuleApiCodeDTO> selectRuleApiCodeList(@Param("ruleCodes") Set<String> ruleCodes);
}
