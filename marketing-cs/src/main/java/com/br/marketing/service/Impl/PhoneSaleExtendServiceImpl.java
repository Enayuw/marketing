package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.dassservice.input.DassImportAdapHaluoDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.strategy.MultipleDassAndBlackHandler;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    PhoneSaleExtendHaluoMapper saleExtendHaluoMapper;

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

    @Autowired
    MultipleDassAndBlackHandler multipleDassAndBlackHandler;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    public HashSet<String> getStatus() {
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

    public Integer getTaskIdDays() {
        Integer taskTimeDays = 35;
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if (haluoTransferRule != null && StringUtils.isNotBlank(haluoTransferRule.get("taskIddate"))) {
            taskTimeDays = Integer.valueOf(haluoTransferRule.get("taskIddate"));
        }
        return taskTimeDays;
    }


    public Integer getDtimes() {
        Integer dtimes = 7;
        HashMap<String, String> haluoTransferRule = marketingCommonConfig.getHaluoTransferRule();
        if (haluoTransferRule != null && StringUtils.isNotBlank(haluoTransferRule.get("dTimes"))) {
            dtimes = Integer.valueOf(haluoTransferRule.get("dTimes"));
        }
        return dtimes;
    }

    public void haluoPushDass() {
        List<String> defaultCode = new ArrayList<>();
        defaultCode.add("7410850");
        defaultCode.add("3710028");
        for (String s : defaultCode) {
            String minDate = LocalDate.now().minusDays(getTaskIdDays() - 1).format(ymd);
            TaskTimeExample timeExample = new TaskTimeExample();
            timeExample.createCriteria()
                    .andApiCodeEqualTo(s)
                    .andStartDateGreaterThanOrEqualTo(minDate);
            List<TaskTime> taskTimes = taskTimeMapper.selectByExample(timeExample);
            List<String> taskIds = taskTimes.stream().map(t -> t.getTaskId()).collect(Collectors.toList());
            if (taskIds.size() <= 0) {
                return;
            }
            for (String taskId : taskIds) {
                Boolean custPage = Boolean.TRUE;
                Integer pageSize = 1000;
                Integer pageIndex = 0;
                while (custPage){
                    Integer start = pageIndex*pageSize;
                    List<String> keys = saleExtendHaluoMapper.selectCustNumsByTaskIdAndD(taskId, "d",start,pageSize);
                    if(keys.size()<=0){
                        custPage = Boolean.FALSE;
                        continue;
                    }
                    pageIndex++;
                    List<List<String>> partition = ListUtils.partition(keys, 50);
                    for (List<String> innerKeys : partition) {
                        pushThread(innerKeys, taskId, s);
                    }
                }

            }
        }
    }

    void pushThread(List<String> keys, String taskId, String apiCode) {
        PhoneSaleExtendHaluoExample infoExample = new PhoneSaleExtendHaluoExample();
        infoExample.createCriteria().andTaskIdEqualTo(taskId)
                .andStatusEqualTo("d")
                .andCustNumIn(keys);
        List<PhoneSaleExtendHaluo> collect = saleExtendHaluoMapper.selectByExample(infoExample);
        DassImportAdapHaluoDTO dassImportAdapDTO = new DassImportAdapHaluoDTO();
        dassImportAdapDTO.setList(new ArrayList<DassImportDataDTO>());
        dassImportAdapDTO.setPhoneSaleExtendHaluos(new ArrayList<PhoneSaleExtendHaluo>());
        dassImportAdapDTO.setIsJob(1);
        List<String> taskquerIds = new ArrayList<>();
        List<String> custnumIds = new ArrayList<>();
        taskquerIds.add(taskId);
        custnumIds.addAll(keys);
        List<MarketingSyncUser> syncUserByTaskAndCust = marketingSyncInfoMapper.getSyncUserByTaskAndCust(apiCode, taskquerIds, custnumIds);
        for (String key : keys) {
            List<PhoneSaleExtendHaluo> phoneSaleExtendInfos1 = collect.stream().filter(t->t.getCustNum().equals(key)).collect(Collectors.toList());
            if (!haluoSaleJudge(phoneSaleExtendInfos1, "d", taskId)) {
                continue;
            }

            //region 获取syncUser 和 transferSyncUser
            Optional<MarketingSyncUser> first = syncUserByTaskAndCust.stream().filter(t -> t.getCustNum().equals(key)).findFirst();
            if (!first.isPresent()) {
                continue;
            }
            MarketingSyncUser syncUser = first.get();

            phoneSaleExtendInfos1.
                    sort(Comparator.comparing(PhoneSaleExtendHaluo::getAppletDate)
                            .thenComparing(PhoneSaleExtendHaluo::getCreateTime).reversed());
            PhoneSaleExtendHaluo extendInfo = phoneSaleExtendInfos1.get(0);
            MarketingTransferSyncUser transferSyncUser =null;
            if(extendInfo.getSourceId()!=null&&extendInfo.getSourceId()>0) {
                MarketingTransferSyncUserExample transferSyncUserExample = new MarketingTransferSyncUserExample();
                transferSyncUserExample.settCid(tableCreateService.getTcId(apiCode));
                transferSyncUserExample.createCriteria().andIdEqualTo(extendInfo.getSourceId());
                List<MarketingTransferSyncUser> transferSyncUsers = transferSyncUserMapper.selectByExample(transferSyncUserExample);
                transferSyncUser = transferSyncUsers.get(0);
            }
            //endregion

            //region dassImportAdapDTO赋值
            PhoneSaleExtendHaluo phoneSaleExtendHaluo = new PhoneSaleExtendHaluo();
            DassImportDataDTO dassImportDataDTO = new DassImportDataDTO();
            //endregion

            //region 赋值phoneSaleExtendHaluo

            phoneSaleExtendHaluo.setCustNum(extendInfo.getCustNum());
            phoneSaleExtendHaluo.setApiCode(apiCode);
            phoneSaleExtendHaluo.setTaskId(extendInfo.getTaskId());
            phoneSaleExtendHaluo.setAppletDate(LocalDate.now().format(ymd));
            phoneSaleExtendHaluo.setAppletTime(LocalDateTime.now().format(ymdhms));
            phoneSaleExtendHaluo.setStatus("d");
            phoneSaleExtendHaluo.setCreateTime(new Date());
            phoneSaleExtendHaluo.setSourceId(extendInfo.getSourceId());
            //endregion

            //region 组装电销数据
            String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
            String s = AESUtil.aesEncrypty(cell, aesKey);
            String name = org.apache.commons.lang3.StringUtils.isNotBlank(syncUser.getName()) ?
                    BrCipherMaker.getInstance().decode(syncUser.getName())
                    : "";
            dassImportDataDTO.setUid(extendInfo.getCustNum());
            dassImportDataDTO.setPhone(s);
            dassImportDataDTO.setName(name);
            dassImportDataDTO.setOrgname("hellobike");
            dassImportDataDTO.setSource("96");
            dassImportDataDTO.setUserType("3");
            if(transferSyncUser!=null) {
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
            }
            //endregion

            Result result = savePhoneExtend(phoneSaleExtendHaluo);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                dassImportAdapDTO.getPhoneSaleExtendHaluos().add(phoneSaleExtendHaluo);
                dassImportAdapDTO.getList().add(dassImportDataDTO);
            }
        }
        if (dassImportAdapDTO.getList() != null && dassImportAdapDTO.getList().size() > 0) {
            methodRetryHandlerService.callDassRealTimeBatchData(dassImportAdapDTO, null);
        }
    }


    public boolean haluoSaleJudge(List<PhoneSaleExtendHaluo> phoneSales, String dataStatus, String taskId) {
        if (phoneSales == null) {
            return true;
        }
        List<PhoneSaleExtendHaluo> sales = phoneSales.stream()
                .filter(t -> t.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(PhoneSaleExtendHaluo::getAppletDate)
                        .thenComparing(PhoneSaleExtendHaluo::getCreateTime).reversed()).collect(Collectors.toList());
        if (sales.size() <= 0) {
            return true;
        }
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
            abcTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("ABCdate", "5"));
            dTimeDays = Integer.valueOf(haluoTransferRule.getOrDefault("Ddate", "4"));
        }
        if (!status.contains(dataStatus)) {
            return false;
        }
        LocalDate nowDate = LocalDate.now();
        if (dataStatus.equals("d")) {
            PhoneSaleExtendHaluo lastSale = null;
            Integer dnum = 0;
            for (PhoneSaleExtendHaluo sale : sales) {
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
            if (lastSale == null) {
                return true;
            }
            LocalDate lastDate = LocalDate.parse(lastSale.getAppletDate(), ymd);
            long until = lastDate.until(nowDate, ChronoUnit.DAYS);
            if (until >= dTimeDays) {
                return true;
            }
        } else {
            PhoneSaleExtendHaluo phoneSaleExtendInfo = sales.get(0);
            String appletDate = phoneSaleExtendInfo.getAppletDate();
            if (appletDate.equals(nowDate.format(ymd))) {
                return false;
            }
            Optional<PhoneSaleExtendHaluo> firstGroupA = sales.stream().filter(t -> defaultGroupA.contains(t.getStatus())).findFirst();
            if (firstGroupA.isPresent()) {
                PhoneSaleExtendHaluo info = firstGroupA.get();
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

    public Result savePhoneExtend(PhoneSaleExtendHaluo info) {
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
        PhoneSaleExtendHaluoExample extendInfoExample = new PhoneSaleExtendHaluoExample();
        extendInfoExample.createCriteria()
                .andApiCodeEqualTo(info.getApiCode())
                .andCustNumEqualTo(info.getCustNum())
                .andTaskIdEqualTo(info.getTaskId())
                .andAppletDateEqualTo(LocalDate.now().format(ymd));
        List<PhoneSaleExtendHaluo> phoneSaleExtendInfos = saleExtendHaluoMapper.selectByExample(extendInfoExample);
        if (phoneSaleExtendInfos.size() > 0) {
            Set<String> statusSet = phoneSaleExtendInfos.stream().map(t -> t.getStatus()).collect(Collectors.toSet());
            if (info.getStatus().equals("d") && statusSet.contains("d")) {
                removeHaluoLock(info);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
            if (info.getStatus().equals("a") || info.getStatus().equals("b") || info.getStatus().equals("c")) {
                removeHaluoLock(info);
                return new Result().setCode(ResultCode.FAIL.getValue());
            }
        }
        saleExtendHaluoMapper.insertSelective(info);
        removeHaluoLock(info);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    private Result<Boolean> addHaluoLock(PhoneSaleExtendHaluo info) {
        String key = RedisKeyConstant.haluoPushDx.concat(":")
                .concat(info.getApiCode()).concat(":")
                .concat(info.getTaskId()).concat(":")
                .concat(info.getCustNum());
        Boolean setnx = redisChgService.setnx(key, info.getStatus(), 3);
        //已经被其他数据抢占锁了
        if (!setnx) {

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

    private void removeHaluoLock(PhoneSaleExtendHaluo info) {
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

    /**
     * 2023-08-25 10:10
     * 组规则判断
     * 查询{@code day}天内（包含当天）是否推送过该手机号:
     * 不存在  推送；
     * 存在，取最近推送状态：
     * 组1→组2 推送
     * 组2→组1 不推送
     * 组1→组1 不推送
     * 组2→组2 不推送
     *
     * @param apiCode     apiCode
     * @param day         天
     * @param cell        手机号
     * @param groupNo 组号
     * @return true 推送
     */
    public boolean groupRule(String apiCode
            , int day
            , String cell
            , int groupNo) {
        if (StringUtils.isBlank(cell)) {
            return false;
        }
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        getInfoExampleGroup(example, day, apiCode).andCellEqualTo(cell);
        List<PhoneSaleExtendInfo> list = phoneSaleExtendInfoMapper.findInfoByMaxPushDxTimeAndCellList(example);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        PhoneSaleExtendInfo info = list.get(0);
        return info.getGroupNo() == 0 || info.getGroupNo() < groupNo;
    }

    /**
     * 2023-08-25 10:15
     * 批量组规则判断,多手机号多组情况
     * 查询{@code day}天内（包含当天）是否推送过该手机号:
     * 不存在  推送；
     * 存在，取最近推送状态：
     * 组1→组2 推送
     * 组2→组1 不推送
     * 组1→组1 不推送
     * 组2→组2 不推送
     *
     * @param apiCode            apiCode
     * @param day                天
     * @param cellGroupNumberMap key cell手机号；value groupNumber组号
     * @return map{@code cellGroupNumberMap} map中存在则推送
     */
    public Map<String, Integer> groupRule(String apiCode
            , int day
            , Map<String, Integer> cellGroupNumberMap) {
        if (CollectionUtils.isEmpty(cellGroupNumberMap)) {
            return cellGroupNumberMap;
        }
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        Set<String> cellSet = cellGroupNumberMap.keySet();
        getInfoExampleGroup(example, day, apiCode).andCellIn(new ArrayList<>(cellSet));
        List<PhoneSaleExtendInfo> list = phoneSaleExtendInfoMapper.findInfoByMaxPushDxTimeAndCellList(example);
        if (CollectionUtils.isEmpty(list)) {
            return cellGroupNumberMap;
        }
        Map<String, Integer> dbCellGroupMap = list.stream().collect(Collectors.toConcurrentMap(PhoneSaleExtendInfo::getCell
                , PhoneSaleExtendInfo::getGroupNo, (v1, v2) -> v1 > v2 ? v1 : v2));
        Map<String, Integer> map = new ConcurrentHashMap<>(cellGroupNumberMap.size());
        cellGroupNumberMap.forEach((cell, groupNo) -> {
            Integer groupNoOld = dbCellGroupMap.get(cell);
            if (groupNoOld == null || groupNoOld == 0 || groupNoOld < groupNo) {
                map.put(cell, groupNo);
            }
        });
        return map;
    }

    /**
     * 2023-08-25 10:15
     * 批量组规则判断,多手机号一组情况
     * 查询{@code day}天内（包含当天）是否推送过该手机号:
     * 不存在  推送；
     * 存在，取最近推送状态：
     * 组1→组2 推送
     * 组2→组1 不推送
     * 组1→组1 不推送
     * 组2→组2 不推送
     *
     * @param apiCode apiCode
     * @param day     天
     * @param cellSet cell手机号
     * @return map{@code cellGroupNumberMap} map中存在则推送
     */
    public Set<String> groupRule(String apiCode
            , int day
            , Set<String> cellSet
            , int groupNo) {
        if (CollectionUtils.isEmpty(cellSet)) {
            return cellSet;
        }
        PhoneSaleExtendInfoExample example = new PhoneSaleExtendInfoExample();
        getInfoExampleGroup(example, day, apiCode).andCellIn(new ArrayList<>(cellSet));
        List<PhoneSaleExtendInfo> list = phoneSaleExtendInfoMapper.findInfoByMaxPushDxTimeAndCellList(example);
        if (CollectionUtils.isEmpty(list)) {
            return cellSet;
        }
        Map<String, Integer> dbCellGroupMap = list.stream().collect(Collectors.toConcurrentMap(PhoneSaleExtendInfo::getCell
                , PhoneSaleExtendInfo::getGroupNo, (v1, v2) -> v1 > v2 ? v1 : v2));
        Set<String> set = new HashSet<>(cellSet.size());
        cellSet.forEach(cell -> {
            Integer groupNoOld = dbCellGroupMap.get(cell);
            if (groupNoOld == null || groupNoOld == 0 || groupNoOld < groupNo) {
                set.add(cell);
            }
        });
        return set;
    }

    /**
     * 2023-09-02 16:29
     * 构造公共参数
     */
    private PhoneSaleExtendInfoExample.Criteria getInfoExampleGroup(PhoneSaleExtendInfoExample example
            , int day
            , String apiCode) {
        example.setOrderByClause("push_dx_time desc");
        LocalDate now = LocalDate.now();
        Instant instantStart = now.minusDays(day).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Instant instantEnd = now.atTime(23, 59, 59, 999999999)
                .atZone(ZoneId.systemDefault()).toInstant();
        return example.createCriteria()
                .andPushDxTimeBetween(Date.from(instantStart), Date.from(instantEnd))
                .andApiCodeEqualTo(apiCode);

    }
}
