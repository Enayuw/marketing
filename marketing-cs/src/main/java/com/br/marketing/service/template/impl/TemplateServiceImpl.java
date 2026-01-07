package com.br.marketing.service.template.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.template.MarketingIndustryTemplateDTO;
import com.br.marketing.entity.MarketingIndustryTemplate;
import com.br.marketing.entity.MarketingIndustryTemplateExample;
import com.br.marketing.entity.MarketingIndustryTemplateJsonParse;
import com.br.marketing.entity.MarketingIndustryTemplateJsonParseExample;
import com.br.marketing.mapper.MarketingIndustryTemplateJsonParseMapper;
import com.br.marketing.mapper.MarketingIndustryTemplateMapper;
import com.br.marketing.service.EntityOptService;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.template.TemplateService;
import com.github.pagehelper.page.PageMethod;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
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

    @Resource
    private EntityOptServiceImpl entityOptService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> addTemplate(MarketingIndustryTemplateDTO marketingIndustryTemplateDTO) {
        MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateDTO.getMarketingIndustryTemplate();
        List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList =
                marketingIndustryTemplateDTO.getMarketingIndustryTemplateJsonParseList();

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
            Long interfaceTemplateId = marketingIndustryTemplate.getId();

            entityOptService.writeOptLog(interfaceTemplateId, marketingIndustryTemplate, null);

            //批量插入json数据
            if (marketingIndustryTemplateJsonParseList != null && !marketingIndustryTemplateJsonParseList.isEmpty()) {
                marketingIndustryTemplateJsonParseList.forEach(item -> {
                    item.setId(null);
                    item.setInterfaceTemplateId(interfaceTemplateId);
                    item.setIsDel(Constants.DATA_VALID);
                    item.setCreateTime(new Date());
                    item.setUpdateTime(new Date());
                });
                marketingIndustryTemplateJsonParseMapper.batchInsert(marketingIndustryTemplateJsonParseList);
            }
            //添加操作日志
            MarketingIndustryTemplateJsonParseExample example = new MarketingIndustryTemplateJsonParseExample();
            example.createCriteria().andInterfaceTemplateIdEqualTo(interfaceTemplateId).andIsDelEqualTo(Constants.DATA_VALID);
            List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseListOld =
                    marketingIndustryTemplateJsonParseMapper.selectByExample(example);

            marketingIndustryTemplateJsonParseListOld.forEach(item -> {
                entityOptService.writeOptLog(item.getId(), item, null);
            });

            logger.warn("新增行业模板成功，行业模板名称：{}", marketingIndustryTemplate.getTemplateName());
            return new Result<>().success().setDate(Boolean.TRUE);
        } catch (Exception e) {
            logger.error("新增行业模板异常，行业模板名称：{}，error：{}", marketingIndustryTemplate.getTemplateName(), e.getMessage());
            return new Result<Boolean>().failure().setDate(Boolean.FALSE);
        }
    }

    @Override
    public Result<PageResultReturn<MarketingIndustryTemplate>> queryAllTemplate(Integer current, Integer pageSize
            , String templateName, String firstDepartment, String secondDepartment, String apiType
            , Integer systemType, Integer dataType) {
        PageMethod.startPage(current, pageSize);

        MarketingIndustryTemplateExample example = new MarketingIndustryTemplateExample();
        MarketingIndustryTemplateExample.Criteria criteria = example.createCriteria();
        criteria.andIsDelEqualTo(Constants.DATA_VALID);
        if (StringUtils.isNotBlank(templateName)) {
            criteria.andTemplateNameLike("%" + templateName + "%");
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
        if (systemType != null) {
            criteria.andSystemTypeEqualTo(systemType);
        }
        if (dataType != null) {
            criteria.andDataTypeEqualTo(dataType);
        }
        example.setOrderByClause("create_time desc");
        try {
            List<MarketingIndustryTemplate> marketingIndustryTemplateList = marketingIndustryTemplateMapper.selectByExample(example);
            if (!marketingIndustryTemplateList.isEmpty()) {
                logger.warn("查询行业模板成功，行业模板总条数：{}", marketingIndustryTemplateList.size());
                return new Result<PageResultReturn<MarketingIndustryTemplate>>().success().setDate(
                        PageResultReturn.setPageResult(marketingIndustryTemplateList, current, pageSize));
            } else {
                logger.warn("未查询到行业模板信息，查询条件：templateName={}，firstDepartment={}，secondDepartment={}，apiType={}",
                        templateName, firstDepartment, secondDepartment, apiType);
                return new Result<PageResultReturn<MarketingIndustryTemplate>>().failure().setMessage("未查询到行业模板信息").setDate(null);
            }
        } catch (Exception e) {
            logger.error("查询行业模板异常，查询条件：templateName={}，firstDepartment={}，secondDepartment={}，apiType={}，error：{}",
                    templateName, firstDepartment, secondDepartment, apiType, e.getMessage());
            return new Result<PageResultReturn<MarketingIndustryTemplate>>().failure().setDate(null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> editTemplate(MarketingIndustryTemplateDTO marketingIndustryTemplateDTO) {
        MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateDTO.getMarketingIndustryTemplate();
        List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList =
                marketingIndustryTemplateDTO.getMarketingIndustryTemplateJsonParseList();
        try {
            if (marketingIndustryTemplateJsonParseList != null && !marketingIndustryTemplateJsonParseList.isEmpty()) {
                Long templateId = marketingIndustryTemplate.getId();
                MarketingIndustryTemplate marketingIndustryTemplateOld =
                        marketingIndustryTemplateMapper.selectByPrimaryKey(templateId);
                //更新模板信息
                marketingIndustryTemplate.setUpdateTime(new Date());

                entityOptService.writeOptLog(templateId, marketingIndustryTemplate, marketingIndustryTemplateOld);

                marketingIndustryTemplateMapper.updateByPrimaryKey(marketingIndustryTemplate);

                //先全量逻辑删除jsonParse数据
                writeOptLog(templateId);
                marketingIndustryTemplateJsonParseMapper.deleteJsonParseList(marketingIndustryTemplate.getId());

                //jsonParse数据重新入库
                marketingIndustryTemplateJsonParseList.forEach(marketingIndustryTemplateJsonParse -> {
                    marketingIndustryTemplateJsonParse.setId(null);
                    marketingIndustryTemplateJsonParse.setIsDel(Constants.DATA_VALID);
                    marketingIndustryTemplateJsonParse.setCreateTime(new Date());
                    marketingIndustryTemplateJsonParse.setUpdateTime(new Date());
                });
                marketingIndustryTemplateJsonParseMapper.batchInsert(marketingIndustryTemplateJsonParseList);

                //添加操作日志
                MarketingIndustryTemplateJsonParseExample example = new MarketingIndustryTemplateJsonParseExample();
                example.createCriteria().andInterfaceTemplateIdEqualTo(templateId).andIsDelEqualTo(Constants.DATA_VALID);
                List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseListOld =
                        marketingIndustryTemplateJsonParseMapper.selectByExample(example);

                marketingIndustryTemplateJsonParseListOld.forEach(item -> {
                    entityOptService.writeOptLog(item.getId(), item, null);
                });

                logger.warn("修改行业模板成功，行业模板id：{}", marketingIndustryTemplate.getId());
                return new Result<Boolean>().success().setDate(Boolean.TRUE);
            } else {
                logger.warn("行业模板json数据不存在，更新失败");
                return new Result<>().failure().setDate(Boolean.FALSE);
            }
        } catch (DuplicateKeyException e) {
            String errorMsg = "修改行业模板失败，该字段已存在";
            logger.error(errorMsg, e);
            return new Result<>().failure().setDate(Boolean.FALSE);
        } catch (Exception e) {
            logger.error("修改行业模板异常，行业模板id：{}，error：{}", marketingIndustryTemplate.getId(), e.getMessage());
            return new Result<>().failure().setDate(Boolean.FALSE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteTemplate(Long id) {
        try {
            MarketingIndustryTemplate marketingIndustryTemplateOld = marketingIndustryTemplateMapper.selectByPrimaryKey(id);
            if (marketingIndustryTemplateOld != null) {
                MarketingIndustryTemplate marketingIndustryTemplateNew = marketingIndustryTemplateOld;
                marketingIndustryTemplateNew.setIsDel(Constants.DATA_DEL);
                marketingIndustryTemplateNew.setUpdateTime(new Date());
                marketingIndustryTemplateMapper.updateByPrimaryKey(marketingIndustryTemplateNew);
                entityOptService.writeOptLog(marketingIndustryTemplateOld.getId(), marketingIndustryTemplateNew, marketingIndustryTemplateOld);
            }

            writeOptLog(id);

            marketingIndustryTemplateJsonParseMapper.deleteJsonParseList(id);

            logger.warn("删除行业模板成功，行业模板id：{}", id);
            return new Result<>().success().setDate(Boolean.TRUE);
        } catch (Exception e) {
            logger.error("删除行业模板异常，行业模板id：{}，error：{}", id, e.getMessage());
            return new Result<>().failure().setDate(Boolean.FALSE);
        }
    }

    private void writeOptLog(Long id) {
        MarketingIndustryTemplateJsonParseExample example = new MarketingIndustryTemplateJsonParseExample();
        example.createCriteria().andInterfaceTemplateIdEqualTo(id).andIsDelEqualTo(Constants.DATA_VALID);
        List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseListOld =
                marketingIndustryTemplateJsonParseMapper.selectByExample(example);
        for (MarketingIndustryTemplateJsonParse oldValue : marketingIndustryTemplateJsonParseListOld) {
            MarketingIndustryTemplateJsonParse newValue = new MarketingIndustryTemplateJsonParse();
            newValue.setIsDel(Constants.DATA_DEL);
            entityOptService.writeOptLog(oldValue.getId(), newValue, oldValue);
        }
    }

    @Override
    public Result<MarketingIndustryTemplateDTO> queryTemplateById(Long id) {
        MarketingIndustryTemplateDTO marketingIndustryTemplateDTO = new MarketingIndustryTemplateDTO();
        try {
            MarketingIndustryTemplate marketingIndustryTemplate = marketingIndustryTemplateMapper.selectByPrimaryKey(id);

            MarketingIndustryTemplateJsonParseExample example = new MarketingIndustryTemplateJsonParseExample();
            example.createCriteria().andInterfaceTemplateIdEqualTo(id).andIsDelEqualTo(Constants.DATA_VALID);
            List<MarketingIndustryTemplateJsonParse> marketingIndustryTemplateJsonParseList =
                    marketingIndustryTemplateJsonParseMapper.selectByExample(example);

            if (marketingIndustryTemplate != null) {
                logger.warn("行业模板查询成功，行业模板id：{}", id);
                marketingIndustryTemplateDTO.setMarketingIndustryTemplate(marketingIndustryTemplate);
                marketingIndustryTemplateDTO.setMarketingIndustryTemplateJsonParseList(marketingIndustryTemplateJsonParseList);
                return new Result<MarketingIndustryTemplateDTO>().success().setDate(marketingIndustryTemplateDTO);
            } else {
                logger.warn("未查询到该行业模板，行业模板id：{}", id);
                return new Result<MarketingIndustryTemplateDTO>().failure().setDate(null);
            }
        } catch (Exception e) {
            logger.error("行业模板查询异常，行业模板id：{}，error：{}", id, e.getMessage());
            return new Result<MarketingIndustryTemplateDTO>().failure().setDate(null);
        }
    }

    public String paramValid(MarketingIndustryTemplate marketingIndustryTemplate) {
        StringBuilder stringBuilder = new StringBuilder();
        if (marketingIndustryTemplate.getTemplateName() == null || marketingIndustryTemplate.getTemplateName().isEmpty()) {
            stringBuilder.append("【templateName】");
        }
        if (marketingIndustryTemplate.getSystemType() == null) {
            stringBuilder.append("【systemType】");
        }
        if (marketingIndustryTemplate.getDataType() == null) {
            stringBuilder.append("【dataType】");
        }
        if (marketingIndustryTemplate.getFirstDepartment() == null || marketingIndustryTemplate.getFirstDepartment().isEmpty()) {
            stringBuilder.append("【firstDepartment】");
        }
        if (marketingIndustryTemplate.getSecondDepartment() == null || marketingIndustryTemplate.getSecondDepartment().isEmpty()) {
            stringBuilder.append("【secondDepartment】");
        }
        if (marketingIndustryTemplate.getApiType() == null || marketingIndustryTemplate.getApiType().isEmpty()) {
            stringBuilder.append("【apiType】");
        }
        return stringBuilder.toString();
    }
}
