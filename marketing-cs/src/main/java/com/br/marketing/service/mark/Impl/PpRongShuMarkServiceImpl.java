package com.br.marketing.service.mark.Impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.service.mark.PpRonShuMarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Description pp榕树打标实现
 * @Author hong.chen
 * @Date 2025/2/19 18:16
 */
@Service
@Slf4j
public class PpRongShuMarkServiceImpl implements PpRonShuMarkService {
    @Autowired
    RedisChgService redisChgService;
    @Resource
    FlagDataMapper flagDataMapper;
    @Autowired
    DataCleaningAutoService cleaningAutoService;
    @Autowired
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;
    @Resource
    LocalFileMapper localFileMapper;

    @Override
    public Result<Boolean> createCleanTask(Long localId) {
        LocalFile localFile = localFileMapper.getByPrimaryKey(localId);
        String apiCode = localFile.getApiCode();
        // 创建任务id
        Long taskId = cleaningAutoService.saveCleanTask(apiCode, 0, "pp榕树打标_上传清洗规则勿动");

        // 更新打标表任务id
        while (true) {
            int count = 0;
            try {
                count = flagDataMapper.updateTaskIdByLocalId(localId);
            } catch (Exception e) {
                String subject = "pp榕树更新打标表taskId异常,localFIleId:" + localFile.getId();
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage()
                        , subject), e);
            }
            if (count == 0) {
                break;
            }
        }

        // 更新数据清洗任务表状态为待清洗
        updateTaskCleanStatusById(taskId);
        return new Result<Boolean>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private void updateTaskCleanStatusById(Long taskId) {
        MarketingCleanDataTask cleanDataTask = new MarketingCleanDataTask();
        cleanDataTask.setId(taskId);
        cleanDataTask.setCleanStatus(0);
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTask);
    }
}
