package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.JuZiRealTimePushDassService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.RandomUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialBatchRealTimeDataHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
@Slf4j
public class JuZiRealTimePushDassServiceImpl implements JuZiRealTimePushDassService {

    final static String EXECUTE_TIME = " 10:30:00";

    final static String APPLYDT = "applyDt";

    final static String APPLYLOANTIME = "applyLoanTime";

    final static String LENTTIME = "lentTime";

    final static String PUSHNUM = "pushNum";

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

    @Autowired
    ArtificialBatchRealTimeDataHandler artificialBatchRealTimeDataHandler;

    @Value("${api.dass.aesKey:}")
    private String aesKey;

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
            if (actionFrontList.size() > 0) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
            }
            //判断是否传输转化数据
            String tcId = tableCreateService.getTcId(apiCode);
            if (marketingTransferSyncUserMapper.getTransferDataCount(tcId, apiCode, recordDate) == 0) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setDate("今日未传输转化数据");
            }
            Long frontId = yiXinTransferService.saveFrontData(apiCode, recordDate, 3);
            Map<String, Map<String, MarketingTransferSyncUser>> buildPushDaasMap = buildRealTimePushData(apiCode, recordDate);
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
    private void pushToDaas(String apiCode, Map<String, Map<String, MarketingTransferSyncUser>> buildPushDaasMap) {
        List<BatchRealTimeUserDataDTO> transferData = new ArrayList<>();
        buildPushDaasMap.forEach((status, map) -> {
            map.forEach((custNum, marketingTransferSyncUser) -> {
                BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
                DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
                dassImportDataDTO.setId(marketingTransferSyncUser.getId());
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
                String phoneDecode = RpcClientProxy.decode(custNum, "cell", "md5", "");
                //解密失败直接丢弃
                if (StringUtils.isEmpty(phoneDecode)) {
                    return;
                }
                dassImportDataDTO.setPhone(AESUtil.aesEncrypty(phoneDecode, aesKey));
                PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
                phoneSaleExtendInfo.setApiCode(apiCode);
                phoneSaleExtendInfo.setCreateTime(new Date());
                phoneSaleExtendInfo.setStatus(status);
                phoneSaleExtendInfo.setCustNum(custNum);
                phoneSaleExtendInfo.setAppletDate(marketingTransferSyncUser.getRequestData());
                phoneSaleExtendInfo.setAppletTime(marketingTransferSyncUser.getRequestTime());
                phoneSaleExtendInfo.setPStatus(1);
                phoneSaleExtendInfo.setUserType(marketingTransferSyncUser.getUserType());
                phoneSaleExtendInfo.setTransformType("1");
                phoneSaleExtendInfo.setSourceId(dassImportDataDTO.getId());
                phoneSaleExtendInfo.setDxType(dassImportDataDTO.getUserType());
                batchRealTimeUserDataDTO.setDassImportDataDTO(dassImportDataDTO);
                batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(phoneSaleExtendInfo);
                transferData.add(batchRealTimeUserDataDTO);
            });
        });
        log.warn("桔子实时推送电销总数据量={}", transferData.size());
        if (!CollectionUtils.isEmpty(transferData)) {
            artificialBatchRealTimeDataHandler.call(transferData, new ProcessHandlerContext());
        }
    }

    /**
     * 构造待推送数据
     *
     * @param apiCode
     * @param date
     * @param
     * @return
     */
    private Map<String, Map<String, MarketingTransferSyncUser>> buildRealTimePushData(String apiCode, String date) {
        String tcId = tableCreateService.getTcId(apiCode);
        Map<String, Map<String, MarketingTransferSyncUser>> pushDaasMap = new HashMap<>();
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
    private void getArulePushData(String apiCode, String tcId, String date, Map<String, Map<String, MarketingTransferSyncUser>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        Map<String, MarketingTransferSyncUser> aRuleTransferData = new HashMap<>();
        String loginTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        while (isContiue) {
            //查询a规则的转化数据
            List<MarketingTransferSyncUser> juZiARuleTransferData = marketingTransferSyncUserMapper.getJuZiARuleTransferData(tcId, date, loginTime, minId);
            if (juZiARuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiARuleTransferData.get(juZiARuleTransferData.size() - 1).getId() + 1;
            Map<String, MarketingTransferSyncUser> transferSyncUserMap = getTransferSyncUserMap(juZiARuleTransferData);
            //剔除锁定期的数据
            int applyDtDays = 30;
            int recordDateDays = 6;
            if (!CollectionUtils.isEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig())
                    && (StringUtils.isNotEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig().get(APPLYDT)))) {
                applyDtDays = Integer.valueOf(marketingCommonConfig.getJuZiRealTimeLockConfig().get(APPLYDT));
            } //默认30天
            String applyDt = LocalDateTime.now().minusDays(applyDtDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> aRuleLockData = marketingTransferSyncUserMapper.getJuZiBOrARuleLockData(tcId, applyDt, transferSyncUserMap.keySet());
            transferSyncUserMap.keySet().removeAll(aRuleLockData);
            //a+a1+b+b1求和7天内推送3次
            if (!CollectionUtils.isEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig())
                    && (StringUtils.isNotEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig().get(PUSHNUM)))) {
                recordDateDays = Integer.valueOf(marketingCommonConfig.getJuZiRealTimeLockConfig().get(PUSHNUM));
            }//默认7天
            String recordDate = LocalDateTime.now().minusDays(recordDateDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!CollectionUtils.isEmpty(transferSyncUserMap)) {
                List<String> pushThreeRecord = phoneSaleExtendInfoMapper.getJuziPushThreeRecordtikv_(apiCode, recordDate, new ArrayList<String>(transferSyncUserMap.keySet()));
                transferSyncUserMap.keySet().removeAll(pushThreeRecord);
            }
            aRuleTransferData.putAll(transferSyncUserMap);
        }
        for (Iterator<String> iterator = aRuleTransferData.keySet().iterator(); iterator.hasNext(); ) {
            String custNum = iterator.next();
            if (redisChgService.saddMember(RedisKeyConstant.juZiPushDaasCustNumKey, custNum) != 1L) {
                iterator.remove();
            }
        }
        //第二天凌晨失效
        if (redisChgService.exists(RedisKeyConstant.juZiPushDaasCustNumKey)) {
            redisChgService.expire(RedisKeyConstant.juZiPushDaasCustNumKey, getKeyExpiration());
        }
        log.warn("桔子实时A规则推送电销数据量={}", aRuleTransferData.size());
        pushDaasMap.put("a", aRuleTransferData);
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
    private void getBrulePushData(String apiCode, String tcId, String date, Map<String, Map<String, MarketingTransferSyncUser>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        Map<String, MarketingTransferSyncUser> bRuleTransferData = new HashMap<>();
        String registerTime = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        while (isContiue) {
            //查询b规则的转化数据
            List<MarketingTransferSyncUser> juZiBRuleTransferData = marketingTransferSyncUserMapper.getJuZiBRuleTransferData(tcId, date, registerTime, minId);
            if (juZiBRuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiBRuleTransferData.get(juZiBRuleTransferData.size() - 1).getId() + 1;
            Map<String, MarketingTransferSyncUser> transferSyncUserMap = getTransferSyncUserMap(juZiBRuleTransferData);
            //剔除锁定期的数据
            int applyDtDays = 30;
            int recordDateDays = 6;
            if (!CollectionUtils.isEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig())
                    && (StringUtils.isNotEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig().get(APPLYDT)))) {
                applyDtDays = Integer.valueOf(marketingCommonConfig.getJuZiRealTimeLockConfig().get(APPLYDT));
            }//默认30天
            String applyDt = LocalDateTime.now().minusDays(applyDtDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> bRuleLockData = marketingTransferSyncUserMapper.getJuZiBOrARuleLockData(tcId, applyDt, transferSyncUserMap.keySet());
            transferSyncUserMap.keySet().removeAll(bRuleLockData);
            //a+a1+b+b1求和7天内推送3次
            if (!CollectionUtils.isEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig())
                    && (StringUtils.isNotEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig().get(PUSHNUM)))) {
                recordDateDays = Integer.valueOf(marketingCommonConfig.getJuZiRealTimeLockConfig().get(PUSHNUM));
            }//默认7天
            String recordDate = LocalDateTime.now().minusDays(recordDateDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (!CollectionUtils.isEmpty(transferSyncUserMap)) {
                List<String> pushThreeRecord = phoneSaleExtendInfoMapper.getJuziPushThreeRecordtikv_(apiCode, recordDate, new ArrayList<String>(transferSyncUserMap.keySet()));
                transferSyncUserMap.keySet().removeAll(pushThreeRecord);
            }
            bRuleTransferData.putAll(transferSyncUserMap);
        }
        for (Iterator<String> iterator = bRuleTransferData.keySet().iterator(); iterator.hasNext(); ) {
            String custNum = iterator.next();
            if (redisChgService.saddMember(RedisKeyConstant.juZiPushDaasCustNumKey, custNum) != 1L) {
                iterator.remove();
            }
        }
        log.warn("桔子实时B规则推送电销数据量={}", bRuleTransferData.size());
        pushDaasMap.put("b", bRuleTransferData);
    }

    /**
     * 获取c规则的数据
     *
     * @param tcId
     * @param date
     * @param
     * @return
     */
    private void getCrulePushData(String tcId, String date, Map<String, Map<String, MarketingTransferSyncUser>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        Map<String, MarketingTransferSyncUser> cRuleTransferData = new HashMap<>();
        while (isContiue) {
            //查询c规则的转化数据
            List<MarketingTransferSyncUser> juZiCRuleTransferData = marketingTransferSyncUserMapper.getJuZiCRuleTransferData(tcId, date, minId);
            if (juZiCRuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiCRuleTransferData.get(juZiCRuleTransferData.size() - 1).getId() + 1;
            Map<String, MarketingTransferSyncUser> transferSyncUserMap = getTransferSyncUserMap(juZiCRuleTransferData);
            int applyLoanTimeDays = 30;
            if (!CollectionUtils.isEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig())
                    && (StringUtils.isNotEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig().get(APPLYLOANTIME)))) {
                applyLoanTimeDays = Integer.valueOf(marketingCommonConfig.getJuZiRealTimeLockConfig().get(APPLYLOANTIME));
            } //默认30天
            String applyLoanTime = LocalDateTime.now().minusDays(applyLoanTimeDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> cRuleLockData = marketingTransferSyncUserMapper.getJuZiCRuleLockData(tcId, applyLoanTime, transferSyncUserMap.keySet());
            //剔除锁定期的数据
            transferSyncUserMap.keySet().removeAll(cRuleLockData);
            cRuleTransferData.putAll(transferSyncUserMap);
        }
        for (Iterator<String> iterator = cRuleTransferData.keySet().iterator(); iterator.hasNext(); ) {
            String custNum = iterator.next();
            if (redisChgService.saddMember(RedisKeyConstant.juZiPushDaasCustNumKey, custNum) != 1L) {
                iterator.remove();
            }
        }
        log.warn("桔子实时C规则推送电销数据量={}", cRuleTransferData.size());
        pushDaasMap.put("c", cRuleTransferData);

    }

    /**
     * 获取d规则的数据
     *
     * @param tcId
     * @param date
     * @param
     * @return
     */
    private void getDrulePushData(String tcId, String date, Map<String, Map<String, MarketingTransferSyncUser>> pushDaasMap) {
        Long minId = null;
        Boolean isContiue = Boolean.TRUE;
        Map<String, MarketingTransferSyncUser> dRuleTransferData = new HashMap<>();
        while (isContiue) {
            //查询d规则的转化数据
            List<MarketingTransferSyncUser> juZiDRuleTransferData = marketingTransferSyncUserMapper.getJuZiDRuleTransferData(tcId, date, minId);
            if (juZiDRuleTransferData.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiDRuleTransferData.get(juZiDRuleTransferData.size() - 1).getId() + 1;
            Map<String, MarketingTransferSyncUser> transferSyncUserMap = getTransferSyncUserMap(juZiDRuleTransferData);
            int lentTimeDays = 30;
            if (!CollectionUtils.isEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig())
                    && (StringUtils.isNotEmpty(marketingCommonConfig.getJuZiRealTimeLockConfig().get(LENTTIME)))) {
                lentTimeDays = Integer.valueOf(marketingCommonConfig.getJuZiRealTimeLockConfig().get(LENTTIME));
            }//默认30天
            String lentTime = LocalDateTime.now().minusDays(lentTimeDays).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<String> dRuleLockData = marketingTransferSyncUserMapper.getJuZiDRuleLockData(tcId, lentTime, transferSyncUserMap.keySet());
            //剔除锁定期的数据
            transferSyncUserMap.keySet().removeAll(dRuleLockData);
            dRuleTransferData.putAll(transferSyncUserMap);
        }
        for (Iterator<String> iterator = dRuleTransferData.keySet().iterator(); iterator.hasNext(); ) {
            String custNum = iterator.next();
            if (redisChgService.saddMember(RedisKeyConstant.juZiPushDaasCustNumKey, custNum) != 1L) {
                iterator.remove();
            }
        }
        log.warn("桔子实时D规则推送电销数据量={}", dRuleTransferData.size());
        pushDaasMap.put("d", dRuleTransferData);
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

    private Map<String, MarketingTransferSyncUser> getTransferSyncUserMap(List<MarketingTransferSyncUser> transferSyncUserList) {
        return transferSyncUserList.stream().collect(
                Collectors.groupingBy(MarketingTransferSyncUser::getCustNum
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
    }
}
