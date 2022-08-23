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
    public void insertBatch(List<PhoneSaleTransferInfo> list) {
        insertBatch(list, 1000);
    }

    @Override
    public void insertBatch(List<PhoneSaleTransferInfo> list, int batchSize) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<PhoneSaleTransferInfo> filterList = list.parallelStream().filter(info -> !ObjectUtils.isEmpty(info)
                && StringUtils.isNotEmpty(info.getApiCode()) && StringUtils.isNotEmpty(info.getCustNum()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterList)) {
            return;
        }
        int size = filterList.size();
        int pageSum = size % batchSize == 0 ? size / batchSize : (size / batchSize + 1);
        for (int i = 0; i < pageSum; i++) {
            int fromIndex = i * batchSize;
            int toIndex = fromIndex + batchSize;
            phoneSaleTransferInfoMapper.insertBatch(filterList.subList(fromIndex, Math.min(toIndex, size)));
        }
    }

    @Override
    public Set<String> findCusaNumList(Set<String> custNums, PhoneSaleTransferInfo info) {
        return phoneSaleTransferInfoMapper.findCusaNumList(custNums, info);
    }
}
