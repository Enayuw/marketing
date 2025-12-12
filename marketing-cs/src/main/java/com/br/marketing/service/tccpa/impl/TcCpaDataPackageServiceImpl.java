package com.br.marketing.service.tccpa.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.tccpa.TcyrCpaCollidingDataPackageVO;
import com.br.marketing.entity.*;
import com.br.marketing.enums.TcCpaCleanStatusEnum;
import com.br.marketing.enums.TcCpaCollidingTaskStatusEnum;
import com.br.marketing.enums.clean.DataCleanStatusEnum;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataCleanTaskMapper;
import com.br.marketing.mapper.TcyrCpaCollidingDataPackageMapper;
import com.br.marketing.mapper.TcyrCpaCollidingTaskMapper;
import com.br.marketing.service.tccpa.TcCpaDataPackageService;
import com.br.marketing.util.EsConditionTransferSqlUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TcCpaDataPackageServiceImpl implements TcCpaDataPackageService {

    @Resource
    private TcyrCpaCollidingDataPackageMapper tcyrCpaCollidingDataPackageMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private TcyrCpaCollidingDataCleanTaskMapper tcyrCpaCollidingDataCleanTaskMapper;

    @Resource
    private TcyrCpaCollidingTaskMapper tcyrCpaCollidingTaskMapper;

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
    public PageResultReturn<TcyrCpaCollidingDataPackageVO> page(int page, int pageSize, String packageName, Integer status) {
        PageHelper.startPage(page, pageSize);
        List<TcyrCpaCollidingDataPackage> packages = tcyrCpaCollidingDataPackageMapper.selectByCondition(packageName, status);
        if(CollectionUtils.isEmpty(packages)) {
            return PageResultReturn.setPageResult(Lists.newArrayList(), page, pageSize);
        }
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
        PageInfo<TcyrCpaCollidingDataPackage> marketingCustomerPageInfo = new PageInfo<>(packages);
        return PageResultReturn.setPageResult(packageVOS, page, pageSize, marketingCustomerPageInfo.getTotal());
    }

    @Override
    public Result update(TcyrCpaCollidingDataPackageVO packageVO) {
        TcyrCpaCollidingDataCleanTaskExample taskExample = new TcyrCpaCollidingDataCleanTaskExample();
        taskExample.createCriteria().andCleanStatusNotEqualTo(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
        if (tcyrCpaCollidingDataCleanTaskMapper.countByExample(taskExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在待清洗、清洗中、清洗失败或重试的清洗任务，不能修改数据包");
        }
        TcyrCpaCollidingTaskExample collidingTaskExample = new TcyrCpaCollidingTaskExample();
        collidingTaskExample.createCriteria().andPackageIdsLike("%" + packageVO.getId() + "%")
                .andStatusLessThan(TcCpaCollidingTaskStatusEnum.STATUS_PUSH_COMPLETED.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        if(packageVO.getEnabled().equals(Constants.ENABLED_FORB) && tcyrCpaCollidingTaskMapper.countByExample(collidingTaskExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("撞库任务中存在的数据包不能禁用");
        }

        TcyrCpaCollidingDataPackage dataPackage = new TcyrCpaCollidingDataPackage();
        BeanUtils.copyProperties(packageVO, dataPackage);

        TcyrCpaCollidingDataPackageExample example = new TcyrCpaCollidingDataPackageExample();
        example.createCriteria().andIdEqualTo(packageVO.getId());

        tcyrCpaCollidingDataPackageMapper.updateByExampleSelective(dataPackage, example);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result delete(Long id) {
        TcyrCpaCollidingDataPackage collidingDataPackage = tcyrCpaCollidingDataPackageMapper.selectByPrimaryKey(id);
        TcyrCpaCollidingDataPackageExample dataPackageExample = new TcyrCpaCollidingDataPackageExample();
        dataPackageExample.createCriteria().andIdEqualTo(id);
        TcyrCpaCollidingDataPackage dataPackage = new TcyrCpaCollidingDataPackage();
        if (Objects.equals(collidingDataPackage.getCleanStatus(), TcCpaCleanStatusEnum.CLEAN_VOID.getValue())) {
            dataPackage.setIsDel(Constants.DATA_DEL);
        } else {
            TcyrCpaCollidingDataCleanTaskExample taskExample = new TcyrCpaCollidingDataCleanTaskExample();
            taskExample.createCriteria().andCleanStatusNotEqualTo(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
            if (tcyrCpaCollidingDataCleanTaskMapper.countByExample(taskExample) > 0) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在待清洗、清洗中、清洗失败或重试的清洗任务，禁止删除数据包");
            }
            TcyrCpaCollidingTaskExample collidingExample = new TcyrCpaCollidingTaskExample();
            collidingExample.createCriteria().andPackageIdsLike("%" + id + "%")
                    .andStatusLessThan(TcCpaCollidingTaskStatusEnum.STATUS_PUSH_COMPLETED.getValue())
                    .andIsDelEqualTo(Constants.DATA_VALID);
            if (tcyrCpaCollidingTaskMapper.countByExample(collidingExample) > 0) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("撞库任务中存在的数据包不能删除");
            }
            dataPackage.setIsDel(Constants.DATA_DELING);
        }
        tcyrCpaCollidingDataPackageMapper.updateByExampleSelective(dataPackage, dataPackageExample);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result genCleanTask() {
        TcyrCpaCollidingDataCleanTaskExample taskExample = new TcyrCpaCollidingDataCleanTaskExample();
        taskExample.createCriteria().andCleanStatusNotEqualTo(TcCpaCleanStatusEnum.CLEAN_SUCCESS.getValue());
        if (tcyrCpaCollidingDataCleanTaskMapper.countByExample(taskExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在待清洗、清洗中、清洗失败或重试的清洗任务，禁止新增清洗任务");
        }
        TcyrCpaCollidingDataPackageExample dataPackageExample = new TcyrCpaCollidingDataPackageExample();
        dataPackageExample.createCriteria().andIsDelEqualTo(Constants.STATUS_START).andPriorityIsNull();
        if(tcyrCpaCollidingDataPackageMapper.countByExample(dataPackageExample) > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("存在未配置优先级的数据包，不能执行清洗操作");
        }

        TcyrCpaCollidingDataCleanTask cleanTask = new TcyrCpaCollidingDataCleanTask();
        cleanTask.setCleanStatus(DataCleanStatusEnum.READY.getCode());
        tcyrCpaCollidingDataCleanTaskMapper.insertSelective(cleanTask);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
