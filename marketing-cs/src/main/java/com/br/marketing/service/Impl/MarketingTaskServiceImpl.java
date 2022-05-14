package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.exception.auth.AppException;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.vo.FastTaskRuleDetailVO;
import com.br.marketing.vo.FastTaskRuleListVO;
import com.br.marketing.vo.MarketingTaskVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 跑人任务接口实现类
 * @Date 2022/5/10 11:57 AM
 * ------------------------------
 */
@Service
@Slf4j
public class MarketingTaskServiceImpl implements MarketingTaskService {

    @Resource
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    private RedisChgService redisChgService;

    @Resource
    private ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Override
    public PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd,
                                 String updateTimeStart, String updateTimeEnd, Integer taskStatus) {

        if (StringUtils.isNotEmpty(createTimeEnd)) {
            createTimeEnd = DateUtils.format(addDay(createTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(updateTimeEnd)) {
            updateTimeEnd = DateUtils.format(addDay(updateTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(search) && search.contains("_")) {
            search = search.replace("_", "\\_");
        }
        PageHelper.startPage(current, size);
        List<MarketingTaskVO> fastTaskRuleListVOS = marketingTaskMapper.selectList(search, status,
                createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd, taskStatus, null);

        return PageResultReturn.setPageResult(fastTaskRuleListVOS, current, size);
    }

    @Override
    public ApiResult<Boolean> editPriority(String id, Integer priority) {
        MarketingTask marketingTask = new MarketingTask();
        marketingTask.setPriority(priority);
        marketingTask.setId(Long.valueOf(id));
        Integer exist = marketingTaskMapper.selectByPriority(priority);
        if (exist > 0) {
            throw new AppException(CodeEnum.TASK_PRIORITY_EXIST);
        }
        marketingTaskMapper.updateByPrimaryKeySelective(marketingTask);
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public boolean updateStatusById(String id, Integer status) {
        try {
            MarketingTask marketingTask = new MarketingTask();
            marketingTask.setStatus(status);
            marketingTaskMapper.updateByPrimaryKeySelective(marketingTask);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public MarketingTaskVO getTask(String id) {
        List<MarketingTaskVO> marketingTaskVO = marketingTaskMapper.selectList(null, null, null, null, null, null, null, id);
        if (marketingTaskVO.size() > 0) {
            return marketingTaskVO.get(0);
        }
        return null;
    }

    @Override
    public Integer getTaskPercent(String hisFileId,String id) {
        String status = marketingTaskMapper.selectHisFileById(hisFileId);
        if("2".equals(status)){
            return 100;
        }else if("3".equals(status)){
            MarketingTask marketingTask = marketingTaskMapper.selectByPrimaryKey(Long.valueOf(id));
            Integer taskNumber = marketingTask.getTaskNumber();
            String s = redisChgService.get(RedisKeyConstant.taskScoreNum + ":" + hisFileId);
            if(s==null) s="0";
            return Integer.valueOf(s)/taskNumber;
        }
        return 0;
    }

    @Override
    public List<ScoreRuleConfig> getScoreRules(String apiCode) {
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.getScoreRules(apiCode);
        return list;
    }


    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

    @Override
    public void addTaskPercent(Long fileId,Long number) {
        String key = RedisKeyConstant.taskScoreNum.concat(":").concat(fileId.toString());
        redisChgService.incrBy(key,number);
    }
}
