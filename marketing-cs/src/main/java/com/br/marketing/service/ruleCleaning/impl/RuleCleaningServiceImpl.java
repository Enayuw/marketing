package com.br.marketing.service.ruleCleaning.impl;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;
import com.br.marketing.entity.MarketingDataCleanGeneralConfigExample;
import com.br.marketing.mapper.MarketingDataCleanGeneralRuleConfigMapper;
import com.br.marketing.mapper.MarketingJsonNodeParseMapper;
import com.br.marketing.mapper.rulecleaning.MarketingCustomerOriginalDataMapper;
import com.br.marketing.mapper.rulecleaning.MarketingDataCleanGeneralConfigMapper;
import com.br.marketing.service.ruleCleaning.RuleCleaningService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 规则数据清洗接口实现
 * @author guangxiu.li
 * @date 2025/5/6
 * @description
 */
@Service
@Slf4j
public class RuleCleaningServiceImpl implements RuleCleaningService {

    @Resource
    private MarketingDataCleanGeneralConfigMapper cleanGeneralConfigMapper;

    @Resource
    private MarketingCustomerOriginalDataMapper customerOriginalDataMapper;

    @Resource
    private MarketingDataCleanGeneralRuleConfigMapper cleanGeneralRuleConfigMapper;

    @Resource
    private MarketingJsonNodeParseMapper jsonNodeParseMapper;

    /**
     * 规则列表查询
     * @param current 当前页
     * @param size 每页条数
     * @param apiCode API编码
     * @param accountType 账号类型
     * @param acceptType 接口类型
     * @return 分页查询结果
     */
    @Override
    public PageResultReturn getRuleList(int current, int size, String apiCode, String accountType, Integer acceptType) {
        // 设置分页
        PageHelper.startPage(current, size);
        
        // 构建查询条件
        MarketingDataCleanGeneralConfig queryParam = new MarketingDataCleanGeneralConfig();
        
        // 设置查询条件
        if (StringUtils.isNotBlank(apiCode)) {
            queryParam.setApiCode(apiCode);
        }
        
        if (StringUtils.isNotBlank(accountType)) {
            queryParam.setAccountType(accountType);
        }
        
        if (acceptType != null) {
            queryParam.setAcceptType(acceptType);
        }

        // 执行查询
        List<MarketingDataCleanGeneralConfig> ruleList = cleanGeneralConfigMapper.selectRuleList(queryParam);
        
        // 获取总记录数
        long total = cleanGeneralConfigMapper.countRuleList(queryParam);
        
        // 返回分页结果
        return PageResultReturn.setPageResult(ruleList, current, size, total);
    }
    
    /**
     * 保存或更新规则
     * @param config 规则配置信息
     * @return 操作结果
     */
    @Override
    public boolean saveOrUpdateRule(MarketingDataCleanGeneralConfig config) {
        try {
            // 设置默认参数
            config.setIsDel(1);
            
            Date now = new Date();
            
            // 判断是新增还是修改
            if (config.getId() == null) {
                // 新增
                config.setCreateTime(now);
                config.setUpdateTime(now);
                return cleanGeneralConfigMapper.insertSelective(config) > 0;
            } else {
                // 修改
                config.setUpdateTime(now);
                return cleanGeneralConfigMapper.updateByPrimaryKeySelective(config) > 0;
            }
        } catch (Exception e) {
            log.error("保存或更新规则失败", e);
            return false;
        }
    }
    



}


