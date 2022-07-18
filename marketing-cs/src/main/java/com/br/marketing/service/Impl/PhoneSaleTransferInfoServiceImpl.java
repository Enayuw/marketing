package com.br.marketing.service.Impl;

import com.br.marketing.entity.PhoneSaleTransferInfo;
import com.br.marketing.mapper.PhoneSaleTransferInfoMapper;
import com.br.marketing.service.PhoneSaleTransferInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 业务逻辑实现
 *
 * @author Guo Zeqiang
 * @dateTime 2022/7/14 20:13
 */
@Service
public class PhoneSaleTransferInfoServiceImpl implements PhoneSaleTransferInfoService {

    @Resource
    private PhoneSaleTransferInfoMapper phoneSaleTransferInfoMapper;

    @Override
    public void insertSelectiveBatch(List<PhoneSaleTransferInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<PhoneSaleTransferInfo> filterList = list.parallelStream().filter(info -> !ObjectUtils.isEmpty(info)
                && StringUtils.isNotEmpty(info.getApiCode()) && StringUtils.isNotEmpty(info.getCusaNum()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterList)) {
            return;
        }
        phoneSaleTransferInfoMapper.insertSelectiveBatch(filterList);
    }

    @Override
    public Set<String> findCusaNumList(Set<String> cusaNums, PhoneSaleTransferInfo info) {
        return phoneSaleTransferInfoMapper.findCusaNumList(cusaNums, info);
    }
}
