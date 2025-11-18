package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.entity.TcyrCpaCollidingDataPackage;
import com.br.marketing.entity.TcyrCpaCollidingDataPackageExample;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

@Service
@Slf4j
public class TcCpaDataPackageServiceImpl implements TcCpaDataPackageService {

    @Resource
    TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

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
}
