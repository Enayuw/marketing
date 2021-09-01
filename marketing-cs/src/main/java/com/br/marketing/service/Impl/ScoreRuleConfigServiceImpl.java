package com.br.marketing.service.Impl;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 跑分配置业务实现
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/31 14:36
 */
@Service
@Slf4j
public class ScoreRuleConfigServiceImpl implements ScoreRuleConfigService {

    @Resource
    private ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Override
    public PageResultReturn findListPage(int page, int pageSize, String search, Integer status, String cts, String cte, String uts, String ute) {
        PageHelper.startPage(page, pageSize);
        try {
            List<ScoreRuleConfigPageVO> list = scoreRuleConfigMapper.findList(search, status, cts, cte, uts, ute);
            return PageResultReturn.setPageResult(list, page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
