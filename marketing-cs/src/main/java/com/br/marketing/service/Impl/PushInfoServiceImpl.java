package com.br.marketing.service.Impl;

import com.br.marketing.common.enums.ApiReturnEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushBatchExample;
import com.br.marketing.entity.CustomerInfoPushLog;
import com.br.marketing.entity.CustomerInfoPushLogExample;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushLogMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.vo.PushInfoListVO;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

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
        list.stream().map(pushInfoListVO->{
            //获取跑分批次号
            StringBuilder batchNumbers = new StringBuilder();
            CustomerInfoPushBatchExample example = new CustomerInfoPushBatchExample();
            example.createCriteria().andMIdEqualTo(pushInfoListVO.getId()).andIsDelEqualTo(1);
            List<CustomerInfoPushBatch> batches = customerInfoPushBatchMapper.selectByExample(example);
            for(CustomerInfoPushBatch s : batches){
                batchNumbers.append(s.getmBatchNumber()).append(",");
            }
            // 得到最后一个字符的索引地址
            int index = batchNumbers.length() - 1;
            // 取到最后一个字符
            char c = batchNumbers.charAt(index);
            if (ch == c) {
                // 删除最后一个字符
                batchNumbers.deleteCharAt(index);
            }
            pushInfoListVO.setBatchNumbers(batchNumbers.toString());

            //获取推送结果信息
            List<Map> msgList = new ArrayList<>();
            CustomerInfoPushLogExample pushLogExample = new CustomerInfoPushLogExample();
            pushLogExample.createCriteria().andMIdEqualTo(pushInfoListVO.getId());
            List<CustomerInfoPushLog> logs = customerInfoPushLogMapper.selectByExample(pushLogExample);
            for(CustomerInfoPushLog log : logs){
                if(log.getRealStauts()!=null){
                    Map msg = new HashMap();
                    msg.put("code",log.getRealStauts());
                    msg.put("message", ApiReturnEnum.getByCode(log.getRealStauts()));
                    msgList.add(msg);
                }
            }
            pushInfoListVO.setReturnMessages(msgList);

            return pushInfoListVO;
        }).collect(Collectors.toList());
        return PageResultReturn.setPageResult(list, dto.getCurrent(), dto.getSize());
    }


}
