package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.IFenqileService;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 分期乐业务
 *
 * @author Guo Zeqiang
 * @dateTime 2023-03-13 10:08
 */
@Service
@Slf4j
public class FenqileServiceImpl implements IFenqileService {

    @Resource
    private MarketingUserMapper marketingUserMapper;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public Integer periodPushDecision(String apiCode, int day, String strategyCode, LocalDate localDate
            , String startTimeStr, String endTimeStr) {
        String localDateStr = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<TransferActionFront> actionRow = getActionRow(apiCode, day, localDateStr);
        int page = 0;
        int sum = 0;
        if (day == 0 || actionRow.size() < 1) {
            Date date = null;
            if (actionRow.size() > 0) {
                TransferActionFront actionFront = actionRow.get(0);
                if (StringUtils.isNotBlank(actionFront.getRemark())) {
                    Date dateOld = new Date();
                    dateOld.setTime(Long.parseLong(actionFront.getRemark()));
                    startTimeStr = dateOld.toInstant().atZone(ZoneId.systemDefault())
                            .plus(1, ChronoUnit.SECONDS)
                            .format(DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT_SSS));
                }
            }
            try {
                while (true) {
                    List<MarketingSyncUser> l = marketingUserMapper.findCustNumCellUserTypeScoreDatePage(apiCode
                            , startTimeStr, endTimeStr, page);
                    if (CollectionUtils.isEmpty(l)) {
                        break;
                    }
                    date = l.get(l.size() - 1).getAppletTime();
                    page++;
                    sum += makeData(apiCode, l, localDateStr, strategyCode);
                    if (l.size() < 2000) {
                        break;
                    }
                }
                saveOrUpdate(day, apiCode, localDateStr, date, actionRow);
            } catch (Exception e) {
                saveOrUpdate(day, apiCode, localDateStr, date, actionRow);
                log.error(e.getMessage(), e);
                return null;
            }
        } else {
            return null;
        }
        return sum;
    }


    private List<TransferActionFront> getActionRow(String apiCode, int day
            , String localDateStr) {
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andActionTypeEqualTo(day)
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(localDateStr)
                .andIsDelEqualTo(1);
        List<TransferActionFront> list = transferActionFrontMapper.selectByExample(frontExample);
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : list;
    }

    /**
     * 2023-03-13 17:42
     * 组装数据
     */
    private int makeData(String apiCode, List<MarketingSyncUser> list, String batchNumber, String strategyCode) {
        List<PushMarketingUserDetailDTO> dtoList = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        int pageSize = 500;
        int number = list.size() / pageSize;
        if (list.size() % pageSize != 0) {
            number++;
        }
        int sum = 0;
        for (MarketingSyncUser syncUser : list) {
            PushMarketingUserDetailDTO dto = new PushMarketingUserDetailDTO();
            dto.setPhone(syncUser.getCell());
            dto.setCaseNumber(syncUser.getCustNum());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userType", syncUser.getUserType());
            jsonObject.put("scoreDate", syncUser.getCreateTime() != null
                    ? syncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
            dto.setVariables(jsonObject);
            dtoList.add(dto);
            ids.add(syncUser.getId());
            int size = dtoList.size();
            if (size == pageSize || number == 1) {
                number--;
                Result<?> result = pushDecision(dtoList, ids, batchNumber, strategyCode, apiCode);
                if (result != null && ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    sum += dtoList.size();
                }
            }
        }
        return sum;
    }

    /**
     * 2023-03-13 17:43
     * 发送数据
     */
    private Result<?> pushDecision(List<PushMarketingUserDetailDTO> dtoList
            , List<Long> ids, String batchNumber, String strategyCode, String apiCode) {
        SecureRandom secureRandom = new SecureRandom();
        PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
        taskInfoDTO.setData(dtoList);
        taskInfoDTO.setAccessNumber(UUID.randomUUID() + String.format("%05d", secureRandom.nextInt(10000)));
        taskInfoDTO.setMethod("caseAdd");
        taskInfoDTO.setBatchNumber(batchNumber);
        taskInfoDTO.setStrategyCode(strategyCode);
        PushMarketingUserDTO<PushMarketingUserTaskInfoDTO> pushMarketingUserDTO = new PushMarketingUserDTO<>();
        pushMarketingUserDTO.setApiCode(apiCode);
        pushMarketingUserDTO.setJsonData(taskInfoDTO);
        PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
        retryByRuleDTO.setIds(ids);
        retryByRuleDTO.setInfoId(null);
        retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
        try {
            return methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void saveOrUpdate(int day, String apiCode, String localDateStr, Date date, List<TransferActionFront> actionRow) {
        if (actionRow.size() < 1) {
            TransferActionFront actionFront = new TransferActionFront();
            actionFront.setActionType(day);
            actionFront.setStatus(2);
            actionFront.setCreateTime(new Date());
            actionFront.setIsDel(1);
            actionFront.setApiCode(apiCode);
            actionFront.setActionData(localDateStr);
            if (day == 0 && date != null) {
                actionFront.setRemark(date.getTime() + "");
            }
            transferActionFrontMapper.insertSelective(actionFront);
        } else if (date != null) {
            TransferActionFront actionFront = actionRow.get(0);
            long time = date.getTime();
            if (actionFront.getRemark() != null && Long.parseLong(actionFront.getRemark()) == (time)) {
                return;
            }
            TransferActionFront actionNew = new TransferActionFront();
            actionNew.setId(actionFront.getId());
            actionNew.setRemark(time + "");
            transferActionFrontMapper.updateByPrimaryKeySelective(actionNew);
        }
    }
}
