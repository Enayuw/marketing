package com.br.marketing.service.thirdpartner;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.ThirdPartnerUploadDataClean;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.ThirdPartnerUploadDataCleanMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.service.thirdpartner.dto.ThirdPartnerDataDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description 三方数据处理实现类
 * @Author hong.chen
 * @CreateTime 2024/11/28
 */
@Service
@Slf4j
public class ThirdPartnerDataServiceImpl implements ThirdPartnerDataService {
    @Autowired
    DataCleaningAutoService cleaningAutoService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Autowired
    MarketingCleanDataTaskMapper marketingCleanDataTaskMapper;
    @Resource
    ThirdPartnerUploadDataCleanMapper uploadDataCleanMapper;

    @Override
    public Result saveData(List<ThirdPartnerDataDTO> dataList) {
        try {
            HashMap<String, String> mappingConfig = marketingCommonConfig.getThirdPartnerApiCodeMappingConfig();
            // 根据源apiCode对数据分组
            Map<String, List<ThirdPartnerUploadDataClean>> map =
                    dataList.stream().map(t -> {
                        ThirdPartnerUploadDataClean dataClean = new ThirdPartnerUploadDataClean();
                        BeanUtils.copyProperties(t, dataClean);
                        return dataClean;
                    }).collect(Collectors.groupingBy(ThirdPartnerUploadDataClean::getApiCode));
            map.forEach((orgApiCode, value) -> {
                String apiCode = mappingConfig.get(orgApiCode);
                Long taskId = cleaningAutoService.saveCleanTask(apiCode, 0, "三方数据_上传清洗规则勿动");
                value.forEach(data -> {
                    data.setApiCode(apiCode);
                    data.setOrgApiCode(orgApiCode);
                    data.setValidStartDate(data.getValidStartDate().substring(1, 10));
                    data.setValidEndDate(data.getValidEndDate().substring(1, 10));
                    data.setTaskId(taskId);
                });

                uploadDataCleanMapper.batchSaveByTaskId(value);
                // 更新数据清洗任务表状态为待清洗
                updateTaskCleanStatusById(taskId);
            });
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), JSON.toJSONString(dataList),
                    "外呼推送三方上传数据接口，入库异常"), e);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage("发生内部错误");
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }

    private void updateTaskCleanStatusById(Long taskId) {
        MarketingCleanDataTask cleanDataTask = new MarketingCleanDataTask();
        cleanDataTask.setId(taskId);
        cleanDataTask.setCleanStatus(0);
        marketingCleanDataTaskMapper.updateByPrimaryKeySelective(cleanDataTask);
    }
}
