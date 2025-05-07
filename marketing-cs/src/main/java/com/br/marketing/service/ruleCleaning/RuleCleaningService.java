package com.br.marketing.service.ruleCleaning;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataCleanGeneralConfig;

/**
 * 规则数据清洗接口
 * @author guangxiu.li
 * @date 2025/5/6
 */
public interface RuleCleaningService {

    /**
     * 规则列表查询
     * @param current 当前页
     * @param size 每页条数
     * @param apiCode API编码
     * @param accountType 账号类型
     * @param acceptType 接口类型
     * @return 分页查询结果
     */
    PageResultReturn getRuleList(int current, int size, String apiCode, String accountType, Integer acceptType);

    /**
     * 保存或更新规则
     * @param config 规则配置信息
     * @return 操作结果
     */
    boolean saveOrUpdateRule(MarketingDataCleanGeneralConfig config);
    

}
