package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
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
 * @author songjuanjuan
 * @dateTime 2021/10/27 13:12
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
    public PageResultReturn getVariableList(VariableAllocationDTO dto) {
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        String apiCode = dto.getApiCode();
        String allocationType = dto.getAllocationType();
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // 格式化当前日期时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime currentTime = LocalDateTime.now();
            String now = currentTime.format(formatter);
            Date date = dto.getRequestTime().equals("") ? outputFormat.parse(now) : outputFormat.parse(dto.getRequestTime());
            List<VariableAllocation> variableList = variableAllocationMapper.getVariableList(apiCode, allocationType);
            ArrayList<VariableAllocationVO> arrayList = new ArrayList<>();
            for (VariableAllocation variabl : variableList) {
                VariableAllocationVO allocationVO = new VariableAllocationVO();
                String allocationValue = variabl.getAllocationValue();
                JSONObject jsonObject = JSON.parseObject(allocationValue);
                int normalQuantity =  jsonObject.getInteger("trueDataThresholdSize");
                int abnormalQuantity = jsonObject.getInteger("retryThresholdSize");
                int releaseTimeNum = variableAllocationMapper.getVariableAllocationVO(date);
                int falseNum = normalQuantity - releaseTimeNum;
                allocationVO.setId(variabl.getId().longValue());
                allocationVO.setApiCode(variabl.getApiCode());
                allocationVO.setAllocationType(variabl.getAllocationType());
                allocationVO.setNormalQuantity(normalQuantity);
                allocationVO.setAbnormalQuantity(abnormalQuantity);
                allocationVO.setReleaseTimeNum(releaseTimeNum);
                allocationVO.setFalseNum(falseNum);
                arrayList.add(allocationVO);
            }

            return PageResultReturn.setPageResult(arrayList, dto.getCurrent(), dto.getSize());
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
        allocationVO.setId(id);
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
        String apiCode = "3710058";
        String allocationType = "携程定制";
        int dbTrueNum = 5000000;
        int dbFalseNum = 100000;
        int normalQuantity = 5000000;
        int abnormalQuantity = 100000;
        VariableAllocation variable = variableAllocationMapper.getVariable(apiCode, allocationType);
        if (ObjectUtil.isNotEmpty(variable)){
            String value = variable.getAllocationValue();
            JSONObject json = JSON.parseObject(value);
            dbTrueNum = json.getInteger("normalQuantity");
            dbFalseNum = json.getInteger("abnormalQuantity");
        }
        String key = RedisKeyConstant.prefix.concat(":").concat(apiCode).concat(":").concat(allocationType);
        if (StringUtil.isNotEmpty(key)){
            String allocationValue = redisChgService.get(key);
            JSONObject jsonObject = JSON.parseObject(allocationValue);
            normalQuantity = jsonObject.getInteger("normalQuantity");
            abnormalQuantity = jsonObject.getInteger("abnormalQuantity");
        } else{
            allocationVO.setNormalQuantity(dbTrueNum);
            allocationVO.setAbnormalQuantity(dbFalseNum);
            return allocationVO;
        }
        allocationVO.setNormalQuantity(normalQuantity);
        allocationVO.setAbnormalQuantity(abnormalQuantity);
        return allocationVO;
    }

}
