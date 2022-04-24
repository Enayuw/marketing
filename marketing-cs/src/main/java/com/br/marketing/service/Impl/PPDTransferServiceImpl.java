package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IPPDTransferService;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PPDTransferServiceImpl implements IPPDTransferService {

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    RabbitMqProducter producter;

    @Resource
    YiXinTransferServiceImpl yiXinTransferService;

    @Override
    public Result actionPPDToDx(String apiCodes) {

        List<String> apiCodeList;
        if (StringUtils.isEmpty(apiCodes)) {
            apiCodeList = Lists.newArrayList("3710014", "3710015");
        } else {
            apiCodeList = Arrays.asList(apiCodes.split(","));
        }
        String endDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String startDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        //region check 1.查询推送记录；2.查询推送记录的状态；3.查询数据处理情况
        Result<TransferActionFront> frontDataRes = yiXinTransferService.getFrontData(StringUtils.join(apiCodeList, ","), endDate, 3);
        if (!ResultCode.SUCCESS.getValue().equals(frontDataRes.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(frontDataRes.getMessage());
        }
        TransferActionFront frontData = frontDataRes.getData();
        if (frontData != null && new Integer(2).equals(frontData.getStatus())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
        }
        Long frontId = yiXinTransferService.saveFrontData(StringUtils.join(apiCodeList, ","), endDate, 3);
        String tcId = tableCreateService.getTcId(apiCodeList.get(0));
        for (String apiCode : apiCodeList) {
            Long minId = null;
            Boolean isContiue = Boolean.TRUE;
            while (isContiue) {
                List<MarketingTransferSyncUser> marketingTransferSyncUsers = marketingTransferSyncUserMapper.getTransferByApiCodeAndCreateTime(apiCode, tcId, startDate, endDate, minId);
                if (marketingTransferSyncUsers.size() <= 0) {
                    isContiue = Boolean.FALSE;
                    continue;
                }
                minId = marketingTransferSyncUsers.get(marketingTransferSyncUsers.size() - 1).getId() + 1;
                List<Long> ids = marketingTransferSyncUsers.stream().map(MarketingTransferSyncUser::getId).collect((Collectors.toList()));
                JSONObject paramMessage = new JSONObject();
                paramMessage.put("apiCode", apiCode);
                paramMessage.put("tcId", tcId);
                paramMessage.put("ids", ids);
                MqFact mqFact = new MqFact();
                mqFact.setIncludeRules(Sets.newHashSet("PPD_TransferData_ArtificialTransfer"));
                mqFact.setSource(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode());
                mqFact.setMessage(JSONObject.toJSONString(paramMessage));
                producter.sendToUniversalTransferQueue(mqFact);
            }
        }
        yiXinTransferService.updateFrontDataStatus(frontId, 2);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }
}
