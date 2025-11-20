package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.entity.TcyrCpaCollidingDataCleanTask;
import com.br.marketing.entity.TcyrCpaCollidingDataCleanTaskExample;
import com.br.marketing.entity.TcyrCpaCollidingDataPackage;
import com.br.marketing.entity.TcyrCpaCollidingDataPackageExample;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.mapper.TcyrCpaCollidingDataCleanTaskMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class TcCpaDataPackageServiceImpl implements TcCpaDataPackageService {

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

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
        dataPackage.setApiCode(dto.getApiCode());
        dataPackage.setPackageName(dto.getPackageName());
        dataPackage.setBatchNumbers(String.join(",", dto.getBatchNumberList()));
        dataPackage.setConditions(EsConditionTransferSqlUtil.jsonTransferSql(conditionJson, ""));
        tcyrCpaCollidingDataPackageMapper.insertSelective(dataPackage);
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
    public Result enable(String packageName, Integer status) {
        TcyrCpaCollidingDataPackageExample dataPackageExample = new TcyrCpaCollidingDataPackageExample();
        dataPackageExample.createCriteria().andPackageNameEqualTo(packageName);

        TcyrCpaCollidingDataPackage dataPackage = new TcyrCpaCollidingDataPackage();
        dataPackage.setEnabled(status);
        tcyrCpaCollidingDataPackageMapper.updateByExampleSelective(dataPackage, dataPackageExample);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result genCleanTask(TcCpDataCleanTaskDTO dto) {
        TcyrCpaCollidingDataCleanTaskExample taskExample = new TcyrCpaCollidingDataCleanTaskExample();
        taskExample.createCriteria().andCleanStatusIn(Lists.newArrayList(DataCleanStatusEnum.READY.getCode(),
                DataCleanStatusEnum.RUNNING.getCode()));
        if (tcyrCpaCollidingDataCleanTaskMapper.countByExample(taskExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在清洗中或待清洗的任务，禁止新增清洗任务");
        }
        TcyrCpaCollidingDataCleanTask cleanTask = new TcyrCpaCollidingDataCleanTask();
        cleanTask.setCleanPackageIds(String.join(",", dto.getPackageList()));
        cleanTask.setCleanStatus(DataCleanStatusEnum.READY.getCode());
        tcyrCpaCollidingDataCleanTaskMapper.insertSelective(cleanTask);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
