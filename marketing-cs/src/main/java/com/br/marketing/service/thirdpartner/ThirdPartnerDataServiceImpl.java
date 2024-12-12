package com.br.marketing.service.thirdpartner;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.entity.ThirdPartnerUploadDataClean;
import com.br.marketing.entity.ThirdPartnerUploadDataCleanFront;
import com.br.marketing.mapper.MarketingCleanDataTaskMapper;
import com.br.marketing.mapper.ThirdPartnerUploadDataCleanFrontMapper;
import com.br.marketing.mapper.ThirdPartnerUploadDataCleanMapper;
import com.br.marketing.service.DataCleaningAutoService;
import com.br.marketing.service.thirdpartner.dto.ThirdPartnerDataDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @Resource
    ThirdPartnerUploadDataCleanFrontMapper uploadDataCleanFrontMapper;

    @Override
    public Result saveData(List<ThirdPartnerDataDTO> dataList, String accessNumber, String originalData) {
        try {
            ThirdPartnerUploadDataCleanFront uploadDataCleanFront = new ThirdPartnerUploadDataCleanFront();
            uploadDataCleanFront.setAccessNumber(accessNumber);
            uploadDataCleanFront.setOriginalData(originalData);
            try {
                uploadDataCleanFrontMapper.insertSelective(uploadDataCleanFront);
            } catch (DuplicateKeyException e) {
                log.warn("外呼推送三方上传数据接口，流水号{}重复，数据不入库", accessNumber);
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("流水号重复");
            }

            HashMap<String, String> mappingConfig = marketingCommonConfig.getThirdPartnerApiCodeMappingConfig();
            // 根据源apiCode对数据分组
            Map<String, List<ThirdPartnerDataDTO>> map =
                    dataList.stream().collect(Collectors.groupingBy(ThirdPartnerDataDTO::getApiCode));
            map.forEach((orgApiCode, value) -> {
                String apiCode = mappingConfig.get(orgApiCode);
                if (Objects.isNull(apiCode)) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), "入参apiCode：" + orgApiCode,
                            "外呼推送三方上传数据接口，入参apiCode没有映射关系。数据不落库，需要关注"));
                    return;
                }

                Long taskId = cleaningAutoService.saveCleanTask(apiCode, 0, "三方数据_上传清洗规则勿动");
                List<ThirdPartnerUploadDataClean> list = new ArrayList<>();
                value.forEach(data -> {
                    String validStartDate = data.getValidStartDate();
                    String validEndDate = data.getValidEndDate();
                    if (Objects.isNull(validStartDate) || Objects.isNull(validEndDate)) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), JSON.toJSONString(data),
                                "外呼推送三方上传数据接口，参数异常"));
                        return;
                    }

                    ThirdPartnerUploadDataClean dataClean = new ThirdPartnerUploadDataClean();
                    BeanUtils.copyProperties(data, dataClean);

                    dataClean.setAccessNumber(accessNumber);
                    dataClean.setApiCode(apiCode);
                    dataClean.setOrgApiCode(orgApiCode);
                    dataClean.setValidStartDate(validStartDate.substring(0, 10));
                    dataClean.setValidEndDate(validEndDate.substring(0, 10));
                    dataClean.setTaskId(taskId);
                    list.add(dataClean);
                });

                uploadDataCleanMapper.batchSaveByTaskId(list);
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
