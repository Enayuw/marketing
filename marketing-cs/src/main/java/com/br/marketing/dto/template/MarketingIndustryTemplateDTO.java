package com.br.marketing.dto.template;

import com.br.marketing.entity.MarketingIndustryTemplate;
import com.br.marketing.entity.MarketingIndustryTemplateJsonParse;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MarketingIndustryTemplateDTO
 * @Author hang.zhou
 * @Date 2025/10/31
 */
@Data
public class MarketingIndustryTemplateDTO {

    MarketingIndustryTemplate marketingIndustryTemplate;

    List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList;

}
