package com.br.marketing.service.halo.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.enums.PushRuleStatusEnum;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.halo.HaloRuleCenterCallbackService;
import com.br.marketing.service.rulecenter.enums.RuleCenterPushTargetEnum;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName HaloRuleCenterCallbackServiceImpl
 * @Author hang.zhou
 * @Date 2025/9/17
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class HaloRuleCenterCallbackServiceImpl implements HaloRuleCenterCallbackService {

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Override
    public Result saveHaloCallbackTask(PushCustomerDTO dto) {
        if (dto.getBatchNumberList().size() > 50) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次最多选择50个");
        }
        if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
        }
        if (dto.getmPercentage() != null && dto.getmPercentage().compareTo(new BigDecimal(0)) <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
        }
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andIdIn(dto.getFileIdList());
        List<StraHisFile> files = straHisFileMapper.selectByExample(fileExample);

        Integer pushNum = dto.getmPrePlanNum();
        //region insert db
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(dto.getFileIdList());
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();

        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> showTitles = straHisFiles.stream().map(t -> t.getBatchNumber()).collect(Collectors.toList());
        customerInfoPushMain.setmApiCode(dto.getApiCode());
        customerInfoPushMain.setmRuleCondition(dto.getmRuleCondition());
        customerInfoPushMain.setmRuleConditionShow(dto.getmRuleConditionShow());
        customerInfoPushMain.setmScoreCondition(dto.getmScoreCondition());
        customerInfoPushMain.setmPercentage(dto.getmPercentage());
        customerInfoPushMain.setmPlanNum(dto.getmPlanNum());
        customerInfoPushMain.setmRealyNum(pushNum);
        Date date = new Date();
        customerInfoPushMain.setCreateTime(date);
        customerInfoPushMain.setUpdateTime(date);
        customerInfoPushMain.setmCusBatchNumberList(Joiner.on(",").join(showTitles));
        customerInfoPushMain.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
        customerInfoPushMain.setOptUserId(String.valueOf(dto.getUserDetail().getId()));
        customerInfoPushMain.setOptUserName(dto.getUserDetail().getRealName());
        customerInfoPushMain.setLabelName(dto.getLabelName());
        customerInfoPushMain.setPushTarget(RuleCenterPushTargetEnum.HALO_CALLBACK.getCode());
        customerInfoPushMain.setFilterType(3);
        customerInfoPushMainMapper.insertSelective(customerInfoPushMain);
        //数据集名称更新
        String batchName;
        if (StringUtils.isNotEmpty(dto.getBatchName())) {
            batchName = dto.getBatchName();
        } else {  //默认名称
            if (StringUtils.isNotEmpty(dto.getRuleModelName())) {
                batchName = LocalDate.now().toString().concat("-").concat(dto.getRuleModelName()).concat("-").concat(LocalTime.now().withNano(0)
                        .toString());
            } else {
                batchName = LocalDate.now().toString().concat("-").concat(customerInfoPushMain.getId().toString()).concat("-").
                        concat(LocalTime.now().withNano(0).toString());
            }
        }
        CustomerInfoPushMain updatePushMain = new CustomerInfoPushMain();
        updatePushMain.setId(customerInfoPushMain.getId());
        updatePushMain.setBatchName(batchName);
        customerInfoPushMainMapper.updateByPrimaryKeySelective(updatePushMain);
        files.forEach(t -> {
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(t.getBatchNumber());
            customerInfoPushBatch.setCreateTime(date);
            customerInfoPushBatch.setUpdateTime(date);
            customerInfoPushBatch.setmFileId(t.getId());
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
        });
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue()).setDate(customerInfoPushMain.getId().toString());
    }
}
