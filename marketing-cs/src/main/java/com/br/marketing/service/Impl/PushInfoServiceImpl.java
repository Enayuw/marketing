package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.output.PolicyResultByTaskIdsDTO;
import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.ApiReturnEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushBatchExample;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushLogMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.mapper.MarketingTaskUserTypeMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.vo.PushInfoListVO;
import com.br.marketing.vo.RulePushLogOfStatusVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PushInfoServiceImpl implements PushInfoService {

    @Autowired
    private CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Autowired
    private CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

    @Autowired
    private CustomerInfoPushLogMapper customerInfoPushLogMapper;
    @Resource
    MarketingTaskUserTypeMapper marketingTaskUserTypeMapper;

    @Autowired
    private IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Override
    public PageResultReturn getPushInfoList(PushInfoFilterDTO dto) {
        final char ch = ',';
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<PushInfoListVO> list = customerInfoPushMainMapper.getPushInfoList(dto);
        List<Long> ids = list.stream().map(t -> t.getId()).collect(Collectors.toList());
        List<String> failStatusIds =list.stream().filter(t->t.getmStatus().equals(5)).map(t->String.valueOf(t.getId())).collect(Collectors.toList());
        if(ids.size()>0) {
            CustomerInfoPushBatchExample example = new CustomerInfoPushBatchExample();
            example.createCriteria().andMIdIn(ids).andIsDelEqualTo(1);
            List<CustomerInfoPushBatch> batches = customerInfoPushBatchMapper.selectByExample(example);

            HashMap<Long, String> batchNumberOfMid = batches.stream()
                    .collect(Collectors.groupingBy(CustomerInfoPushBatch::getmId
                            , HashMap::new
                            , Collectors.mapping(CustomerInfoPushBatch::getmBatchNumber, Collectors.joining(","))));

            List<RulePushLogOfStatusVO> rulePushLogOfStatusVOS = customerInfoPushLogMapper.selectRealStatusByMid(ids);
            Map<Long, List<RulePushLogOfStatusVO>> realStatusOfMid = rulePushLogOfStatusVOS.stream()
                    .collect(Collectors.groupingBy(RulePushLogOfStatusVO::getMId));
            Map<String,Map<String,Object>> resultMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(failStatusIds)) {
                Result<List<PolicyResultByTaskIdsDTO>> result = intelligentCustomerServiceClient.getTaskIdsResult(dto.getmApiCode(), failStatusIds);
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    List<PolicyResultByTaskIdsDTO> resultByTaskIdsDTOS = result.getData();
                    resultByTaskIdsDTOS.forEach(policyResultByTaskIdsDTO -> {
                        Map errorMap = JSON.parseObject(policyResultByTaskIdsDTO.getVerificationReason());
                        resultMap.put(policyResultByTaskIdsDTO.getVerification(), errorMap);
                    });
                } else {
                    log.warn("决策查询接口异常result={}", JSON.toJSONString(result));
                }
            }
            list.forEach(t -> {
                String batchNumber = batchNumberOfMid.get(t.getId());
                t.setBatchNumbers(batchNumber);
                List<String> userTypeList = marketingTaskUserTypeMapper.queryUserTypeByBatchNumbertikv_(batchNumber);
                if(null != userTypeList){
                    String userType = userTypeList.stream().collect(Collectors.joining(","));
                    t.setUserType(userType);
                }
                List<Map> msgList = new ArrayList<>();
                Map map = resultMap.get(t.getId().toString());
                msgList.add(map);
                t.setReturnMessages(msgList);
            });
        }
        return PageResultReturn.setPageResult(list, dto.getCurrent(), dto.getSize());
    }

    @Autowired
    MarketingApiService marketingApiService;

    @Override
    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    public Result pushUploadByRetry(UploadDataDTO dto, Integer retry) {
        return marketingApiService.pushUpload(dto);
    }

    @Override
    @RetryMethod(retryNowNum = 2,isOrNoDbRetry = true)
    public Result pushTransferByRetry(PushTransferDataDetailDTO dto, Integer retry) {
        return marketingApiService.pushMarketingApiTransfer(dto,retry);
    }


}
