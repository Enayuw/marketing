package com.br.marketing.service.Impl;
import java.time.LocalDateTime;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.dto.SingleDassAndRecordDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PhoneSaleExtendServiceImpl {

    @Autowired
    RedisChgService redisChgService;

    public static final DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String msTimeRegex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$";

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    PhoneSaleExtendInfoMapper saleExtendInfoMapper;

    @Resource
    TaskTimeMapper taskTimeMapper;
    
    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;
    
    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;
    
    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    MethodRetryHandlerService methodRetryHandlerService;

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    public HashSet<String> getStatus(){
        HashSet status = new HashSet();
        status.add("a");
        status.add("b");
        status.add("c");
        status.add("d");
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if (haluoTransferRule != null) {
            String statusStr = haluoTransferRule.getOrDefault("status", "a,b,d");
            status = new HashSet<>(Arrays.asList(statusStr.split(",")));
        }
        return status;
    }

    public Integer getTaskIdDays(){
        Integer taskTimeDays = 35;
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if (haluoTransferRule != null && StringUtils.isNotBlank(haluoTransferRule.get("taskIddate"))) {
            taskTimeDays = Integer.valueOf(haluoTransferRule.get("taskIddate"));
        }
        return taskTimeDays;
    }


    public Integer getDtimes(){
        Integer dtimes = 7;
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if (haluoTransferRule != null && StringUtils.isNotBlank(haluoTransferRule.get("dTimes"))) {
            dtimes = Integer.valueOf(haluoTransferRule.get("dTimes"));
        }
        return dtimes;
    }

    public void haluoPushDass(){
        List<String> defaultCode = new ArrayList<>();
        defaultCode.add("7410850");
        defaultCode.add("3710028");
        for (String s : defaultCode) {
            String minDate = LocalDate.now().minusDays(getTaskIdDays() - 1).format(ymd);
            TaskTimeExample timeExample= new TaskTimeExample();
            timeExample.createCriteria()
                    .andApiCodeEqualTo(s)
                    .andStartDateGreaterThanOrEqualTo(minDate);
            List<TaskTime> taskTimes = taskTimeMapper.selectByExample(timeExample);
            List<String> taskIds = taskTimes.stream().map(t -> t.getTaskId()).collect(Collectors.toList());
            if(taskIds.size()<=0){
                return;
            }
            for (String taskId : taskIds) {
                PhoneSaleExtendInfoExample infoExample= new PhoneSaleExtendInfoExample();
                infoExample.createCriteria().andTaskIdEqualTo(taskId)
                        .andStatusEqualTo("d");
                List<PhoneSaleExtendInfo> phoneSaleExtendInfos = saleExtendInfoMapper.selectByExample(infoExample);
                Map<String, List<PhoneSaleExtendInfo>> collect = phoneSaleExtendInfos.stream().collect(Collectors.groupingBy(PhoneSaleExtendInfo::getCustNum));
                Integer num =0;
                for (String key : collect.keySet()) {
                    List<PhoneSaleExtendInfo> phoneSaleExtendInfos1 = collect.get(key);
                    if(!haluoSaleJudge(phoneSaleExtendInfos1,"d",taskId)){
                        continue;
                    }
                    phoneSaleExtendInfos1.
                            sort(Comparator.comparing(PhoneSaleExtendInfo::getAppletDate).reversed()
                            .thenComparing(PhoneSaleExtendInfo::getCreateTime).reversed());
                    PhoneSaleExtendInfo extendInfo = phoneSaleExtendInfos1.get(0);
                    PhoneSaleExtendInfo saleExtendInfo = new PhoneSaleExtendInfo();
                    saleExtendInfo.setApiCode(extendInfo.getApiCode());
                    saleExtendInfo.setCustNum(extendInfo.getCustNum());
                    saleExtendInfo.setTaskId(extendInfo.getTaskId());
                    saleExtendInfo.setUserType(extendInfo.getUserType());
                    saleExtendInfo.setAppletDate(LocalDate.now().format(ymd));
                    saleExtendInfo.setAppletTime(LocalDateTime.now().format(ymdhms));
                    saleExtendInfo.setStatus("d");
                    saleExtendInfo.setCreateTime(new Date());
                    saleExtendInfo.setUpdateTime(new Date());
                    saleExtendInfo.setSourceId(extendInfo.getSourceId());
                    Result result = savePhoneExtend(saleExtendInfo);
                    if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
                        continue;
                    }
                    MarketingTransferSyncUserExample transferSyncUserExample = new MarketingTransferSyncUserExample();
                    transferSyncUserExample.settCid(tableCreateService.getTcId(extendInfo.getApiCode()));
                    transferSyncUserExample.createCriteria().andIdEqualTo(extendInfo.getSourceId());
                    List<MarketingTransferSyncUser> transferSyncUsers = transferSyncUserMapper.selectByExample(transferSyncUserExample);
                    MarketingTransferSyncUser transferSyncUser = transferSyncUsers.get(0);
                    List<String> taskquerIds = new ArrayList<>();
                    List<String> custnumIds = new ArrayList<>();
                    taskquerIds.add(extendInfo.getTaskId());
                    custnumIds.add(extendInfo.getCustNum());
                    List<MarketingSyncUser> syncUserByTaskAndCust = marketingSyncInfoMapper.getSyncUserByTaskAndCust(extendInfo.getApiCode(), taskquerIds, custnumIds);
                    MarketingSyncUser syncUser = syncUserByTaskAndCust.get(0);
                    String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
                    String name = org.apache.commons.lang3.StringUtils.isNotBlank(syncUser.getName()) ?
                            BrCipherMaker.getInstance().decode(syncUser.getName())
                            : "";
                    DassSingleImportDataDTO dassImportDataDTO = new DassSingleImportDataDTO();
                    dassImportDataDTO.setUid(extendInfo.getCustNum());
                    dassImportDataDTO.setPhone(cell);
                    dassImportDataDTO.setName(name);
                    dassImportDataDTO.setOrgname("hellobike");
                    dassImportDataDTO.setSource("96");
                    dassImportDataDTO.setUserType("3");
                    dassImportDataDTO.setLoginTime(haluoBydxTimeFormat(transferSyncUser.getLoginTime()));
                    dassImportDataDTO.setIfApply(transferSyncUser.getIfApply());
                    dassImportDataDTO.setApplyDt(haluoBydxTimeFormat(transferSyncUser.getApplyDt()));
                    dassImportDataDTO.setAuditTime(haluoBydxTimeFormat(transferSyncUser.getAuditTime()));
                    dassImportDataDTO.setAuditAmount(transferSyncUser.getAuditAmount());
                    dassImportDataDTO.setUnlentAmount(transferSyncUser.getUnlentAmount());
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(transferSyncUser.getReserveField1())) {
                        JSONObject jsonObject = JSON.parseObject(transferSyncUser.getReserveField1());
                        if (jsonObject != null) {
                            String applyInformation = jsonObject.getString("applyInformation");
                            if (org.apache.commons.lang3.StringUtils.isNotBlank(applyInformation)) {
                                JSONObject jsonObject1 = new JSONObject();
                                jsonObject1.put("applyInformation", applyInformation);
                                dassImportDataDTO.setExtend(JSON.toJSONString(jsonObject1));
                            }
                        }
                    }
                    SingleDassAndRecordDTO dto = new SingleDassAndRecordDTO();
                    dto.setSaleExtentId(saleExtendInfo.getId());
                    dto.setDassSingleImportDataDTO(dassImportDataDTO);
                    methodRetryHandlerService.callSingleDassAndRecord(dto,null);
                }
            }


        }
    }


    public boolean haluoSaleJudge(List<PhoneSaleExtendInfo> phoneSales, String dataStatus, String taskId) {
        if(phoneSales==null||phoneSales.size()<=0){
            return true;
        }
        List<PhoneSaleExtendInfo> sales = phoneSales.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(PhoneSaleExtendInfo::getAppletDate).reversed()
                        .thenComparing(PhoneSaleExtendInfo::getCreateTime).reversed()).collect(Collectors.toList());
        Integer taskTimeDays = 35;
        Integer abcTimeDays = 5;
        Integer dTimeDays = 4;
        Integer dTimes = getDtimes();
        HashSet status = getStatus();
        HashSet defaultGroupA = new HashSet();
        defaultGroupA.add("a");
        defaultGroupA.add("b");
        defaultGroupA.add("c");
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if (haluoTransferRule != null) {
            taskTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("taskIddate", "35"));
            abcTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("ABCdate", "5"));
            dTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("Ddate", "4"));
        }
        if (!status.contains(dataStatus)) {
            return false;
        }
        LocalDate nowDate = LocalDate.now();
        if (dataStatus.equals("d")) {
            PhoneSaleExtendInfo lastSale = null;
            Integer dnum = 0;
            for (PhoneSaleExtendInfo sale : sales) {
                if (sale.getStatus().equals("d")) {
                    if (lastSale == null) {
                        lastSale = sale;
                    }
                    dnum++;
                }
            }
            if (dnum >= dTimes) {
                return false;
            }
            if(lastSale==null){
                return true;
            }
            LocalDate lastDate = LocalDate.parse(lastSale.getAppletDate(), ymd);
            long until = lastDate.until(nowDate, ChronoUnit.DAYS);
            if (until >= dTimeDays) {
                return true;
            }
        } else {
            PhoneSaleExtendInfo phoneSaleExtendInfo = sales.get(0);
            String appletDate = phoneSaleExtendInfo.getAppletDate();
            if (appletDate.equals(nowDate.format(ymd))) {
                return false;
            }
            Optional<PhoneSaleExtendInfo> firstGroupA = sales.stream().filter(t -> defaultGroupA.contains(t.getStatus())).findFirst();
            if (firstGroupA.isPresent()) {
                PhoneSaleExtendInfo info = firstGroupA.get();
                LocalDate lastDate = LocalDate.parse(info.getAppletDate(), ymd);
                long until = lastDate.until(nowDate, ChronoUnit.DAYS);
                if (until >= abcTimeDays) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public String getHaluoStatus(MarketingTransferSyncUser transferSyncUser, MarketingSyncUser syncUser) {
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        HashSet status = new HashSet();
        status.add("a");
        status.add("b");
        status.add("d");
        if (haluoTransferRule != null) {
            String statusStr = haluoTransferRule.getOrDefault("status", "a,b,d");
            status = new HashSet<>(Arrays.asList(statusStr.split(",")));
        }
        JSONObject jb = JSON.parseObject(transferSyncUser.getReserveField1());
        boolean a = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "0".equals(jb.getString("applyInformation")))
                && !"1".equals(transferSyncUser.getIfApply());

        boolean b = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
                && !"1".equals(transferSyncUser.getIfApply());

        boolean c = "1".equals(transferSyncUser.getIfLogin())
                && (jb != null && org.apache.commons.lang3.StringUtils.isNotBlank(jb.getString("applyInformation")) && "1".equals(jb.getString("applyInformation")))
                && "1".equals(transferSyncUser.getIfApply())
                && "0".equals(transferSyncUser.getApplyResult());
        Double unlentAmount = Double.valueOf(org.apache.commons.lang3.StringUtils.isNotBlank(transferSyncUser.getUnlentAmount()) ? transferSyncUser.getUnlentAmount() : "0");
        boolean d = unlentAmount > 0;
        String statusStr = "";
        if (status.contains("d") && d) {
            statusStr = "d";
        } else if (status.contains("a") && a) {
            statusStr = "a";
        } else if (status.contains("b") && b) {
            statusStr = "b";
        } else if (status.contains("c") && c) {
            statusStr = "c";
        }
        return statusStr;
    }

    public Result savePhoneExtend(PhoneSaleExtendInfo info){
        Boolean lock = Boolean.FALSE;
        while (!lock) {
            Result<Boolean> booleanResult = addHaluoLock(info);
            //不需要等待
            if (!ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())) {
                removeHaluoLock(info);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            lock = booleanResult.getData();
            //如满足需要等待再次获取
            if (!lock) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        PhoneSaleExtendInfoExample extendInfoExample = new PhoneSaleExtendInfoExample();
        extendInfoExample.createCriteria()
                .andCustNumEqualTo(info.getCustNum())
                .andTaskIdEqualTo(info.getTaskId())
                .andAppletDateEqualTo(LocalDate.now().format(ymd));
        List<PhoneSaleExtendInfo> phoneSaleExtendInfos = saleExtendInfoMapper.selectByExample(extendInfoExample);
        if(phoneSaleExtendInfos.size()>0){
            Set<String> statusSet = phoneSaleExtendInfos.stream().map(t -> t.getStatus()).collect(Collectors.toSet());
            if(info.getStatus().equals("d")&&statusSet.contains("d")){
                removeHaluoLock(info);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            if(info.getStatus().equals("a")||info.getStatus().equals("b")||info.getStatus().equals("c")){
                removeHaluoLock(info);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        }
        saleExtendInfoMapper.insertSelective(info);
        removeHaluoLock(info);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private Result<Boolean> addHaluoLock(PhoneSaleExtendInfo info) {
        String key = RedisKeyConstant.haluoPushDx.concat(":")
                .concat(info.getApiCode()).concat(":")
                .concat(info.getTaskId()).concat(":")
                .concat(info.getCustNum());
        Long setnx = redisChgService.setnx(key, info.getStatus(), 3);
        //已经被其他数据抢占锁了
        if (setnx.equals(0L)) {

            //如果当前数据不是d就不推
            if (!info.getStatus().equals("d")) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            String s = redisChgService.get(key);

            //分布式锁的数据状态如果是d则都不推
            if (s.equals("d")) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            //如果当前数据状态是d 并且锁里的数据不是d 需要等待500ms然后再次获取锁
            if (info.getStatus().equals("d")) {
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
            }
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.TRUE);
    }

    private void removeHaluoLock(PhoneSaleExtendInfo info) {
        String key = RedisKeyConstant.haluoPushDx.concat(":")
                .concat(info.getApiCode()).concat(":")
                .concat(info.getTaskId()).concat(":")
                .concat(info.getCustNum());
        String s = redisChgService.get(key);
        if (info.getStatus().equals(s)) {
            redisChgService.del(key);
        }
    }

    public String haluoBydxTimeFormat(String time) {
        if (org.apache.commons.lang3.StringUtils.isBlank(time)) {
            return time;
        }

        if (Pattern.matches(msTimeRegex, time)) {
            return time.replace(":000", "");
        }

        return time;
    }
}
