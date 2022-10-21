package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.JuZiRealTimePushDassService;
import com.br.marketing.client.DecodeClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.RandomUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialBatchRealTimeDataHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 桔子实时推送电销 业务实现
 *
 * @author Lizhen
 * @dateTime 2022/10/19 14:32
 */
@Service
public class JuZiRealTimePushDassServiceImpl implements JuZiRealTimePushDassService {

    final static String EXECUTE_TIME = " 10:30:00";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Resource
    YiXinTransferServiceImpl yiXinTransferService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    DecodeClient decodeClient;

    @Autowired
    ArtificialBatchRealTimeDataHandler artificialBatchRealTimeDataHandler;

    @Override
    public Result actionRealTimeDataToDx(String apiCode) {
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "3710037";
        }
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotBlank(marketingCommonConfig.getJuZiRealTimeTransferExecuteTime())) {
            execute = " " + marketingCommonConfig.getJuZiRealTimeTransferExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        String recordDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (!now.before(executeTime)) {
            //查询推送记录
            List<TransferActionFront> actionFrontList = getActionFront(apiCode, 3);
            if (actionFrontList.size()>0) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
            }
            Long frontId = yiXinTransferService.saveFrontData(apiCode, recordDate, 3);
            Map<String, List<String>> buildPushDaasMap = buildRealTimePushData(apiCode, recordDate);
            pushToDaas(apiCode, buildPushDaasMap);
            yiXinTransferService.updateFrontDataStatus(frontId, 2);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate("桔子实时任务推送电销完成");
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 推送至电销批量接口
     *
     * @return
     */
    private void pushToDaas(String apiCode, Map<String, List<String>> buildPushDaasMap) {
        String appletDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<BatchRealTimeUserDataDTO> transferData = new ArrayList<>();
        buildPushDaasMap.forEach((status, list) -> {
            list.forEach(custNum -> {
                BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
                DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
                dassImportDataDTO.setId(Long.valueOf(RandomUtils.randomStr(5)));
                dassImportDataDTO.setSource("15");
                dassImportDataDTO.setOptype("1");
                dassImportDataDTO.setOrgname("juzi");
                dassImportDataDTO.setName("1");
                if (status.equals("a") || status.equals("b")) {
                    dassImportDataDTO.setUserType("A");
                } else {
                    dassImportDataDTO.setUserType("B");
                }
                dassImportDataDTO.setUid(custNum);
                dassImportDataDTO.setPhone(decodeClient.query(custNum, "cell", "md5", ""));
                PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
                phoneSaleExtendInfo.setApiCode(apiCode);
                phoneSaleExtendInfo.setCreateTime(new Date());
                phoneSaleExtendInfo.setStatus(status);
                phoneSaleExtendInfo.setCustNum(custNum);
                phoneSaleExtendInfo.setAppletDate(appletDate);
                phoneSaleExtendInfo.setPStatus(1);
                phoneSaleExtendInfo.setUserType(dassImportDataDTO.getUserType());
                phoneSaleExtendInfo.setTransformType("1");
                batchRealTimeUserDataDTO.setDassImportDataDTO(dassImportDataDTO);
                batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
                transferData.add(batchRealTimeUserDataDTO);
            });
        });
        artificialBatchRealTimeDataHandler.call(transferData, new ProcessHandlerContext());
    }

    /**
     * 构造待推送数据
     *
     * @param apiCode
     * @param date
     * @param
     * @return
     */
    private Map<String, List<String>> buildRealTimePushData(String apiCode, String date) {
        String tcId = tableCreateService.getTcId(apiCode);
        Map<String, List<String>> pushDaasMap = new HashMap<>();
        //获取d规则的待推送数据
        getDrulePushData(tcId, date, pushDaasMap);
        //获取c规则的待推送数据
        getCrulePushData(tcId, date, pushDaasMap);
        //获取b规则的待推送数据
        getBrulePushData(apiCode, tcId, date, pushDaasMap);
        //获取a规则的待推送数据
        getArulePushData(apiCode, tcId, date, pushDaasMap);
        return pushDaasMap;

    }

    /**
     * 获取a规则的数据
     *
     * @param apiCode
     * @param tcId
     * @param date
     * @param
     * @return
     */
    private void getArulePushData(String apiCode, String tcId, String date, Map<String, List<String>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        List<String> aRulecustNum = new ArrayList<>();
        String loginTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        while (isContiue) {
            //查询a规则的转化数据
            List<MarketingTransferSyncUser> juZiARuleTransferData = marketingTransferSyncUserMapper.getJuZiARuleTransferData(tcId, date, loginTime, minId);
            if (juZiARuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiARuleTransferData.get(juZiARuleTransferData.size() - 1).getId() + 1;
            List<String> custNums = juZiARuleTransferData.stream().map(transferData -> transferData.getCustNum()).collect(Collectors.toList());
            //剔除锁定期的数据
            String applyDt = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> aRuleLockData = marketingTransferSyncUserMapper.getJuZiBOrARuleLockData(tcId, applyDt, custNums);
            custNums.removeAll(aRuleLockData);
            //a+a1+b+b1求和7天内推送3次
            String recordDate = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> pushThreeRecord = phoneSaleExtendInfoMapper.getJuziPushThreeRecord(apiCode, recordDate, custNums);
            custNums.removeAll(pushThreeRecord);
            aRulecustNum.addAll(custNums);
        }
        for (Iterator<String> iterator = aRulecustNum.iterator(); iterator.hasNext(); ) {
            String custNum = iterator.next();
            if (redisChgService.saddMember(RedisKeyConstant.juZiPushDaasCustNumKey, custNum) != 1L) {
                iterator.remove();
            }
        }
        pushDaasMap.put("a", aRulecustNum);
    }

    /**
     * 获取b规则的数据
     *
     * @param apiCode
     * @param tcId
     * @param date
     * @param
     * @return
     */
    private void getBrulePushData(String apiCode, String tcId, String date, Map<String, List<String>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        List<String> bRulecustNum = new ArrayList<>();
        String registerTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        while (isContiue) {
            //查询b规则的转化数据
            List<MarketingTransferSyncUser> juZiBRuleTransferData = marketingTransferSyncUserMapper.getJuZiBRuleTransferData(tcId, date, registerTime, minId);
            if (juZiBRuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiBRuleTransferData.get(juZiBRuleTransferData.size() - 1).getId() + 1;
            List<String> custNums = juZiBRuleTransferData.stream().map(transferData -> transferData.getCustNum()).collect(Collectors.toList());
            //剔除锁定期的数据
            String applyDt = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> bRuleLockData = marketingTransferSyncUserMapper.getJuZiBOrARuleLockData(tcId, applyDt, custNums);
            custNums.removeAll(bRuleLockData);
            //a+a1+b+b1求和7天内推送3次
            String recordDate = LocalDateTime.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> pushThreeRecord = phoneSaleExtendInfoMapper.getJuziPushThreeRecord(apiCode, recordDate, custNums);
            custNums.removeAll(pushThreeRecord);
            bRulecustNum.addAll(custNums);
        }
        for (Iterator<String> iterator = bRulecustNum.iterator(); iterator.hasNext(); ) {
            String custNum = iterator.next();
            if (redisChgService.saddMember(RedisKeyConstant.juZiPushDaasCustNumKey, custNum) != 1L) {
                iterator.remove();
            }
        }
        pushDaasMap.put("b", bRulecustNum);
    }

    /**
     * 获取c规则的数据
     *
     * @param tcId
     * @param date
     * @param
     * @return
     */
    private void getCrulePushData(String tcId, String date, Map<String, List<String>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        List<String> cRulecustNum = new ArrayList<>();
        while (isContiue) {
            //查询c规则的转化数据
            List<MarketingTransferSyncUser> juZiCRuleTransferData = marketingTransferSyncUserMapper.getJuZiCRuleTransferData(tcId, date, minId);
            if (juZiCRuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiCRuleTransferData.get(juZiCRuleTransferData.size() - 1).getId() + 1;
            List<String> custNums = juZiCRuleTransferData.stream().map(transferData -> transferData.getCustNum()).collect(Collectors.toList());
            String applyLoanTime = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> cRuleLockData = marketingTransferSyncUserMapper.getJuZiCRuleLockData(tcId, applyLoanTime, custNums);
            //剔除锁定期的数据
            custNums.removeAll(cRuleLockData);
            cRulecustNum.addAll(custNums);
        }
        for (Iterator<String> iterator = cRulecustNum.iterator(); iterator.hasNext(); ) {
            String custNum = iterator.next();
            if (redisChgService.saddMember(RedisKeyConstant.juZiPushDaasCustNumKey, custNum) != 1L) {
                iterator.remove();
            }
        }
        pushDaasMap.put("c", cRulecustNum);

    }

    /**
     * 获取d规则的数据
     *
     * @param tcId
     * @param date
     * @param
     * @return
     */
    private Map<String, List<String>> getDrulePushData(String tcId, String date, Map<String, List<String>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        List<String> dRulecustNum = new ArrayList<>();
        while (isContiue) {
            //查询d规则的转化数据
            List<MarketingTransferSyncUser> juZiDRuleTransferData = marketingTransferSyncUserMapper.getJuZiDRuleTransferData(tcId, date, minId);
            if (juZiDRuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiDRuleTransferData.get(juZiDRuleTransferData.size() - 1).getId() + 1;
            List<String> custNums = juZiDRuleTransferData.stream().map(transferData -> transferData.getCustNum()).distinct().collect(Collectors.toList());
            String lentTime = LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> dRuleLockData = marketingTransferSyncUserMapper.getJuZiDRuleLockData(tcId, lentTime, custNums);
            //剔除锁定期的数据
            custNums.removeAll(dRuleLockData);
            dRulecustNum.addAll(custNums);
        }
        pushDaasMap.put("d", dRulecustNum);
        redisChgService.sadd(RedisKeyConstant.juZiPushDaasCustNumKey, dRulecustNum);
        redisChgService.expire(RedisKeyConstant.juZiPushDaasCustNumKey, getKeyExpiration());
        return pushDaasMap;
    }


    /**
     * 获取当前时间到第二天凌晨的秒
     *
     * @dateTime 2021/10/19 9:21
     */
    private int getKeyExpiration() {
        final LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }

    private List<TransferActionFront> getActionFront(String apiCode, int actionType) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);
        return transferActionFrontMapper.selectByExample(example);
    }
}
