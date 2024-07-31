package com.br.marketing.service.Impl;

import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreOptLog;
import com.br.marketing.entity.ScoreOptLogExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.ScoreOptLogMapper;
import com.br.marketing.service.ScoreOptLogService;
import com.br.marketing.vo.ScoreRuleVO;
import com.github.pagehelper.PageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
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

//    @Resource
//    private VariableDicMapper variableDicMapper;

    @Override
    public PageResultReturn findListPage(int page, int pageSize, Long rid, String cid, String apiCode) {
        PageHelper.startPage(page, pageSize);
        ScoreOptLogExample example = new ScoreOptLogExample();
        example.createCriteria().andScoreRuleIdEqualTo(String.valueOf(rid)).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc");
        List<ScoreOptLog> scoreOptLogs = scoreOptLogMapper.selectByExample(example);
        return PageResultReturn.setPageResult(scoreOptLogs, page, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int save(ScoreRuleVO scoreRuleVO, int status, MarketingUserDetail userDetail,String conditionInfo) {
        ScoreOptLog scoreOptLog = new ScoreOptLog();
        scoreOptLog.setApicode(scoreRuleVO.getApiCode());
        scoreOptLog.setCid(scoreRuleVO.getCid());
        scoreOptLog.setScoreRuleId(String.valueOf(scoreRuleVO.getId()));
        scoreOptLog.setRuleName(scoreRuleVO.getRuleName());
        scoreOptLog.setCreateTime(new Date());
        scoreOptLog.setOptUserId(String.valueOf(userDetail.getId()));
        scoreOptLog.setOptUserName(userDetail.getUserName());
        scoreOptLog.setConditionShowInfo(conditionInfo);
//        spliceConditionInfoJsonLog(scoreOptLog, scoreRuleVO.getVdSet());
        String jsonStr = "{\"".concat("strategyId\":\"").concat(scoreRuleVO.getStrategyId())
                .concat("\",\"").concat("products\":").concat(scoreRuleVO.getStrategyProductShow()).concat("}");
        scoreOptLog.setStrategyProductShow(jsonStr);
        scoreOptLog.setStartTime(scoreRuleVO.getStartTime());
        scoreOptLog.setStatus(status);
        scoreOptLog.setIsDel(1);
        scoreOptLog.setUpdateTime(scoreOptLog.getCreateTime());
        int insert = scoreOptLogMapper.insert(scoreOptLog);
        if (insert < 1) {
            throw new BusinessException("很遗憾小主，变更记录添加失败");
        }
        return insert;
    }


    /**
     * 场景json结构拼接
     */
//    private void spliceConditionInfoJsonLog(ScoreOptLog scoreOptLog, Set<VariableDicSelectVO> vdSet) {
//        List<String> fieldNames = vdSet.stream().map(VariableDicSelectVO::getFieldName).collect(Collectors.toList());
//        List<String> fieldValues = vdSet.stream().map(VariableDicSelectVO::getFieldValue).collect(Collectors.toList());
//        VariableDicExample example = new VariableDicExample();
//        example.createCriteria()
//                .andCidEqualTo(scoreOptLog.getCid())
//                .andApiCodeEqualTo(scoreOptLog.getApicode())
//                .andFieldNameIn(fieldNames).andFieldValueIn(fieldValues);
//        List<VariableDic> variableDics = variableDicMapper.selectByExample(example);
//        StringBuilder ci = new StringBuilder("{\"logicalOperation\":\"or\",\"operationFactor\":[");
//        final char ch = ',';
//        variableDics.forEach(vd -> ci.append("{\"fieldName\":\"")
//                .append(vd.getFieldName())
//                .append("\",\"fieldValue\":\"")
//                .append(vd.getFieldValue())
//                .append("\",\"fieldDesc\":\"")
//                .append(vd.getFieldDesc())
//                .append("\",\"operation\":\"=\"}").append(ch));
//        // 得到最后一个字符的索引地址
//        int index = ci.length() - 1;
//        // 取到最后一个字符
//        char c = ci.charAt(index);
//        if (c == ch) {
//            // 删除最后一个字符
//            ci.deleteCharAt(index);
//        }
//        scoreOptLog.setConditionShowInfo(ci.append("]}").toString());
//    }


}
