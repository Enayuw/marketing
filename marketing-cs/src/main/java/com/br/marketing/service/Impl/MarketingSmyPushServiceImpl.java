package com.br.marketing.service.Impl;

import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.MarketingSmyPushService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 萨摩耶数据推送实现类
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.service.Impl
 * @Description: 多线程处理类
 * @CreateTime: 2022-07-01 14 :00
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */

@Service
public class MarketingSmyPushServiceImpl implements MarketingSmyPushService {

    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public void pushSmyUploadDataToDaas() {
        //7410437 为测试apiCode
        List<MarketingSyncUser> marketingSyncUserList = marketingSyncInfoMapper.getSmyDataByGroupType("7410437", "S09");
        List<BatchRealTimeUserDataDTO> subList = new ArrayList<>();
        marketingSyncUserList.stream().forEach(msu -> {
            DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
            PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
            BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
            dassImportDataDTO.setId(msu.getId());
            dassImportDataDTO.setName("1");
            dassImportDataDTO.setOrgname("samoye");
            dassImportDataDTO.setPhone(msu.getCell());
            dassImportDataDTO.setUserType("1");
//            dassImportDataDTO.setRecvData();
//            dassImportDataDTO.setRecvVars();
            dassImportDataDTO.setUid(msu.getCustNum());
            dassImportDataDTO.setSource("23");
            batchRealTimeUserDataDTO.setDassImportDataDTO(dassImportDataDTO);
            BeanUtils.copyProperties(msu, phoneSaleExtendInfo);
            phoneSaleExtendInfo.setSourceId(msu.getId());
            phoneSaleExtendInfo.setPStatus(1);
            batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
            subList.add(batchRealTimeUserDataDTO);
        });

        smyPushDaas(subList);

    }


    public void smyPushDaas(List<BatchRealTimeUserDataDTO> batchRealTimeUserDataDTOList) {
        /**
         * 批量人工推电销接口 每1000条数据一个批次
         */
        int pageSize = 1000;
        int totalCount = batchRealTimeUserDataDTOList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BatchRealTimeUserDataDTO> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = batchRealTimeUserDataDTOList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = batchRealTimeUserDataDTOList.subList((i - 1) * pageSize, pageSize * (i));
            }
            DassImportAdapDTO dassImportAdapDTO = new DassImportAdapDTO();

            List<DassImportDataDTO> dataDTOS = subList.stream().map(batchData->batchData.getDassImportDataDTO()).collect(Collectors.toList());
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos = subList.stream().map(batchData->batchData.getPhoneSaleExtendInfo())
                    .filter(item-> StringUtils.isNotEmpty(item)).collect(Collectors.toList());
            dassImportAdapDTO.setList(dataDTOS);
            dassImportAdapDTO.setPhoneSaleExtendInfos(phoneSaleExtendInfos);
            if (!CollectionUtils.isEmpty(phoneSaleExtendInfos)){
                phoneSaleExtendInfoMapper.saveBatch(dassImportAdapDTO.getPhoneSaleExtendInfos());
            }
            methodRetryHandlerService.callDassRealTimeBatchData(dassImportAdapDTO, 0);
        }
    }

}
