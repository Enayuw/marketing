package com.br.marketing.monkey.service.syj.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.suiyiji.SuiyijiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.monkey.service.syj.SuiYiJiBlackService;
import com.br.marketing.service.PushInfoService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author zhen.Li1
 * @Date 2025/12/4
 */
@Slf4j
@Service
public class SuiYiJiBlackSericeImpl implements SuiYiJiBlackService {

    @Resource
    private SuiyijiClient suiyijiClient;

    @Resource
    private PushInfoService pushInfoService;

    @Override
    public void blackPushTransfer(String apiCode) {

        //查询数据
        Result<String> result = suiyijiClient.getBlackList();
        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return;
        }
        log.warn("随意记黑名单查询结果result={}",JSON.toJSONString(result));
        String jsonData = result.getData();
        List<String> blackList = JSON.parseArray(jsonData, String.class);
        List<List<String>> splitLists = Lists.partition(blackList, 500);
        for (List<String> blackDatas : splitLists) {
            List<TransferDataItemDTO> dataItems = new ArrayList<>();
            for (String cell : blackDatas) {
                TransferDataItemDTO item = new TransferDataItemDTO();
                UserValidator userValidator = new UserValidator(0);
                //明文进行MD5处理
                if (userValidator.validatePhone(cell)) {
                    cell = Md5Utils.cell32(cell);
                }
                item.setCustNum(cell);
                item.setUserType("1");
                JSONObject reserveField1 = new JSONObject();
                reserveField1.put("isBlack", "1");
                item.setReserveField1(reserveField1.toJSONString());
                item.setApiCode(apiCode);
                dataItems.add(item);
            }
            PushTransferDataDetailDTO dto = buildPushTransferDataDTO(apiCode, dataItems);
            pushInfoService.pushTransferByRetry(dto, null);
        }
    }

    /**
     * 构建推送转化数据DTO
     */
    private PushTransferDataDetailDTO buildPushTransferDataDTO(String apiCode, List<TransferDataItemDTO> dataItems) {
        TransferDataDTO<TransferDataItemDTO> transferDataDTO = new TransferDataDTO<>();
        // requestId格式: apicode + 时间戳(毫秒级) + 五位以上随机数
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomNum = RandomStringUtils.randomNumeric(5);
        String requestId = apiCode + timestamp + randomNum;
        transferDataDTO.setRequestId(requestId);

        transferDataDTO.setDataItems(dataItems);

        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));

        return dto;
    }
}
