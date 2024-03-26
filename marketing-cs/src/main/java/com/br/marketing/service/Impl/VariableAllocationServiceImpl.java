package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.VariableAllocationDTO;
import com.br.marketing.entity.VariableAllocation;
import com.br.marketing.mapper.VariableAllocationMapper;
import com.br.marketing.service.VariableAllocationService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.VariableAllocationVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * sftp账号配置业务逻辑实现
 *
 * @author guangxiu.li
 * @dateTime 2024/03/21 13:12
 */
@Service
@Slf4j
public class VariableAllocationServiceImpl implements VariableAllocationService {


    @Resource
    private VariableAllocationMapper variableAllocationMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Resource
    private RedisChgService redisChgService;


    @Override
    public VariableAllocationVO getVariableList(VariableAllocationDTO dto) {
        String apiCode = dto.getApiCode();
        String allocationType = dto.getAllocationType();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime currentTime = LocalDateTime.now();
            String nowTime = currentTime.format(formatter);
            LocalDateTime time = LocalDateTime.parse(dto.getRequestTime(), formatter);
            String requestTime = time.format(formatter);
            String date = "".equals(dto.getRequestTime()) ? nowTime : requestTime;

            VariableAllocation variableList = variableAllocationMapper.getVariableList(apiCode, allocationType);
            VariableAllocationVO allocationVO = new VariableAllocationVO();
            if (ObjectUtil.isNotEmpty(variableList)) {
                String allocationValue = variableList.getAllocationValue();
                JSONObject jsonObject = JSON.parseObject(allocationValue);
                int normalQuantity =  jsonObject.getInteger("trueDataThresholdSize");
                int abnormalQuantity = jsonObject.getInteger("retryThresholdSize");
                int releaseTimeNum = variableAllocationMapper.getVariableAllocationVO(date);
                int falseNum = normalQuantity - releaseTimeNum;
                allocationVO.setId(variableList.getId().longValue());
                allocationVO.setApiCode(variableList.getApiCode());
                allocationVO.setAllocationType(variableList.getAllocationType());
                allocationVO.setNormalQuantity(normalQuantity);
                allocationVO.setAbnormalQuantity(abnormalQuantity);
                allocationVO.setReleaseTimeNum(releaseTimeNum);
                allocationVO.setFalseNum(falseNum);
            }
            return allocationVO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> updateVariableList(Long id, int normalQuantity, int abnormalQuantity) {
        //原数据记录
        VariableAllocation data = variableAllocationMapper.selectByPrimaryKey(id.intValue());
        //更新记录
        VariableAllocationVO allocationVO = new VariableAllocationVO();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trueDataThresholdSize", normalQuantity);
        jsonObject.put("retryThresholdSize", abnormalQuantity);
        allocationVO.setId(id);
        allocationVO.setAllocationValue(jsonObject.toString());
        allocationVO.setNormalQuantity(normalQuantity);
        allocationVO.setAbnormalQuantity(abnormalQuantity);
        allocationVO.setRequestTime(LocalDate.now().toString());
        int i = variableAllocationMapper.updateByPrimaryMutchKeySelective(allocationVO);
        VariableAllocation newData = variableAllocationMapper.selectByPrimaryKey(id.intValue());
        if (i>=0){
            entityOptService.writeOptLog(id, newData, data);
            // 将数据保存到 Redis
            String key = RedisKeyConstant.prefix.concat(":").concat(data.getApiCode()).concat(":").concat(data.getAllocationType());
            redisChgService.set(key, newData.getAllocationValue());
        }
        return new ApiResult<Boolean>().success(true);
    }


    @Override
    public VariableAllocationVO getVariableAllocation(){
        VariableAllocationVO allocationVO = new VariableAllocationVO();
        String apiCode = marketingCommonConfig.getXieChengDingZhiApiCode();
        String allocationType = "携程定制";
        int dbTrueNum = 5000000;
        int dbFalseNum = 100000;
        int normalQuantity = 5000000;
        int abnormalQuantity = 100000;
        String key = RedisKeyConstant.prefix.concat(":").concat(apiCode).concat(":").concat(allocationType);
        String allocationValue = redisChgService.get(key);
        if (StringUtil.isNotEmpty(key) && StringUtil.isNotEmpty(allocationValue)){
            JSONObject jsonObject = JSON.parseObject(allocationValue);
            normalQuantity = jsonObject.getInteger("trueDataThresholdSize");
            abnormalQuantity = jsonObject.getInteger("retryThresholdSize");
            allocationVO.setNormalQuantity(normalQuantity);
            allocationVO.setAbnormalQuantity(abnormalQuantity);
            return allocationVO;
        }
        VariableAllocation variable = variableAllocationMapper.getVariable(apiCode, allocationType);
        if (ObjectUtil.isNotEmpty(variable)){
            String value = variable.getAllocationValue();
            JSONObject json = JSON.parseObject(value);
            dbTrueNum = json.getInteger("trueDataThresholdSize");
            dbFalseNum = json.getInteger("retryThresholdSize");
        }
        allocationVO.setNormalQuantity(dbTrueNum);
        allocationVO.setAbnormalQuantity(dbFalseNum);
        return allocationVO;

    }

}
