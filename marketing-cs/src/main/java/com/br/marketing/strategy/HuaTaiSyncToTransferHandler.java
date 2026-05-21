package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.rule.huatai.dto.HuaTaiTransferAssembleDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class HuaTaiSyncToTransferHandler extends AbstractExternalInterfaceHandler<HuaTaiTransferAssembleDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public JSONObject call(List<HuaTaiTransferAssembleDTO> resultList, ProcessHandlerContext context) {
        if (!CollectionUtils.isEmpty(resultList)) {
            String apiCode = context.getApiCode();
            List<TransferDataItemDTO> dataItems = collectTransferDataItems(resultList);
            PushTransferDataDetailDTO pushDto = buildPushTransferData(apiCode, dataItems);
            Result pushResult = methodRetryHandlerService.pushTransferByRetry(pushDto, null);
            log.warn("华泰转化 push apiCode:{}, size:{}, code:{}, success:{}, msg:{}",
                    apiCode, dataItems.size(), pushResult.getCode(), pushResult.isSuccess(), pushResult.getMessage());
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.HUATAI_SYNC_TO_TRANSFER;
    }

    /**
     * 从规则结果集收集全部转化明细。
     */
    private List<TransferDataItemDTO> collectTransferDataItems(List<HuaTaiTransferAssembleDTO> resultList) {
        List<TransferDataItemDTO> dataItems = new ArrayList<>();
        for (HuaTaiTransferAssembleDTO item : resultList) {
            if (item == null || item.getSyncUser() == null) {
                continue;
            }
            dataItems.add(convertTransferDataItem(item.getSyncUser()));
        }
        return dataItems;
    }

    private PushTransferDataDetailDTO buildPushTransferData(String apiCode, List<TransferDataItemDTO> transferDataItems) {
        if (apiCode == null) {
            apiCode = transferDataItems.get(0).getApiCode();
        }
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItems);
        int randomNumber = 10000 + ThreadLocalRandom.current().nextInt(90000);
        String requestId = apiCode + "_" + System.currentTimeMillis() + "_" + randomNumber;
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }

    /**
     * 单条 syncUser 转为一条转化明细。
     */
    private TransferDataItemDTO convertTransferDataItem(MarketingSyncUser syncUser) {
        TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
        transferDataItemDTO.setApiCode(syncUser.getApiCode());
        transferDataItemDTO.setCustNum(syncUser.getCustNum());
        transferDataItemDTO.setUserType(marketingCommonConfig.getHuaTaiSyncToTransferUserType());
        transferDataItemDTO.setCaseEffective("0");
        transferDataItemDTO.setInsertTime(DATE_FORMAT.format(
                syncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        JSONObject jsonObject = new JSONObject();
        String phone = BrCipherMaker.getInstance().decode(syncUser.getCell());
        jsonObject.put("cellExposed", phone);
        transferDataItemDTO.setReserveField1(JSON.toJSONString(jsonObject));
        return transferDataItemDTO;
    }
}
