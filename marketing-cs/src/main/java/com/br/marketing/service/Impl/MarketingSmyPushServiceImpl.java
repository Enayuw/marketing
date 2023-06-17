package com.br.marketing.service.Impl;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Override
    public void pushSmyUploadDataToDaas(String apiCode) {
        //7410437 为测试apiCode
        List<MarketingSyncUser> marketingSyncUserList = marketingSyncInfoMapper.getSmyDataByGroupType(StringUtils.isBlank(apiCode) ? "3710013" : apiCode, "S09");
        List<BatchRealTimeUserDataDTO> subList = new ArrayList<>();
        marketingSyncUserList.stream().forEach(msu -> {
            DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
            PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
            BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
            dassImportDataDTO.setId(msu.getId());
            dassImportDataDTO.setName("1");
            dassImportDataDTO.setOrgname("samoye");
            String cell = BrCipherMaker.getInstance().decode(msu.getCell());
            dassImportDataDTO.setPhone(AESUtil.aesEncrypty(cell, aesKey));
            dassImportDataDTO.setUserType("1");
//            dassImportDataDTO.setRecvData();
//            dassImportDataDTO.setRecvVars();
            dassImportDataDTO.setUid(msu.getCustNum());
            dassImportDataDTO.setSource("23");
            batchRealTimeUserDataDTO.setDassImportDataDTO(dassImportDataDTO);
            BeanUtils.copyProperties(msu, phoneSaleExtendInfo);
            phoneSaleExtendInfo.setSourceId(msu.getId());
            phoneSaleExtendInfo.setPStatus(1);
            phoneSaleExtendInfo.setCreateTime(new Date());
            phoneSaleExtendInfo.setUpdateTime(new Date());
            phoneSaleExtendInfo.setPushDxTime(new Date());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            phoneSaleExtendInfo.setAppletTime(simpleDateFormat.format(msu.getAppletTime()));
            batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
            subList.add(batchRealTimeUserDataDTO);
        });

        smyPushDaas(subList);

    }

    @Override
    public void pushSmyTransferDataToDaas(String apiCode) {
        //7410437 为测试apiCode
        List<MarketingTransferCell> marketingTransferInfoList = marketingTransferInfoMapper.getSmyTransferDataByGroupType(StringUtils.isBlank(apiCode) ? "3710013" : apiCode, "S09");
        List<DassTransferDataDTO> dassTransferDataDTOList = new ArrayList<>();
        marketingTransferInfoList.stream().forEach(transfer -> {
            DassTransferDataDTO dassTransferDataDTO = new DassTransferDataDTO();
            dassTransferDataDTO.setId(transfer.getId());
            dassTransferDataDTO.setUid(transfer.getCustNum());
            dassTransferDataDTO.setSource("23");
            dassTransferDataDTO.setUserType("1");
            String cell = BrCipherMaker.getInstance().decode(transfer.getCell());
            dassTransferDataDTO.setPhone(cell);
            dassTransferDataDTO.setPhone(transfer.getCell());
            dassTransferDataDTO.setOrgName("samoye");
            dassTransferDataDTO.setIfTransform("1");
            dassTransferDataDTO.setTransformStatus("1");
            dassTransferDataDTOList.add(dassTransferDataDTO);
        });
        smyTransferPushDaas(dassTransferDataDTOList);

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

            List<DassImportDataDTO> dataDTOS = subList.stream().map(batchData -> batchData.getDassImportDataDTO()).collect(Collectors.toList());
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos = subList.stream().map(batchData -> batchData.getPhoneSaleExtendInfo())
                    .filter(item -> StringUtils.isNotEmpty(item)).collect(Collectors.toList());
            dassImportAdapDTO.setList(dataDTOS);
            dassImportAdapDTO.setPhoneSaleExtendInfos(phoneSaleExtendInfos);
            if (!CollectionUtils.isEmpty(phoneSaleExtendInfos)) {
                phoneSaleExtendInfoMapper.saveBatch(dassImportAdapDTO.getPhoneSaleExtendInfos());
            }
            methodRetryHandlerService.smyCallDassRealTimeBatchData(dassImportAdapDTO, 0);
        }
    }

    public void smyTransferPushDaas(List<DassTransferDataDTO> transferData) {
        /**
         * 萨摩耶转化数据剔除 每500条数据一个批次
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
            methodRetryHandlerService.smyCallDassTransferData(dassTransferDataAdapDTO, 0);
        }
    }
}
