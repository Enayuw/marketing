package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @ClassName TransferFieldProcessByZhaoLianFactory
 * @Description 招联转化数据清洗
 * @Author kongbx
 * @Date 2025/5/27 16:34
 */
@Service
@Slf4j
public class TransferFieldProcessByZhaoLianFactory  implements TransferFieldProcessFactory {


    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public String customerName() {
        return "zhaolian";
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
                    MarketingSyncUser marketingSyncUser = syncUsers.get(0);
                    log.warn("招联转化清洗匹配有效上传数据：" + JSONObject.toJSONString(marketingSyncUser));
                    dto.setUserType(marketingSyncUser.getUserType());
                    JSONObject jsonObject = JSONObject.parseObject(dto.getReserveField1());
                    jsonObject.put("cell",marketingSyncUser.getCellSha256());
                    dto.setReserveField1(jsonObject.toJSONString());
                }
            }
        }
        return transferDataDTO;
    }

}