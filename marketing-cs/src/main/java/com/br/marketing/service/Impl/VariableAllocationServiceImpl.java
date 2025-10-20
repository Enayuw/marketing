package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.dto.VariableAllocationDTO;
import com.br.marketing.entity.VariableAllocation;
import com.br.marketing.mapper.VariableAllocationMapper;
import com.br.marketing.service.Impl.xc.XcExceptionDataRetryService;
import com.br.marketing.service.VariableAllocationService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.VariableAllocationVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private ObjectMapper objectMapper;

    @Resource
    private VariableAllocationMapper variableAllocationMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private EntityOptServiceImpl entityOptService;

    @Resource
    private XcExceptionDataRetryService xcExceptionDataRetryService;

    @Resource
    private RedisChgService redisChgService;

    final static String TYPE = "xiechengdingzhi";

    final static String XIECHENG_TYPE = "携程定制";

    @Override
    public VariableAllocationVO getVariableList(VariableAllocationDTO dto) {
        String apiCode = marketingCommonConfig.getXieChengDingZhiApiCode();
        String allocationType = Optional.ofNullable(dto.getAllocationType()).orElse(XIECHENG_TYPE);
        try {
            String requestTime = StringUtils.isBlank(dto.getRequestTime()) ?  LocalDate.now().toString() : dto.getRequestTime();
            VariableAllocation variableList = variableAllocationMapper.getVariableList(apiCode, allocationType);
            VariableAllocationVO allocationVO = new VariableAllocationVO();
            if (ObjectUtil.isNotEmpty(variableList)) {
                String allocationValue = variableList.getAllocationValue();
                JSONObject jsonObject = JSON.parseObject(allocationValue);
                Integer normalQuantity = jsonObject.getInteger("trueDataThresholdSize");
                Integer abnormalQuantity = jsonObject.getInteger("retryThresholdSize");
                VariableAllocationVO vo = variableAllocationMapper.getVariableAllocationVOtiflash_(requestTime);
                Integer releaseTimeNum = vo.getReleaseTimeNum();
                Integer falseNum = Optional.ofNullable(normalQuantity).orElse(0) -
                        Optional.ofNullable(releaseTimeNum).orElse(0);
                allocationVO.setId(variableList.getId().longValue());
                allocationVO.setApiCode(variableList.getApiCode());
                allocationVO.setAllocationType(variableList.getAllocationType());
                allocationVO.setNormalQuantity(normalQuantity);
                allocationVO.setAbnormalQuantity(abnormalQuantity);
                allocationVO.setReleaseTimeNum(releaseTimeNum);
                allocationVO.setFalseNum(falseNum);
                allocationVO.setRequestTime(vo.getRequestTime());
                allocationVO.setRequestEndTime(vo.getRequestEndTime());
                allocationVO.setAllocationValueMap(JSON.parseObject(variableList.getAllocationValue(), new TypeReference<Map<String, Object>>(){}));
            }
            return allocationVO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public JSONObject getAllocationValue(String apiCode, String allocationType) {
        String suffix = StringUtils.equals(allocationType, XIECHENG_TYPE) ? XIECHENG_TYPE : allocationType;
        String key = RedisKeyConstant.prefix.concat(":").concat(apiCode).concat(":").concat(suffix);
        VariableAllocation variable;
        try {
            String allocationValue = redisChgService.get(key);
            if (StringUtil.isNotEmpty(allocationValue)) {
                return JSON.parseObject(allocationValue);
            }
            variable = variableAllocationMapper.getVariable(apiCode, allocationType);
            if(Objects.isNull(variable)) {
                return null;
            }
            redisChgService.setex(key, variable.getAllocationValue(), 3600 * 12);
        } catch (Exception e) {
            log.error("携程挡板获取定制配置异常{}", e);
            variable = variableAllocationMapper.getVariable(apiCode, allocationType);
        }
        return JSON.parseObject(Optional.ofNullable(variable).orElse(new VariableAllocation()).getAllocationValue());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> updateVariableList(String params) {
        JSONObject param = JSONObject.parseObject(params);
        Long id = param.getLong("id");

        VariableAllocation originData = variableAllocationMapper.selectByPrimaryKey(id.intValue());

        VariableAllocationVO allocationVO = JSON.parseObject(params, VariableAllocationVO.class);
        String allocationValue = JSON.toJSONString(allocationVO.getAllocationValueMap());
        String origAllocationValue = originData.getAllocationValue();

        if(StringUtils.equals(originData.getAllocationType(), XIECHENG_TYPE)) {
            Integer normalQuantity = param.getInteger("normalQuantity");
            Integer abnormalQuantity = param.getInteger("abnormalQuantity");
            JSONObject originJson = JSON.parseObject(originData.getAllocationValue());
            int originTrueDataThresholdSize = originJson.getInteger("trueDataThresholdSize");
            int originRetryThresholdSize = originJson.getInteger("retryThresholdSize");
            if (normalQuantity == originTrueDataThresholdSize && abnormalQuantity == originRetryThresholdSize) {
                return new ApiResult<Boolean>().success(true, "修改前后数据一致");
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("trueDataThresholdSize", normalQuantity);
            jsonObject.put("retryThresholdSize", abnormalQuantity);
            allocationVO.setAllocationValue(jsonObject.toString());
        } else {
            if (StringUtils.equals(allocationValue, origAllocationValue)) {
                return new ApiResult<Boolean>().success(true, "修改前后数据一致");
            }
            allocationVO.setAllocationValue(allocationValue);
        }
        int updateCounts = variableAllocationMapper.updateByPrimaryMutchKeySelective(allocationVO);
        VariableAllocation newData = variableAllocationMapper.selectByPrimaryKey(id.intValue());
        if (updateCounts > 0 ){
            entityOptService.writeOptLog(id, newData, originData);
            // 将数据保存到 Redis
            String suffix = StringUtils.equals(newData.getAllocationType(), XIECHENG_TYPE) ? XIECHENG_TYPE : newData.getAllocationType();
            String key = RedisKeyConstant.prefix.concat(":").concat(originData.getApiCode()).concat(":").concat(suffix);
            try {
                redisChgService.del(key);
                redisChgService.setex(key, newData.getAllocationValue(), 5*60);
                return new ApiResult<Boolean>().success(true);
            } catch (Exception e) {
                log.error("获取携程定制配置接口更新redis异常{}", e);
                return new ApiResult<Boolean>().fail(false,"更新携程定制配置redis异常");
            }
        }
        return new ApiResult<Boolean>().fail(false, "更新配置失败");
    }


    @Override
    public VariableAllocationVO getVariableAllocation(){
        VariableAllocationVO allocationVO = new VariableAllocationVO();
        String apiCode = marketingCommonConfig.getXieChengDingZhiApiCode();
        int normalQuantity, abnormalQuantity;
        // 读取 Redis缓存中的数据
        String key = RedisKeyConstant.prefix.concat(":").concat(apiCode).concat(":").concat(TYPE);
        String allocationValue;
        try {
            allocationValue = redisChgService.get(key);
            if (StringUtil.isNotEmpty(allocationValue)){
                JSONObject jsonObject = JSON.parseObject(allocationValue);
                normalQuantity = jsonObject.getInteger("trueDataThresholdSize");
                abnormalQuantity = jsonObject.getInteger("retryThresholdSize");
                allocationVO.setNormalQuantity(normalQuantity);
                allocationVO.setAbnormalQuantity(abnormalQuantity);
                return allocationVO;
            }
            VariableAllocationVO vo = getVariableAllocationVO(allocationVO, apiCode);
            if (ObjectUtil.isNotEmpty(vo)){
                redisChgService.del(key);
                redisChgService.setex(key,vo.getAllocationValue(),5*60);
                return vo;
            }
        } catch (Exception e) {
            log.error("获取携程定制配置redis异常{}", e);
            VariableAllocationVO vo = getVariableAllocationVO(allocationVO, apiCode);
            if (vo != null){
                return vo;
            }
        }
        //数值为0报警
        String msg = "获取撞得总量级和异常报警量级为空";
        xcExceptionDataRetryService.sendDingDingAlert("获取携程定制配置异常！", msg);
        return allocationVO;
    }

    private VariableAllocationVO getVariableAllocationVO(VariableAllocationVO allocationVO, String apiCode) {
        VariableAllocation variable = variableAllocationMapper.getVariable(apiCode, XIECHENG_TYPE);
        if (ObjectUtil.isNotEmpty(variable)) {
            String value = variable.getAllocationValue();
            JSONObject json = JSON.parseObject(value);
            Integer dbTrueNum = json.getInteger("trueDataThresholdSize");
            Integer dbFalseNum = json.getInteger("retryThresholdSize");
            allocationVO.setNormalQuantity(dbTrueNum);
            allocationVO.setAbnormalQuantity(dbFalseNum);
            return allocationVO;
        }
        return null;
    }
}
