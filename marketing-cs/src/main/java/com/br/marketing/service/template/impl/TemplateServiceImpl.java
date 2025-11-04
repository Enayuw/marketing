package com.br.marketing.service.template.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.template.MarketingIndustryTemplateDTO;
import com.br.marketing.entity.MarketingIndustryTemplate;
import com.br.marketing.entity.MarketingIndustryTemplateExample;
import com.br.marketing.entity.MarketingIndustryTemplateJsonParse;
import com.br.marketing.entity.MarketingIndustryTemplateJsonParseExample;
import com.br.marketing.mapper.MarketingIndustryTemplateJsonParseMapper;
import com.br.marketing.mapper.MarketingIndustryTemplateMapper;
import com.br.marketing.service.template.TemplateService;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName TemplateServiceImpl
 * @Author hang.zhou
 * @Date 2025/10/30
 */
@Service
public class TemplateServiceImpl implements TemplateService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);

    @Resource
    private MarketingIndustryTemplateMapper marketingIndustryTemplateMapper;

    @Resource
    private MarketingIndustryTemplateJsonParseMapper marketingIndustryTemplateJsonParseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addTemplate(MarketingIndustryTemplateDTO marketingIndustryTemplateDTO) {
        MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateDTO.getMarketingIndustryTemplate();
        List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList = marketingIndustryTemplateDTO.getMarketingIndustryTemplateJsonParseList();

        //非空校验
        String errorMsg = paramValid(marketingIndustryTemplate);
        if (StringUtils.isNotBlank(errorMsg)) {
            logger.error("必填参数缺失：{}", errorMsg);
            return new Result<Boolean>().failure().setMessage("必填参数缺失：" + errorMsg).setDate(Boolean.FALSE);
        }
        marketingIndustryTemplate.setIsDel(1);
        marketingIndustryTemplate.setCreateTime(new Date());
        marketingIndustryTemplate.setUpdateTime(new Date());
        try {
            //新增行业模板
            marketingIndustryTemplateMapper.insertSelective(marketingIndustryTemplate);
            //批量插入json数据
            marketingIndustryTemplateJsonParseList.forEach(item -> {
                item.setInterfaceTemplateId(marketingIndustryTemplate.getId());
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
            });
            marketingIndustryTemplateJsonParseMapper.batchInsert(marketingIndustryTemplateJsonParseList);

            logger.warn("新增行业模板成功，行业模板名称：{}", marketingIndustryTemplate.getTemplateName());
            return new Result<>().success().setDate(Boolean.TRUE);
        } catch (Exception e) {
            logger.error("新增行业模板失败，行业模板名称：{}，error：{}", marketingIndustryTemplate.getTemplateName(), e.getMessage());
            throw new RuntimeException("新增行业模板失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<PageResultReturn<MarketingIndustryTemplate>> queryAllTemplate(Integer current, Integer pageSize, String templateName, String firstDepartment, String secondDepartment, String apiType) {
        PageHelper.startPage(current, pageSize);

        MarketingIndustryTemplateExample example = new MarketingIndustryTemplateExample();
        MarketingIndustryTemplateExample.Criteria criteria = example.createCriteria();
        criteria.andIsDelEqualTo(1);
        if (StringUtils.isNotBlank(templateName)) {
            criteria.andTemplateNameEqualTo(templateName);
        }
        if (StringUtils.isNotBlank(firstDepartment)) {
            criteria.andFirstDepartmentEqualTo(firstDepartment);
        }
        if (StringUtils.isNotBlank(secondDepartment)) {
            criteria.andSecondDepartmentEqualTo(secondDepartment);
        }
        if (StringUtils.isNotBlank(apiType)) {
            criteria.andApiTypeEqualTo(apiType);
        }
        try {
            List<MarketingIndustryTemplate> marketingIndustryTemplateList = marketingIndustryTemplateMapper.selectByExample(example);
            if (!marketingIndustryTemplateList.isEmpty()) {
                logger.warn("查询行业模板成功，行业模板总条数：{}", marketingIndustryTemplateList.size());
                return new Result<PageResultReturn<MarketingIndustryTemplate>>().success().setDate(PageResultReturn.setPageResult(marketingIndustryTemplateList, current, pageSize));
            } else {
                logger.warn("未查询到行业模板信息，查询条件：templateName={}，firstDepartment={}，secondDepartment={}，apiType={}",
                        templateName, firstDepartment, secondDepartment, apiType);
                return new Result<PageResultReturn<MarketingIndustryTemplate>>().success().setMessage("未查询到行业模板信息").setDate(null);
            }
        } catch (Exception e) {
            logger.error("查询行业模板信异常，查询条件：templateName={}，firstDepartment={}，secondDepartment={}，apiType={}",
                    templateName, firstDepartment, secondDepartment, apiType);
            throw new RuntimeException("查询行业模板信异常失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> editTemplate(MarketingIndustryTemplateDTO marketingIndustryTemplateDTO) {
        MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateDTO.getMarketingIndustryTemplate();
        List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList = marketingIndustryTemplateDTO.getMarketingIndustryTemplateJsonParseList();

        MarketingIndustryTemplateExample example = new MarketingIndustryTemplateExample();
        example.createCriteria().andIdEqualTo(marketingIndustryTemplate.getId());
        try {
            marketingIndustryTemplateMapper.updateByExampleSelective(marketingIndustryTemplate, example);

            for (MarketingIndustryTemplateJsonParse marketingIndustryTemplateJsonParse : marketingIndustryTemplateJsonParseList) {
                MarketingIndustryTemplateJsonParseExample jsonParseExample = new MarketingIndustryTemplateJsonParseExample();
                jsonParseExample.createCriteria().andIdEqualTo(marketingIndustryTemplate.getId());
                marketingIndustryTemplateJsonParseMapper.updateByExampleSelective(marketingIndustryTemplateJsonParse, jsonParseExample);
            }

            logger.warn("修改行业模板成功，行业模板id：{}", marketingIndustryTemplate.getId());
            return new Result<>().success().setDate(Boolean.TRUE);
        } catch (Exception e) {
            logger.error("修改行业模板失败，行业模板id：{}，error：{}", marketingIndustryTemplate.getId(), e.getMessage());
            throw new RuntimeException("修改行业模板失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> deleteTemplate(Long id) {
        try {
            marketingIndustryTemplateMapper.deleteByPrimaryKey(id);
            MarketingIndustryTemplateJsonParseExample example = new MarketingIndustryTemplateJsonParseExample();
            example.createCriteria().andInterfaceTemplateIdEqualTo(id);
            marketingIndustryTemplateJsonParseMapper.deleteByExample(example);
            logger.warn("删除行业模板成功，行业模板id：{}", id);
            return new Result<>().success().setDate(Boolean.TRUE);
        } catch (Exception e) {
            logger.error("删除行业模板失败，行业模板id：{}，error：{}", id, e.getMessage());
            throw new RuntimeException("删除行业模板失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Result<MarketingIndustryTemplateDTO> queryTemplateById(Long id) {
        MarketingIndustryTemplateDTO marketingIndustryTemplateDTO = new MarketingIndustryTemplateDTO();
        try {
            MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateMapper.selectByPrimaryKey(id);

            MarketingIndustryTemplateJsonParseExample example = new MarketingIndustryTemplateJsonParseExample();
            example.createCriteria().andInterfaceTemplateIdEqualTo(id);
            List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList = marketingIndustryTemplateJsonParseMapper.selectByExample(example);

            if (marketingIndustryTemplate != null && marketingIndustryTemplateJsonParseList.size() > 0) {
                logger.warn("行业模板查询成功，行业模板id：{}", id);
                marketingIndustryTemplateDTO.setMarketingIndustryTemplate(marketingIndustryTemplate);
                marketingIndustryTemplateDTO.setMarketingIndustryTemplateJsonParseList(marketingIndustryTemplateJsonParseList);
                return new Result<MarketingIndustryTemplate>().success().setDate(marketingIndustryTemplateDTO);
            } else {
                logger.warn("未查询到该行业模板，行业模板id：{}", id);
                return new Result<MarketingIndustryTemplate>().success().setDate(null);
            }
        } catch (Exception e) {
            logger.warn("行业模板查询异常，行业模板id：{}", id);
            return new Result<MarketingIndustryTemplate>().failure().setMessage(e.getMessage()).setDate(null);
        }
    }

    public String paramValid(MarketingIndustryTemplate marketingIndustryTemplate) {
        StringBuilder stringBuilder = new StringBuilder();
        if (marketingIndustryTemplate.getTemplateName() == null || marketingIndustryTemplate.getTemplateName().isEmpty()) {
            stringBuilder.append("【templateName】");
        }
        if (marketingIndustryTemplate.getSystemType() == null || marketingIndustryTemplate.getSystemType().isEmpty()) {
            stringBuilder.append("【systemType】");
        }
        if (marketingIndustryTemplate.getDataType() == null) {
            stringBuilder.append("【dataType】");
        }
        if (marketingIndustryTemplate.getFirstDepartment() == null || marketingIndustryTemplate.getFirstDepartment().isEmpty()) {
            stringBuilder.append("【firstDepartment】");
        }
        return stringBuilder.toString();
    }
}
