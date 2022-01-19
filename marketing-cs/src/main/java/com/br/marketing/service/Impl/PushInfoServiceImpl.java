package com.br.marketing.service.Impl;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.PushInfoFilterDTO;
import com.br.marketing.entity.CustomerInfoPushBatch;
import com.br.marketing.entity.CustomerInfoPushBatchExample;
import com.br.marketing.mapper.CustomerInfoPushBatchMapper;
import com.br.marketing.mapper.CustomerInfoPushMainMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.vo.PushInfoListVO;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PushInfoServiceImpl implements PushInfoService {

    @Autowired
    private CustomerInfoPushMainMapper customerInfoPushMainMapper;

    @Autowired
    private CustomerInfoPushBatchMapper customerInfoPushBatchMapper;

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
            return pushInfoListVO;
        }).collect(Collectors.toList());
        return PageResultReturn.setPageResult(list, dto.getCurrent(), dto.getSize());
    }


}
