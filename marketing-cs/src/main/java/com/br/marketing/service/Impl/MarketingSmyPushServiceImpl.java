package com.br.marketing.service.Impl;

import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.PhoneSaleTransferInfoMapper;
import com.br.marketing.service.MarketingSmyPushService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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

    @Autowired
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Autowired
    private PhoneSaleTransferInfoMapper phoneSaleTransferInfoMapper;

    @Override
    public void pushSmyUploadDataToDaas() {
        //7410437 为测试apiCode
        List<MarketingSyncUser> marketingSyncUserList = marketingSyncInfoMapper.getSmyDataByGroupType("7410437", "S09");
        List<DassImportDataDTO> dassImportDataDTOlist = new ArrayList<>();
        marketingSyncUserList.stream().forEach(msu -> {
            DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
            dassImportDataDTO.setName("1");
            dassImportDataDTO.setOrgname("samoye");
            dassImportDataDTO.setPhone(msu.getCell());
//            dassImportDataDTO.setRecvData();
//            dassImportDataDTO.setRecvVars();
            dassImportDataDTO.setUid(msu.getCustNum());
            dassImportDataDTO.setSource("23");
            dassImportDataDTOlist.add(dassImportDataDTO);

        });
        smyPushDaas(dassImportDataDTOlist);

    }

    @Override
    public void pushSmyTransferDataToDaas() {
        //7410437 为测试apiCode
        List<MarketingTransferCell> marketingTransferInfoList = marketingTransferInfoMapper.getSmyTransferDataByGroupType("7410437", "S09");
        List<DassTransferDataDTO> dassImportDataDTOlist = new ArrayList<>();
        marketingTransferInfoList.stream().forEach(transfer -> {
            DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
            dassTransferDataDTO.setId(transfer.getId());
            dassTransferDataDTO.setUid(transfer.getCustNum());
            dassTransferDataDTO.setSource("23");
            dassTransferDataDTO.setUserType("1");
            dassTransferDataDTO.setPhone(transfer.getCell());
            dassTransferDataDTO.setOrgName("samoye");
            dassTransferDataDTO.setIfTransform("1");
            dassTransferDataDTO.setTransformStatus("1");
            dassImportDataDTOlist.add(dassTransferDataDTO);

        });
        smyTransferPushDaas(dassImportDataDTOlist);

    }
    public void smyPushDaas(List<DassImportDataDTO> daasImportDataDTOlist) {
        /**
         * 批量推电销接口 每1000条数据一个批次
         */
        int pageSize = 1000;
        int totalCount = daasImportDataDTOlist.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<DassImportDataDTO> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = daasImportDataDTOlist.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = daasImportDataDTOlist.subList((i - 1) * pageSize, pageSize * (i));
            }
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos = new ArrayList<>();
            DassImportAdapDTO dassImportAdapDTO = new DassImportAdapDTO();
            dassImportAdapDTO.setList(subList);
            BeanUtils.copyProperties(subList, phoneSaleExtendInfos);
            dassImportAdapDTO.setPhoneSaleExtendInfos(phoneSaleExtendInfos);
            if (!CollectionUtils.isEmpty(phoneSaleExtendInfos)) {
                phoneSaleExtendInfoMapper.saveBatch(dassImportAdapDTO.getPhoneSaleExtendInfos());
            }
            methodRetryHandlerService.callDassRealTimeBatchData(dassImportAdapDTO, 0);
        }
    }
    public void smyTransferPushDaas(List<DassTransferDataDTO> transferData) {
        /**
         * 电销转化接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferData.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<DassTransferDataDTO> subList;
            if (i == pageCount) {
                subList = transferData.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferData.subList((i - 1) * pageSize, pageSize * (i));
            }
            DassTransferDataAdapDTO dassTransferDataAdapDTO = new DassTransferDataAdapDTO();
            dassTransferDataAdapDTO.setDassTransferDataDTOList(subList);
            methodRetryHandlerService.callDassTransferData(dassTransferDataAdapDTO, 0);
        }
    }
}
