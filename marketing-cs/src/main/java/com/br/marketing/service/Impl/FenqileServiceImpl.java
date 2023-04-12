package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.MarketingSyncInfoExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.IFenqileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    private final static DateTimeFormatter DTF = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT_SSS);

    @Override
    public Integer periodPushDecision(String apiCode, int day, String strategyCode, LocalDate localDate
            , String startTimeStr, String endTimeStr) {
        MarketingSyncInfoExample example = new MarketingSyncInfoExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo(1)
                .andCreateTimeGreaterThanOrEqualTo(Date.from(LocalDate.now().atStartOfDay()
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .andCreateTimeLessThanOrEqualTo(new Date());
        int count = marketingSyncInfoMapper.countByExample(example);
        if (count != 0 && day == 0) {
            return 0;
        }
        String localDateStr = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String basicDateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        List<TransferActionFront> actionRow = getActionRow(apiCode, day, localDateStr);
        int sum = 0;
        if (day == 0 || actionRow.size() < 1) {
            int page = 0;
            int sumOld = 0;
            Date date = null;
            Date exceptionDate = null;
            if (actionRow.size() > 0) {
                TransferActionFront actionFront = actionRow.get(0);
                if (StringUtils.isNotBlank(actionFront.getRemark())) {
                    String[] split = actionFront.getRemark().split(";");
                    Date dateOld = new Date();
                    dateOld.setTime(Long.parseLong(split[0]));
                    if (split.length > 1) {
                        sumOld = Integer.parseInt(split[1]);
                    }
                    LocalDateTime localDateTime = dateOld.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    if (localDateTime.compareTo(LocalDateTime.parse(startTimeStr, DTF)) > 0) {
                        startTimeStr = localDateTime.format(DTF);
                    } else {
                        return sum;
                    }
                }
            }
            HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
            Integer encType = pushCellEncPolicy == null ? ScoreThreeKeyEncryptEnum.md5.getValue()
                    : pushCellEncPolicy.getOrDefault(apiCode, ScoreThreeKeyEncryptEnum.md5.getValue());
            try {
                while (true) {
                    List<MarketingSyncUser> l = marketingUserMapper.findCustNumCellUserTypeScoreDatePage(apiCode
                            , startTimeStr, endTimeStr, page);
                    if (CollectionUtils.isEmpty(l)) {
                        break;
                    }
                    date = l.get(l.size() - 1).getCreateTime();
                    page++;
                    sum += makeData(apiCode, l, basicDateStr, strategyCode, encType, date);
                    exceptionDate = date;
                    if (l.size() < 2000) {
                        break;
                    }
                }
                saveOrUpdate(day, apiCode, localDateStr, date, actionRow, sum + sumOld);
            } catch (Exception e) {
                saveOrUpdate(day, apiCode, localDateStr, exceptionDate, actionRow, sum + sumOld);
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
        frontExample.setOrderByClause("create_time desc");
        List<TransferActionFront> list = transferActionFrontMapper.selectByExample(frontExample);
        return CollectionUtils.isEmpty(list) ? Collections.emptyList() : list;
    }

    /**
     * 2023-03-13 17:42
     * 组装数据
     */
    private int makeData(String apiCode, List<MarketingSyncUser> list, String batchNumber, String strategyCode
            , Integer encType, Date date) {
        List<PushMarketingUserDetailDTO> dtoList = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        int pageSize = 500;
        int s = list.size();
        int sum = 0;
        for (MarketingSyncUser syncUser : list) {
            PushMarketingUserDetailDTO dto = new PushMarketingUserDetailDTO();
            String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
            if (ScoreThreeKeyEncryptEnum.md5.getValue().equals(encType)) {
                dto.setPhone(DigestUtils.md5DigestAsHex(cell.getBytes()));
            } else if (ScoreThreeKeyEncryptEnum.sha256.getValue().equals(encType)) {
                dto.setPhone(Sha256Util.getSHA256Encrypt(cell));
            } else {
                dto.setPhone(cell);
            }
            dto.setCaseNumber(syncUser.getCustNum());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userType", syncUser.getUserType());
            jsonObject.put("scoreDate", syncUser.getCreateTime() != null
                    ? syncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ISO_LOCAL_DATE) : "");
            dto.setVariables(jsonObject);
            dtoList.add(dto);
            ids.add(syncUser.getId());
            s--;
            int size = dtoList.size();
            if (size == pageSize || s == 0) {
                Result<?> result = pushDecision(dtoList, ids, batchNumber, strategyCode, apiCode, date);
                if (result != null && ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    sum += size;
                }
                dtoList.clear();
                ids.clear();
            }
        }
        return sum;
    }

    /**
     * 2023-03-13 17:43
     * 发送数据
     */
    private Result<?> pushDecision(List<PushMarketingUserDetailDTO> dtoList
            , List<Long> ids, String batchNumber, String strategyCode, String apiCode, Date date) {
        SecureRandom secureRandom = new SecureRandom();
        PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
        taskInfoDTO.setData(dtoList);
        taskInfoDTO.setAccessNumber(System.nanoTime() + String.format("%05d", secureRandom.nextInt(10000)));
        taskInfoDTO.setMethod("caseAdd");
        taskInfoDTO.setBatchNumber(batchNumber + "_" + date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .format(DateTimeFormatter.BASIC_ISO_DATE) + "_" + apiCode);
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

    private void saveOrUpdate(int day, String apiCode, String localDateStr, Date date
            , List<TransferActionFront> actionRow, int sum) {
        if (actionRow.size() < 1) {
            TransferActionFront actionFront = new TransferActionFront();
            actionFront.setActionType(day);
            actionFront.setStatus(2);
            actionFront.setCreateTime(new Date());
            actionFront.setIsDel(1);
            actionFront.setApiCode(apiCode);
            actionFront.setActionData(localDateStr);
            if (day == 0) {
                actionFront.setRemark((date != null ? date.getTime()
                        : Date.from(LocalDateTime.now().minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant())
                        .getTime()) + ";" + sum);
            } else {
                actionFront.setRemark(String.valueOf(sum));
            }
            transferActionFrontMapper.insertSelective(actionFront);
        } else if (date != null) {
            TransferActionFront actionFront = actionRow.get(0);
            long time = date.getTime();
            if (actionFront.getRemark() != null && actionFront.getRemark().startsWith(String.valueOf(time))) {
                return;
            }
            TransferActionFront actionNew = new TransferActionFront();
            actionNew.setId(actionFront.getId());
            actionNew.setRemark(time + ";" + sum);
            transferActionFrontMapper.updateByPrimaryKeySelective(actionNew);
        }
    }
}
