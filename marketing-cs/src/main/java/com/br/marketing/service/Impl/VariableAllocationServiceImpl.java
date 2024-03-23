package com.br.marketing.service.Impl;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
                BigDecimal normalQuantity =  jsonObject.getBigDecimal("normalQuantity");
                BigDecimal abnormalQuantity = jsonObject.getBigDecimal("abnormalQuantity");
                BigDecimal releaseTimeNum = variableAllocationMapper.getVariableAllocationVO(date);
                BigDecimal falseNum = normalQuantity.subtract(releaseTimeNum);
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
    public ApiResult<Boolean> updateVariableList(Long id, BigDecimal normalQuantity, BigDecimal abnormalQuantity) {
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

    public VariableAllocationVO getVariableAllocation(){
        VariableAllocationVO allocationVO = new VariableAllocationVO();
        String key = RedisKeyConstant.prefix.concat(":").concat("3710058").concat(":").concat("携程定制");
        String allocationValue = redisChgService.get(key);
        JSONObject jsonObject = JSON.parseObject(allocationValue);
        BigDecimal normalQuantity = jsonObject.getBigDecimal("normalQuantity");
        BigDecimal abnormalQuantity = jsonObject.getBigDecimal("abnormalQuantity");
        allocationVO.setNormalQuantity(normalQuantity);
        allocationVO.setAbnormalQuantity(abnormalQuantity);
        return allocationVO;
    }
}
