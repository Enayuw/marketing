package com.br.marketing.rule.fenqihappy;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.CaseUserServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * 消费延迟队列，推电销
 */
@Service
@Slf4j
public class FenQiHappyCallRecordToDass implements AssembleData<RealTimeUserDataDTO> {

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    RedisChgService redisChgService;


    @Autowired
    CaseUserServiceImpl caseUserService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        Date day = dto.getCreateTime();
        SimpleDateFormat dfDay = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dfSecond = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        MarketingSyncUser marketingSyncUser = marketingSyncInfoMapper.getNewestByCusnumAndStatus(dto.getApiCode(), dto.getCaseNum());
        if (marketingSyncUser == null) {
            log.warn("上传数据表中(apicode=%s)不存在 custNum=%s 的数据！", dto.getApiCode(), dto.getCaseNum());
            return null;
        }

        HashMap<String, Long> fenqiHappyPushDassConfig = marketingCommonConfig.getFenqiHappyPushDassConfig();
        if (fenqiHappyPushDassConfig == null || fenqiHappyPushDassConfig.size() <= 0) {
            fenqiHappyPushDassConfig = new HashMap<String, Long>();
            fenqiHappyPushDassConfig.put("1", 30L);
            fenqiHappyPushDassConfig.put("3", 30L);
            fenqiHappyPushDassConfig.put("5", 30L);
        }
        if (!fenqiHappyPushDassConfig.keySet().contains(marketingSyncUser.getUserType())) {
            return null;
        }
        Long soleDays = fenqiHappyPushDassConfig.get(marketingSyncUser.getUserType());
        String lockValue = UUID.randomUUID().toString();
        Date from = Date.from(LocalDate.now().minusDays(soleDays - 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(LocalDate.now().plusDays(1L).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Boolean lock = addLock(context.getApiCode(), marketingSyncUser.getCell(), lockValue);
        if (lock) {
            RealTimeUserDataDTO realTimeUserDataDTO = null;
            PhoneSaleExtendInfoExample infoExample = new PhoneSaleExtendInfoExample();
            infoExample.createCriteria().andCellEqualTo(marketingSyncUser.getCell())
                    .andApiCodeEqualTo(context.getApiCode())
                    .andCreateTimeGreaterThanOrEqualTo(from)
                    .andCreateTimeLessThan(end);
            int i = phoneSaleExtendInfoMapper.countByExample(infoExample);
            if (i <= 0) {

                realTimeUserDataDTO = new RealTimeUserDataDTO();
                DassSingleImportAdapDTO dassSingleImportAdapDTO = new DassSingleImportAdapDTO();
                PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
                phoneSaleExtendInfo.setApiCode(context.getApiCode());
                phoneSaleExtendInfo.setCustNum(marketingSyncUser.getCustNum());
                phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());
                phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
                phoneSaleExtendInfo.setUserType(marketingSyncUser.getUserType());
                phoneSaleExtendInfo.setAppletDate(new SimpleDateFormat("yyyy-MM-dd").format(dto.getCreateTime()));
                phoneSaleExtendInfo.setAppletTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dto.getCreateTime()));
                phoneSaleExtendInfo.setStatus("a");
                phoneSaleExtendInfo.setPStatus(1);
                phoneSaleExtendInfo.setCreateTime(new Date());
                phoneSaleExtendInfo.setUpdateTime(new Date());
                phoneSaleExtendInfo.setSourceId(dto.getId());
                phoneSaleExtendInfoMapper.insertSelective(phoneSaleExtendInfo);

                DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();//单条
                dassSingleImportDataDTO.setUid(dto.getCaseNum());
                String s = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
                dassSingleImportDataDTO.setPhone(s);
                dassSingleImportDataDTO.setName("1");
                dassSingleImportDataDTO.setOrgname("lexin");
                dassSingleImportDataDTO.setUserType("97");
                dassSingleImportDataDTO.setSource("8");
                dassSingleImportDataDTO.setOptype("1");
                dassSingleImportDataDTO.setType("2");

                dassSingleImportAdapDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
                dassSingleImportAdapDTO.setExtendInfo(context.getApiCode().concat(":通话明细:").concat(dto.getId().toString()));
                realTimeUserDataDTO.setDassSingleImportAdapDTO(dassSingleImportAdapDTO);
                realTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
            }
            removeLock(context.getApiCode(), marketingSyncUser.getCell(), lockValue);
            return realTimeUserDataDTO;
        }
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO bo = (CallRecordBO) transmitFact;

            if (bo == null
                    || bo.getDetail() == null
                    || StringUtils.isBlank(bo.getDetail().getIntentionGrade())
                    || (!bo.getDetail().getIntentionGrade().toUpperCase().contains("A")&&!bo.getDetail().getIntentionGrade().toUpperCase().contains("B"))) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "FenQiHappy_CallRecordData_PhoneSale";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_LOG.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }


    Boolean addLock(String apiCode, String cell, String val) {
        String key = RedisKeyConstant.fenqiHappyPushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(cell);
        return redisChgService.setnx(key, val, 5);
    }

    void removeLock(String apiCode, String cell, String val) {
        String key = RedisKeyConstant.fenqiHappyPushDx.concat(":")
                .concat(apiCode).concat(":")
                .concat(cell);
        if (redisChgService.exists(key) && val.equals(redisChgService.get(key))) {
            redisChgService.del(key);
        }
    }

}
