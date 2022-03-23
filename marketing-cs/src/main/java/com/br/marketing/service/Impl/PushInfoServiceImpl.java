package com.br.marketing.service.Impl;

import com.br.marketing.common.enums.ApiReturnEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushBatchExample;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushLogMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.vo.PushInfoListVO;
import com.br.marketing.vo.RulePushLogOfStatusVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public PageResultReturn getPushInfoList(PushInfoFilterDTO dto) {
        final char ch = ',';
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<PushInfoListVO> list = customerInfoPushMainMapper.getPushInfoList(dto);
        List<Long> ids = list.stream().map(t -> t.getId()).collect(Collectors.toList());
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

            list.forEach(t -> {
                t.setBatchNumbers(batchNumberOfMid.get(t.getId()));
                List<RulePushLogOfStatusVO> rulePushLogOfStatusVOS1 = realStatusOfMid.get(t.getId());
                List<Map> msgList = new ArrayList<>();
                if(rulePushLogOfStatusVOS1!=null){
                    rulePushLogOfStatusVOS1.forEach(k -> {
                        Map msg = new HashMap();
                        msg.put("code", k.getRealStatus());
                        msg.put("message", ApiReturnEnum.getByCode(k.getRealStatus()));
                        msgList.add(msg);
                    });
                }
                t.setReturnMessages(msgList);
            });
        }
        return PageResultReturn.setPageResult(list, dto.getCurrent(), dto.getSize());
    }


}
