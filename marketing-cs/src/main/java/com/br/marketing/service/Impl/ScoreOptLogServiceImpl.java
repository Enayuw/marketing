package com.br.marketing.service.Impl;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreOptLog;
import com.br.marketing.entity.ScoreOptLogExample;
import com.br.marketing.mapper.ScoreOptLogMapper;
import com.br.marketing.service.ScoreOptLogService;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 跑分配置记录
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/6 13:30
 */
@Service
public class ScoreOptLogServiceImpl implements ScoreOptLogService {

    @Resource
    private ScoreOptLogMapper scoreOptLogMapper;


    @Override
    public PageResultReturn findListPage(int page, int pageSize, Long rid) {
        PageHelper.startPage(page, pageSize);
        ScoreOptLogExample example = new ScoreOptLogExample();
        example.createCriteria().andScoreRuleIdEqualTo(String.valueOf(rid)).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc");
        List<ScoreOptLog> scoreOptLogs = scoreOptLogMapper.selectByExample(example);
        return PageResultReturn.setPageResult(scoreOptLogs, page);
    }
}
