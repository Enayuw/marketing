package com.br.marketing.check.service.Impl;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.check.service.JuZiCheckToDassService;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.commondto.Result;

import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;

import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/15 17:57
 */
@Service
@Slf4j
public class JuZiCheckToDassServiceImpl implements JuZiCheckToDassService {


    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;
    @Override
    public Result transferDataPeriodToDass(String status,List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellList) {

        List<BatchRealTimeUserDataDTO> transferDataList = new ArrayList<>();
        marketingTransferSyncUserCellList.forEach(marketingTransferSyncUserCell -> {
            DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
            dassImportDataDTO.setId(marketingTransferSyncUserCell.getId());
            dassImportDataDTO.setSource("15");
            dassImportDataDTO.setOptype("1");
            dassImportDataDTO.setOrgname("juzi");
            dassImportDataDTO.setName("1");
            dassImportDataDTO.setUid(marketingTransferSyncUserCell.getCustNum());
            dassImportDataDTO.setUserType("B");
            dassImportDataDTO.setAuditTime(marketingTransferSyncUserCell.getAuditTime());
            dassImportDataDTO.setAuditAmount(marketingTransferSyncUserCell.getAuditAmount());
            String cell = BrCipherMaker.getInstance().decode(marketingTransferSyncUserCell.getCell());
            //解密失败直接丢弃
            if (StringUtils.isEmpty(cell)) {
                return;
            }
            dassImportDataDTO.setPhone(cell);
            BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
            PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
            phoneSaleExtendInfo.setApiCode(marketingTransferSyncUserCell.getApiCode());
            phoneSaleExtendInfo.setCreateTime(new Date());
            phoneSaleExtendInfo.setStatus(status);
            phoneSaleExtendInfo.setCustNum(marketingTransferSyncUserCell.getCustNum());
            phoneSaleExtendInfo.setAppletDate(marketingTransferSyncUserCell.getRequestData());
            phoneSaleExtendInfo.setAppletTime(marketingTransferSyncUserCell.getRequestTime());
            phoneSaleExtendInfo.setPStatus(1);
            phoneSaleExtendInfo.setUserType(marketingTransferSyncUserCell.getUserType());
            phoneSaleExtendInfo.setTransformType("1");
            phoneSaleExtendInfo.setSourceId(dassImportDataDTO.getId());
            phoneSaleExtendInfo.setDxType(dassImportDataDTO.getUserType());
            batchRealTimeUserDataDTO.setDassImportDataDTO(dassImportDataDTO);
            batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
            transferDataList.add(batchRealTimeUserDataDTO);

        });

        JuZiPushDaas(transferDataList);
        return null;
    }

    public void JuZiPushDaas(List<BatchRealTimeUserDataDTO> batchRealTimeUserDataDTOList) {
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
                    .filter(item-> com.br.marketing.common.utils.StringUtils.isNotEmpty(item)).collect(Collectors.toList());
            dassImportAdapDTO.setList(dataDTOS);
            dassImportAdapDTO.setPhoneSaleExtendInfos(phoneSaleExtendInfos);
            if (!org.apache.commons.collections.CollectionUtils.isEmpty(phoneSaleExtendInfos)){
                phoneSaleExtendInfoMapper.saveBatch(dassImportAdapDTO.getPhoneSaleExtendInfos());
            }
            methodRetryHandlerService.transferDataPeriodToDass(dassImportAdapDTO, 0);
        }
    }
}
