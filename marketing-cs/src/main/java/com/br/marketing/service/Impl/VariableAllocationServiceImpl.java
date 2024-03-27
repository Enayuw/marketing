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
import com.br.marketing.service.Impl.xc.XcExceptionDataRetryService;
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
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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

    @Autowired
    XcExceptionDataRetryService xcExceptionDataRetryService;

    @Resource
    private RedisChgService redisChgService;

    final static String TYPE = "xiechengdingzhi";
    final static String XIECHENG_TYPE = "携程定制";;

    @Override
    public VariableAllocationVO getVariableList(VariableAllocationDTO dto) {
        String apiCode = dto.getApiCode();
        String allocationType = dto.getAllocationType();
        try {
            LocalDate now = LocalDate.now();
            String requestTime = "".equals(dto.getRequestTime()) ? now.toString() : dto.getRequestTime();
            VariableAllocation variableList = variableAllocationMapper.getVariableList(apiCode, allocationType);
            VariableAllocationVO allocationVO = new VariableAllocationVO();
            if (ObjectUtil.isNotEmpty(variableList)) {
                String allocationValue = variableList.getAllocationValue();
                JSONObject jsonObject = JSON.parseObject(allocationValue);
                int normalQuantity =  jsonObject.getInteger("trueDataThresholdSize");
                int abnormalQuantity = jsonObject.getInteger("retryThresholdSize");
                VariableAllocationVO vo = variableAllocationMapper.getVariableAllocationVO(requestTime);
                int releaseTimeNum = vo.getReleaseTimeNum();
                int falseNum = normalQuantity - releaseTimeNum;
                allocationVO.setId(variableList.getId().longValue());
                allocationVO.setApiCode(variableList.getApiCode());
                allocationVO.setAllocationType(variableList.getAllocationType());
                allocationVO.setNormalQuantity(normalQuantity);
                allocationVO.setAbnormalQuantity(abnormalQuantity);
                allocationVO.setReleaseTimeNum(releaseTimeNum);
                allocationVO.setFalseNum(falseNum);
                allocationVO.setRequestTime(vo.getRequestTime());
                allocationVO.setRequestEndTime(vo.getRequestEndTime());
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
        //数值为0报警
        if (normalQuantity == 0 || abnormalQuantity == 0){
            String msg = "携程总量级: " + normalQuantity + ",异常报警量级:" + abnormalQuantity;
            xcExceptionDataRetryService.sendDingDingAlert("携程定制化配置异常！", msg);
        }
        //原数据记录
        VariableAllocation data = variableAllocationMapper.selectByPrimaryKey(id.intValue());
        JSONObject json = JSON.parseObject(data.getAllocationValue());
        int trueDataThresholdSize =  json.getInteger("trueDataThresholdSize");
        int retryThresholdSize = json.getInteger("retryThresholdSize");
        if (normalQuantity == trueDataThresholdSize && abnormalQuantity == retryThresholdSize ) {
            return new ApiResult<Boolean>().success(true,"修改前后数据一致");
        }
        //更新记录
        VariableAllocationVO allocationVO = new VariableAllocationVO();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("trueDataThresholdSize", normalQuantity);
        jsonObject.put("retryThresholdSize", abnormalQuantity);
        allocationVO.setId(id);
        allocationVO.setAllocationValue(jsonObject.toString());
        int i = variableAllocationMapper.updateByPrimaryMutchKeySelective(allocationVO);
        VariableAllocation newData = variableAllocationMapper.selectByPrimaryKey(id.intValue());
        if (i > 0 ){
            entityOptService.writeOptLog(id, newData, data);
            // 将数据保存到 Redis
            if (XIECHENG_TYPE.equals(data.getAllocationType())){
                String key = RedisKeyConstant.prefix.concat(":").concat(data.getApiCode()).concat(":").concat(TYPE);
                try {
                    redisChgService.del(key);
                    redisChgService.setex(key, newData.getAllocationValue(), 5*60);
                    return new ApiResult<Boolean>().success(true);
                } catch (Exception e) {
                    log.error("获取携程定制配置接口更新redis异常{}", e);
                    return new ApiResult<Boolean>().fail(false,"更新携程定制配置redis异常");
                }
            }
            return new ApiResult<Boolean>().fail(false, "更新携程定制配置");
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
        String allocationValue = null;
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
        VariableAllocation variable = variableAllocationMapper.getVariable(apiCode, TYPE);
        if (ObjectUtil.isNotEmpty(variable)) {
            String value = variable.getAllocationValue();
            JSONObject json = JSON.parseObject(value);
            int dbTrueNum = json.getInteger("trueDataThresholdSize");
            int dbFalseNum = json.getInteger("retryThresholdSize");
            allocationVO.setNormalQuantity(dbTrueNum);
            allocationVO.setAbnormalQuantity(dbFalseNum);
            return allocationVO;
        }
        return null;
    }

}
