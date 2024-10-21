package com.br.marketing.service.rulecenter.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.rulecenter.*;
import com.br.marketing.vo.MarketingTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Service
@Slf4j
public class RuleRefreshConfigServiceImpl implements IRuleRefreshConfigService {


    @Autowired
    MarketingTaskService marketingTaskService;

    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Override
    public void buildRefreshConfig() {
        Map<String, String> map = new HashMap<>();
        map.put("3710128","推送模板_3710128");
        map.put("3710148","推送模板_3710148");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            PageResultReturn list = marketingTaskService.list(1, 10, key,
                    null, null, null, null, null, null, null);

            List<MarketingTaskVO> fastTaskRuleListVOS = list.getRecords();
            if(CollectionUtil.isEmpty(fastTaskRuleListVOS)){
                return;
            }
            MarketingTaskVO marketingTaskVO = fastTaskRuleListVOS.get(0);

            Long hisFileId = marketingTaskVO.getHisFileId();

            //根据value查询模板
            ScoreSearchConditionExample scoreSearchConditionExample = new ScoreSearchConditionExample();
            scoreSearchConditionExample.createCriteria().andNameEqualTo(value).andIsDelEqualTo(0).andStatusEqualTo(1);
            List<ScoreSearchCondition> scoreSearchConditions = scoreSearchConditionMapper.selectByExample(scoreSearchConditionExample);
            if(CollectionUtil.isEmpty(scoreSearchConditions)){
                return;
            }
            //若不为空，则更新改模板的source_condition字段为hisFileId
            ScoreSearchCondition scoreSearchCondition1 = new ScoreSearchCondition();
            scoreSearchCondition1.setSourceCondition(String.valueOf(hisFileId));
            ScoreSearchConditionExample scoreSearchConditionExample1 = new ScoreSearchConditionExample();
            scoreSearchConditionExample1.createCriteria().andNameEqualTo(value).andIsDelEqualTo(0).andStatusEqualTo(1);
            int updateCount = scoreSearchConditionMapper.updateByExampleSelective(scoreSearchCondition1, scoreSearchConditionExample1);
            if(updateCount == 0){
                log.warn("自动刷新模板失败:{}" ,key);
                return;
            }

        }


    }
}
