package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingTransferConstants;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IPPDTransferService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificialBatchRealTimeDataHandler;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
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
    private RocketMqSwitch rocketMqSwitch;
    @Resource
    private RocketMqTemplate template;

    @Resource
    YiXinTransferServiceImpl yiXinTransferService;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ArtificialBatchRealTimeDataHandler artificialBatchRealTimeDataHandler;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

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
                mqFact.setIdempotentKey(snowflakeRedisGeneratorHandle.nextId());

                mqFact.setMessage(JSONObject.toJSONString(paramMessage));
                if(rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE)){
                    String message = JSON.toJSONString(mqFact);
                    rocketMqSwitch.syncSend(MarketingTransferConstants.TOPIC
                            , MarketingTransferConstants.TAG_MARKETING_UNIVERSAL_TRANSFER_RECEIVE, message);
                }else{
                    producter.sendToUniversalTransferQueue(mqFact);
                }
            }
        }
        yiXinTransferService.updateFrontDataStatus(frontId, 2);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public long ppdaiOldPeriodicityPushDx(LocalDate now, String... apiCode) {
        String yyyymmdd = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        int number = 0;
        // 查询任务状态为4的记录
        Result<TransferActionFront> frontDataRes = yiXinTransferService.getFrontData(
                StringUtils.join(apiCode, ","), yyyymmdd, 4);
        if (!ResultCode.SUCCESS.getValue().equals(frontDataRes.getCode())) {
            return number;
        }
        TransferActionFront frontData = frontDataRes.getData();
        if (frontData != null && new Integer(2).equals(frontData.getStatus())) {
            log.warn("拍拍贷老客周期性推送电销任务今日已经推送!");
            return number;
        }
        // 任务状态标记为4
        Long frontId = yiXinTransferService.saveFrontData(StringUtils.join(apiCode, ","), yyyymmdd, 4);
        String tcId = tableCreateService.getTcId(apiCode[0]);
        int ppdOldPhoneValidityDay = marketingCommonConfig.getPpdOldPhoneValidityDay() != null
                ? marketingCommonConfig.getPpdOldPhoneValidityDay() : 5;
        // 获取周期日期
        LocalDate localDate = now.minusDays(ppdOldPhoneValidityDay);
        Date startDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(localDate.atTime(23, 59, 59, 999999999)
                .atZone(ZoneId.systemDefault()).toInstant());
        //有效期判断-1天
        LocalDate date = now.minusDays(1);
        int pageSize = 2000;
        List<String> statusList = Arrays.asList("a", "b");
        List<BatchRealTimeUserDataDTO> transferData;
        for (String code : apiCode) {
            int pageNum = 1;
            int numberA = 0;
            int numberB = 0;
            PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
            example.createCriteria().andApiCodeEqualTo(code).andStatusIn(statusList).andPStatusEqualTo(2)
                    .andPushDxTimeBetween(startDate, endDate);
            example.setOrderByClause("id,status");
            while (true) {
                List<PhoneSaleExtendInfo> list = phoneSaleExtendInfoMapper.findListPageByExample(
                        example, pageNum, pageSize);
                int size = list.size();
                if (size == 0) {
                    break;
                }
                pageNum++;
                Map<String, PhoneSaleExtendInfo> infoMap = list.parallelStream().collect(Collectors.toMap(
                        PhoneSaleExtendInfo::getCustNum, Function.identity(), (v1, v2) -> v1));
                Set<String> set = infoMap.keySet();
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum =
                        transferDataValidityPeriodService.getValidityPeriodsByCustNum(set, code, date);
                // 获取（ppdOldPhoneValidityDay-1）天内（包括当天）已推送过的案件编号
                Set<String> custNumSet = selectPush(code, ppdOldPhoneValidityDay - 1, new ArrayList<>(set), statusList);
                if (custNumSet == null) {
                    continue;
                }
                // 删除已推送过的案件编号
                set.removeAll(custNumSet);
                Collection<PhoneSaleExtendInfo> values = infoMap.values();
                transferData = new ArrayList<>();
                for (PhoneSaleExtendInfo info : values) {
                    SyncUserValidityPeriodsBO bo = validityPeriodsByCustNum.get(info.getCustNum());
                    // 判断有效期,为null时代表不在有效期
                    if (bo == null) {
                        continue;
                    }
                    MarketingSyncUser syncUser = bo.getSyncUsers().get(0);
                    // 剔除对应案件编号有效期内转化数据中命中IfLent=Y的案件编号
                    if (checkPeriodOfValidityAndIfLentIsY(bo.getBuilders(), tcId, info)) {
                        // 数据情况统计
                        if ("a".equals(info.getStatus())) {
                            numberA++;
                        } else if ("b".equals(info.getStatus())) {
                            numberB++;
                        }
                        // 封装人工接口数据
                        DassImportDataDTO dassImportData = getDassImportData(info, syncUser);
                        BatchRealTimeUserDataDTO dataDTO = new BatchRealTimeUserDataDTO();
                        dataDTO.setDassImportDataDTO(dassImportData);
                        // 封装人工本地数据记录
                        getPhoneSaleExtendInfo(info);
                        dataDTO.setPhoneSaleExtendInfo(info);
                        transferData.add(dataDTO);
                    }
                }
                artificialBatchRealTimeDataHandler.call(transferData, new ProcessHandlerContext());
                number += transferData.size();
                transferData.clear();
                if (size < pageSize) {
                    break;
                }
            }
            log.warn("拍拍贷老客周期性推送电销任务: apiCode={},推送总量:{},a情况推送量:{},b情况推送量{}"
                    , code, number, numberA, numberB);
        }
        yiXinTransferService.updateFrontDataStatus(frontId, 2);
        return number;
    }

    /**
     * 2023-02-15 14:53
     * 检查是否存在案件编号有效期内转化数据中命中IfLent=Y的案件编号
     * <p>
     * 2024年1月8日17点16分 有效期方法变更
     * <p>
     *
     * @param builderList 有效期范围集合
     * @param tcId        cid
     * @param info        电销数据
     * @return true 不存在
     * @version 1.0
     */
    private boolean checkPeriodOfValidityAndIfLentIsY(List<PeriodOfValidityBO.Builder> builderList
            , String tcId
            , PhoneSaleExtendInfo info) {
        try {
            MarketingTransferSyncUserExample transferSyncUserExample = new MarketingTransferSyncUserExample();
            transferSyncUserExample.settCid(tcId);
            transferSyncUserExample.createCriteria()
                    .andTCidEqualTo(tcId)
                    .andApiCodeEqualTo(info.getApiCode())
                    .andCustNumEqualTo(info.getCustNum())
                    .andIfLentEqualTo("Y");
            return marketingTransferSyncUserMapper.countByExampleSql(
                    transferSyncUserExample, getDatesBetweenSql(builderList)) < 1;
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 2024-01-09 10:29
     * 获取日期间隔范围内所有日期
     *
     * @param builderList 有效期范围
     * @return 日期集合，格式：yyyy-mm-dd
     */
    private String getDatesBetweenSql(List<PeriodOfValidityBO.Builder> builderList) {
        int size = builderList.size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            PeriodOfValidityBO bo = builderList.get(i).addDateString().builder();
            String beginDateStr = bo.getBeginDateStr();
            String enDateStr = bo.getEnDateStr();
            sb.append("(request_data between '").append(beginDateStr).append("' and  '").append(enDateStr).append("')");
            if (i != (size - 1)) {
                sb.append(" or ");
            }
        }
        if (sb.length() > 0) {
            return " and (" + sb + ") ";
        }
        return " and 1!=1";
    }

    /**
     * 2023-02-15 16:05
     * <p>
     * 检查前{@code day}天前是否推送过，包括当天
     */
    private Set<String> selectPush(String apiCode, int day, List<String> custNums, List<String> statusList) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Instant endInstant = now.atZone(ZoneId.systemDefault()).withHour(23).withMinute(59).withSecond(59)
                    .withNano(999999999).toInstant();
            Instant startInstant = now.toLocalDate().minusDays(day).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            PhoneSaleExtendInfoExample extendInfoExample = new PhoneSaleExtendInfoExample();
            extendInfoExample.createCriteria().andApiCodeEqualTo(apiCode).andStatusIn(statusList)
                    .andCustNumIn(custNums).andPushDxTimeBetween(Date.from(startInstant), Date.from(endInstant));
            extendInfoExample.setDistinct(true);
            return phoneSaleExtendInfoMapper.getCustNumSettikv_(extendInfoExample);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private DassImportDataDTO getDassImportData(PhoneSaleExtendInfo info, MarketingSyncUser syncUser) {
        DassImportDataDTO batchImportData = new DassImportDataDTO();
        batchImportData.setId(info.getSourceId());
        try {
            String decodeName;
            String name = StringUtils.isNotBlank(syncUser.getName()) ?
                    (syncUser.getName().equals(decodeName = BrCipherMaker.getInstance().decode(syncUser.getName())) ? "1"
                            : decodeName) : "1";
            // 根据custNum取上传接口最新的name转成明文传输
            batchImportData.setName(name);
            String phone = AESUtil.aesEncrypty(BrCipherMaker.getInstance().decode(syncUser.getCell()), aesKey);
            // 根据custNum取上传接口最新的cell转aes加密
            batchImportData.setPhone(phone);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
        batchImportData.setUid(info.getCustNum());
        batchImportData.setOrgname("ppdai");
        batchImportData.setUserType("1");
        batchImportData.setSource("18");
        batchImportData.setType("8");
        return batchImportData;
    }

    private void getPhoneSaleExtendInfo(PhoneSaleExtendInfo info) {
        info.setPStatus(1);
        info.setCreateTime(new Date());
        info.setPushDxTime(new Date());
        if ("a".equals(info.getStatus())) {
            info.setStatus("b");
        }
        info.setId(null);
    }
}
