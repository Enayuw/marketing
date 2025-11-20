package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.tccpa.TcyrCpaCollidingDataPackageVO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataCleanTaskMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TcCpaDataPackageServiceImpl implements TcCpaDataPackageService {

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private TcyrCpaCollidingDataCleanTaskMapper tcyrCpaCollidingDataCleanTaskMapper;

    @Override
    public Result tcDataPackageGen(TcCpDataPackageGenDTO dto) {
        //1.校验数据包名称是否重复
        TcyrCpaCollidingDataPackageExample dataPackageExample = new TcyrCpaCollidingDataPackageExample();
        dataPackageExample.createCriteria()
                .andPackageNameEqualTo(dto.getPackageName())
                .andIsDelEqualTo(Constants.DATA_VALID);
        int count = tcyrCpaCollidingDataPackageMapper.countByExample(dataPackageExample);
        if (count > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("数据包名称重复，请更换名称");
        }
        //2.插入【b_tcyr_cpa_colliding_data_package】
        JSONObject conditionJson = JSON.parseObject(dto.getMRuleCondition());
        TcyrCpaCollidingDataPackage dataPackage = new TcyrCpaCollidingDataPackage();
        dataPackage.setPackageName(dto.getPackageName());
        dataPackage.setBatchNumbers(String.join(",", dto.getBatchNumberList()));
        dataPackage.setConditions(EsConditionTransferSqlUtil.jsonTransferSql(conditionJson, ""));
        tcyrCpaCollidingDataPackageMapper.insertSelective(dataPackage);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public PageResultReturn<TcyrCpaCollidingDataPackageVO> page(int page, int pageSize, String packageName, Integer status) {
        TcyrCpaCollidingDataPackageExample example = new TcyrCpaCollidingDataPackageExample();
        if(StringUtils.isNotEmpty(packageName)) {
            example.createCriteria().andPackageNameLike("%" + packageName + "%");
        }
        if(Objects.nonNull(status)) {
            example.createCriteria().andEnabledEqualTo(status);
        }
        List<TcyrCpaCollidingDataPackage> packages = tcyrCpaCollidingDataPackageMapper.selectByExample(example);
        List<String> apiCodes = packages.stream().map(TcyrCpaCollidingDataPackage::getApiCode).collect(Collectors.toList());
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andApiCodeIn(apiCodes);
        Map<String, MarketingCustomer> customers = marketingCustomerMapper.selectByExample(customerExample)
                .stream().collect(Collectors.toMap(MarketingCustomer::getApiCode, customer -> customer));
        List<TcyrCpaCollidingDataPackageVO> packageVOS = packages.stream().map(dataPackage -> {
            TcyrCpaCollidingDataPackageVO vo = new TcyrCpaCollidingDataPackageVO();
            BeanUtils.copyProperties(dataPackage, vo);

            MarketingCustomer customer = customers.get(dataPackage.getApiCode());
            if (customer != null) {
                vo.setCid(customer.getCid());
                vo.setCustomerName(customer.getShortName());
            }
            return vo;
        }).collect(Collectors.toList());
        return PageResultReturn.setPageResult(packageVOS, page, pageSize);
    }

    @Override
    public Result update(TcyrCpaCollidingDataPackageVO packageVO) {
        TcyrCpaCollidingDataPackage dataPackage = new TcyrCpaCollidingDataPackage();
        BeanUtils.copyProperties(packageVO, dataPackage);

        tcyrCpaCollidingDataPackageMapper.updateByPrimaryKey(dataPackage);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result delete(TcCpDataPackageGenDTO dto) {
        TcyrCpaCollidingDataCleanTaskExample taskExample = new TcyrCpaCollidingDataCleanTaskExample();
        taskExample.createCriteria().andCleanStatusIn(Lists.newArrayList(DataCleanStatusEnum.READY.getCode(),
                DataCleanStatusEnum.RUNNING.getCode()));
        if (tcyrCpaCollidingDataCleanTaskMapper.countByExample(taskExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在清洗中或待清洗的任务，禁止删除数据包");
        }
        TcyrCpaCollidingDataPackageExample dataPackageExample = new TcyrCpaCollidingDataPackageExample();
        dataPackageExample.createCriteria().andPackageNameEqualTo(dto.getPackageName());
        TcyrCpaCollidingDataPackage dataPackage = new TcyrCpaCollidingDataPackage();
        dataPackage.setIsDel(Constants.STATUS_DELETE);
        tcyrCpaCollidingDataPackageMapper.updateByExampleSelective(dataPackage, dataPackageExample);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result genCleanTask() {
        TcyrCpaCollidingDataCleanTaskExample taskExample = new TcyrCpaCollidingDataCleanTaskExample();
        taskExample.createCriteria().andCleanStatusIn(Lists.newArrayList(DataCleanStatusEnum.READY.getCode(),
                DataCleanStatusEnum.RUNNING.getCode()));
        if (tcyrCpaCollidingDataCleanTaskMapper.countByExample(taskExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在清洗中或待清洗的任务，禁止新增清洗任务");
        }
        TcyrCpaCollidingDataCleanTask cleanTask = new TcyrCpaCollidingDataCleanTask();
        cleanTask.setCleanStatus(DataCleanStatusEnum.READY.getCode());
        tcyrCpaCollidingDataCleanTaskMapper.insertSelective(cleanTask);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
