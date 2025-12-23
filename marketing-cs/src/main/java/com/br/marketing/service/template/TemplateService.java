package com.br.marketing.service.template;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.template.MarketingIndustryTemplateDTO;
import com.br.marketing.entity.MarketingIndustryTemplate;
import org.springframework.stereotype.Service;

@Service
public interface TemplateService {

    Result<Boolean> addTemplate(MarketingIndustryTemplateDTO marketingIndustryTemplateDTO);

    Result<PageResultReturn<MarketingIndustryTemplate>> queryAllTemplate(Integer current, Integer pageSize
            , String templateName, String firstDepartment, String secondDepartment, String apiType
            , Integer systemType, Integer dataType);

    Result<Boolean> editTemplate(MarketingIndustryTemplateDTO marketingIndustryTemplateDTO);

    Result<Boolean> deleteTemplate(Long id);

    Result<MarketingIndustryTemplateDTO> queryTemplateById(Long id);

}
