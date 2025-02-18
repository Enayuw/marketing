package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.TransferFieldProcessFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName TransferFieldProcessByHengChangFactory
 * @Description 恒昌转化数据清洗
 * @Author kongbx
 * @Date 2025/1/15 15:55
 */
@Service
@Slf4j
public class TransferFieldProcessByHengChangFactory implements TransferFieldProcessFactory {


    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public String customerName() {
        return "hengchang";
    }

    @Override
    public void fieldProcess(MarketingTransferSyncUser transferSyncUser, TransferDataItemDTO transferDataItemDTO) {}

    @Override
    public Boolean isFormat() {
        return true;
    }

    @Override
    public TransferDataDTO formatTransferObj(String jsonData) {

        TransferDataDTO transferDataDTO = JSON.parseObject(jsonData, new TypeReference<TransferDataDTO<TransferDataItemDTO>>() {
        }.getType());

        List<TransferDataItemDTO> transferDataItemDTOS = transferDataDTO.getDataItems();

        if(!CollectionUtils.isEmpty(transferDataItemDTOS)){

            TransferDataItemDTO transferDataItemDTO = transferDataItemDTOS.get(0);

            Set<String> custNumSet = transferDataItemDTOS.stream()
                    .map(TransferDataItemDTO::getCustNum) // 提取 cusNum
                    .collect(Collectors.toSet());

            Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNumAndTaskId =
                    transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNumSet, transferDataItemDTO.getApiCode(), new Date());

            for (TransferDataItemDTO dto : transferDataItemDTOS) {
                SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = validityPeriodsByCustNumAndTaskId.get(dto.getCustNum());
                if(syncUserValidityPeriodsBO != null){
                    List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
                    dto.setUserType(syncUsers.get(0).getUserType());
                }
            }
        }
        return transferDataDTO;
    }

}
