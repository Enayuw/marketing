package com.br.marketing.service.strategy.pushcustomer;

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
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 推送客户策略抽象基类
 * 提取公共的参数校验、对象组装、批次名称生成等逻辑
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
public abstract class AbstractPushCustomerStrategy implements IPushCustomerStrategy {

    @Resource
    protected CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Resource
    protected CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Resource
    protected StraHisFileMapper straHisFileMapper;

    /**
     * 校验通用参数（mPlanNum 和 mPercentage）
     *
     * @param dto 推送客户DTO
     * @return 校验结果
     */
    protected Result<String> validateCommonParams(PushCustomerDTO dto) {
        if (dto.getmPlanNum() != null && dto.getmPlanNum() <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("推送数量不能小于等于0");
        }
        if (dto.getmPercentage() != null && dto.getmPercentage().compareTo(BigDecimal.ZERO) <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("百分比不能小于等于0");
        }
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 校验跑分任务特有参数（批次号、fileIdList、批次数量）
     *
     * @param dto 推送客户DTO
     * @return 校验结果
     */
    protected Result<String> validateScoreParams(PushCustomerDTO dto) {
        if (dto.getBatchNumberList().isEmpty()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次号不能为空");
        }
        if (dto.getFileIdList().isEmpty()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("fileIdList不能为空");
        }
        if (dto.getBatchNumberList().size() > 50) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("批次最多选择50个");
        }
        return validateCommonParams(dto);
    }

    /**
     * 组装 CustomerInfoPushMain 基础字段
     *
     * @param dto     推送客户DTO
     * @param pushNum 实际推送数量
     * @return CustomerInfoPushMain 对象
     */
    protected CustomerInfoPushMain buildCustomerInfoPushMain(PushCustomerDTO dto, Integer pushNum) {
        CustomerInfoPushMain customerInfoPushMain = new CustomerInfoPushMain();
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
        customerInfoPushMain.setmStatus(PushRuleStatusEnum.TO_BE_RUNNING.getValue());
        customerInfoPushMain.setOptUserId(String.valueOf(dto.getUserDetail().getId()));
        customerInfoPushMain.setOptUserName(dto.getUserDetail().getRealName());
        customerInfoPushMain.setTagContent(dto.getmTagCondition());
        
        return customerInfoPushMain;
    }

    /**
     * 组装跑分任务的 CustomerInfoPushMain（包含批次号列表）
     *
     * @param dto     推送客户DTO
     * @param pushNum 实际推送数量
     * @return CustomerInfoPushMain 对象
     */
    protected CustomerInfoPushMain buildScoreCustomerInfoPushMain(PushCustomerDTO dto, Integer pushNum) {
        CustomerInfoPushMain customerInfoPushMain = buildCustomerInfoPushMain(dto, pushNum);
        
        // 查询文件获取批次号列表
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria().andIdIn(dto.getFileIdList());
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        List<String> showTitles = straHisFiles.stream().map(StraHisFile::getBatchNumber).collect(Collectors.toList());
        customerInfoPushMain.setmCusBatchNumberList(Joiner.on(",").join(showTitles));
        
        return customerInfoPushMain;
    }

    /**
     * 生成批次名称
     *
     * @param dto                  推送客户DTO
     * @param customerInfoPushMain 已插入的 CustomerInfoPushMain 对象（需要有id）
     * @return 生成的批次名称
     */
    protected String generateBatchName(PushCustomerDTO dto, CustomerInfoPushMain customerInfoPushMain) {
        String batchName;
        if (StringUtils.isNotEmpty(dto.getBatchName())) {
            batchName = dto.getBatchName();
        } else {  // 默认名称
            if (StringUtils.isNotEmpty(dto.getRuleModelName())) {
                batchName = LocalDate.now().toString().concat("-").concat(dto.getRuleModelName()).concat("-")
                        .concat(LocalTime.now().withNano(0).toString());
            } else {
                batchName = LocalDate.now().toString().concat("-").concat(customerInfoPushMain.getId().toString())
                        .concat("-").concat(LocalTime.now().withNano(0).toString());
            }
        }
        return batchName;
    }

    /**
     * 更新批次名称
     *
     * @param customerInfoPushMainId CustomerInfoPushMain 的 ID
     * @param batchName              批次名称
     */
    protected void updateBatchName(Long customerInfoPushMainId, String batchName) {
        CustomerInfoPushMain updatePushMain = new CustomerInfoPushMain();
        updatePushMain.setId(customerInfoPushMainId);
        updatePushMain.setBatchName(batchName);
        customerInfoPushMainMapper.updateByPrimaryKeySelective(updatePushMain);
    }

    /**
     * 批量插入 CustomerInfoPushBatch
     *
     * @param dto                  推送客户DTO
     * @param customerInfoPushMain 已插入的 CustomerInfoPushMain 对象
     * @param files                文件列表
     * @param createTime           创建时间
     */
    protected void insertCustomerInfoPushBatch(PushCustomerDTO dto, CustomerInfoPushMain customerInfoPushMain, 
                                               List<StraHisFile> files, Date createTime) {
        files.forEach(file -> {
            CustomerInfoPushBatch customerInfoPushBatch = new CustomerInfoPushBatch();
            customerInfoPushBatch.setmId(customerInfoPushMain.getId());
            customerInfoPushBatch.setmApiCode(dto.getApiCode());
            customerInfoPushBatch.setmBatchNumber(file.getBatchNumber());
            customerInfoPushBatch.setCreateTime(createTime);
            customerInfoPushBatch.setUpdateTime(createTime);
            customerInfoPushBatch.setmFileId(file.getId());
            customerInfoPushBatchMapper.insertSelective(customerInfoPushBatch);
        });
    }

    /**
     * 查询 StraHisFile 列表
     *
     * @param fileIdList 文件ID列表
     * @return StraHisFile 列表
     */
    protected List<StraHisFile> queryStraHisFiles(List<Long> fileIdList) {
        StraHisFileExample fileExample = new StraHisFileExample();
        fileExample.createCriteria().andIdIn(fileIdList);
        return straHisFileMapper.selectByExample(fileExample);
    }
}

