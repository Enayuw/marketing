package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.MarketingSyncReportService;
import com.br.marketing.vo.MarketingSyncReportNumVO;
import com.br.marketing.vo.MarketingSyncReportVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 客户上传数据统计报表
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/18 14:47
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/18 14:47
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Service
@Slf4j
public class MarketingSyncReportServiceImpl implements MarketingSyncReportService {

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private VariableDicMapper variableDicMapper;
    @Resource
    private MarketingSyncReportMapper syncReportMapper;

    @Autowired
    ICompatibleService iCompatibleService;

    @Override
    public void syncReportProcess(String uploadDate, String jobName) {
        this.doSyncReportProcess(uploadDate,null,jobName);
    }

    /**
     * 上传数据统计报表流程
     *
     * @param uploadDate
     * @return
     */
    @Override
    public void syncReportProcess(String uploadDate) {
        this.doSyncReportProcess(uploadDate,null,null);
    }
    @Override
    public void syncReportProcessByApiCode(String uploadDate,String apiCode) {
        this.doSyncReportProcess(uploadDate,apiCode,null);
    }

    public void doSyncReportProcess(String uploadDate, String apiCodes,String jobName) {
        long l = System.currentTimeMillis();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50);
        List<Customer> customers = new ArrayList<>();
        if (apiCodes != null) {
            //1.获取所有客户
            customers.add(customerMapper.getCustomerByApiCode(apiCodes));
        } else {
            customers = customerMapper.getAllCustomer();
        }
        Map<String, Set<String>> userTypeMap = getUserTypeMap();
        CountDownLatch countDownLatch = new CountDownLatch(customers.size());
        for (Customer customer : customers) {
            if(StringUtils.isNoneBlank(jobName)){
                Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(),jobName);
                if(!action){
                    countDownLatch.countDown();
                    continue;
                }
            }
            threadPool.submit(() -> {
                try {
                    if (AuthShowProductor.NORMAL.getCode().equals(customer.getStatus())) {
                        String apiCode = customer.getApiCode();
                        log.warn("开始执行上传数据统计报表任务,apiCode={},uploadDate={}", apiCode, uploadDate);
                        //3.组装数据
                        Set<String> userTypeList = userTypeMap.getOrDefault(apiCode, Collections.emptySet());
                        //获取场景
                        if (!userTypeList.isEmpty()) {
                            for (String userType : userTypeList) {
                                String createStartDate = uploadDate;
                                String createEndDate = LocalDate.parse(uploadDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                        .plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                List<String> appletDateList = syncReportMapper.getAppletDatetikv_(apiCode, userType, createStartDate, createEndDate);
                                for (String appletDate : appletDateList) {
                                    //上传开始时间
                                    String appletBeginTime = getAppletTime(apiCode, userType, appletDate, Boolean.TRUE);
                                    if (StringUtils.isNotBlank(appletBeginTime)) {
                                        //数据正常入库条数
                                        Integer uploadNum = getUploadNum(apiCode, userType, appletDate, Boolean.TRUE);
                                        if (uploadNum != null && uploadNum > 0) {
                                            //判断是否更新
                                            MarketingSyncReport report = selectMarketingSyncReport(apiCode, userType, appletDate);
                                            MarketingSyncReport modifyReport = new MarketingSyncReport();
                                            Date appletEndTime = DateHelper.parseDate(getAppletTime(apiCode, userType, appletDate, Boolean.FALSE));
                                            if (report == null) {
                                                //新增
                                                modifyReport.setAppletBeginTime(DateHelper.parseDate(appletBeginTime));
                                                //场景
                                                modifyReport.setUserType(userType);
                                                //上传日期
                                                modifyReport.setAppletDate(appletDate);
                                                //客户编号
                                                modifyReport.setCid(customer.getCid());
                                                //apiCode
                                                modifyReport.setApiCode(apiCode);
                                                //客户名称
                                                modifyReport.setShortName(customer.getShortName());
                                                //上传结束时间
                                                modifyReport.setAppletEndTime(appletEndTime);
                                                modifyReport.setCreateTime(new Date());
                                            }
                                            //数据正常入库条数
                                            modifyReport.setNormalNum(uploadNum);
                                            //去重后数据量
                                            modifyReport.setDuplicateRemovalNum(getUploadNum(apiCode, userType, appletDate, Boolean.FALSE));
                                            //入库
                                            if (report == null) {
                                                log.warn("新增上传数据统计：{}", JSON.toJSONString(modifyReport));
                                                syncReportMapper.insert(modifyReport);
                                            } else {
                                                Date appletEndTimeReport = report.getAppletEndTime();
                                                //更新
                                                if (appletEndTime.compareTo(appletEndTimeReport) == 1) {
                                                    modifyReport.setId(report.getId());
                                                    modifyReport.setAppletEndTime(appletEndTime);
                                                    log.warn("编辑上传数据统计：{}", JSON.toJSONString(modifyReport));
                                                    syncReportMapper.modifyReportById(modifyReport);
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("程序执行上传数据统计报表任务异常，apiCode={}", customer.getApiCode(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        // 等待线程执行完毕
        try {
            countDownLatch.await();
            //关闭线程池
            threadPool.shutdown();
            log.warn("上传记录-同步记录操作执行完成，耗时{}s", (System.currentTimeMillis() - l)/1000);
        } catch (InterruptedException e) {
            log.error("countDownLatch 线程执行异常", e);
        }
    }
    /**
     * 根据参数获取上传统计数据
     *
     * @param apiCode
     * @param userType
     * @param uploadDate
     * @return
     */
    private MarketingSyncReport selectMarketingSyncReport(String apiCode, String userType, String uploadDate) {
        MarketingSyncReportExample report = new MarketingSyncReportExample();
        report.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andUserTypeEqualTo(userType)
                .andAppletDateEqualTo(uploadDate);
        List<MarketingSyncReport> reportList = syncReportMapper.selectByExample(report);
        if (reportList != null && !reportList.isEmpty()) {
            return reportList.get(0);
        }
        return null;
    }

    /**
     * 获取数据上传时间
     *
     * @param apiCode
     * @param userType
     * @return
     */
    private String getAppletTime(String apiCode, String userType, String uploadDate, Boolean flag) {
        if (flag) {
            return syncReportMapper.uploadSyncMinAppletTime(apiCode, userType, uploadDate);
        } else {
            return syncReportMapper.uploadSyncMaxAppletTime(apiCode, userType, uploadDate);
        }
    }

    /**
     * 获取数据条数
     *
     * @param apiCode
     * @param userType
     * @return
     */
    private Integer getUploadNum(String apiCode, String userType, String uploadDate, Boolean flag) {
        if (flag) {
            return syncReportMapper.uploadSyncCounttiflash_(apiCode, userType, uploadDate, AuthShowProductor.NO_NORMAL.getCode());
        } else {
            return syncReportMapper.uploadSyncCounttiflash_(apiCode, userType, uploadDate, AuthShowProductor.NORMAL.getCode());
        }
    }
    /**
     * 获取所有场景
     * @return
     */
    private Map<String, Set<String>> getUserTypeMap() {
        VariableDicExample dic = new VariableDicExample();
        dic.createCriteria().andIsDelEqualTo(1);
        List<VariableDic> dicList = variableDicMapper.selectByExample(dic);
        return dicList.parallelStream().collect(Collectors.groupingBy(VariableDic::getApiCode
                , Collectors.mapping(VariableDic::getFieldValue, Collectors.toSet())));
    }




    /**
     * 获取场景
     *
     * @param apiCode
     * @return
     */
    private List<String> getUserTypeList(String apiCode) {
        VariableDicExample dic = new VariableDicExample();
        dic.createCriteria().andApiCodeEqualTo(apiCode);
        List<VariableDic> dicList = variableDicMapper.selectByExample(dic);
        List<String> userTypeList = new ArrayList<>();
        if (dicList != null && !dicList.isEmpty()) {
            userTypeList = dicList.stream().map(v -> v.getFieldValue()).collect(Collectors.toList());
        }
        return userTypeList;
    }

    @Override
    public PageResultReturn getReportList(int current, int size, String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes, String userTypes) {

        if (StringUtils.isNotEmpty(appletTimeEnd)){
            appletTimeEnd = DateUtils.format(addDay(appletTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }

        if (StringUtils.isNotEmpty(cidOrName) && cidOrName.contains("_")){
            cidOrName = cidOrName.replace("_", "\\_");
        }

        List<String> apiCodeList = new ArrayList<>();
        List<String> userTypeList = new ArrayList<>();
        if(apiCodes != null && !"".equals(apiCodes)){
            String[] split = apiCodes.split(",");
            for(String item : split){
                apiCodeList.add(item);
            }
        }
        if(userTypes != null && !"".equals(userTypes)){
            String[] split = userTypes.split(",");
            for(String item : split){
                userTypeList.add(item);
            }
        }
        Map params = new HashMap();
        params.put("cidOrName",cidOrName);
        params.put("appletTimeStart",appletTimeStart);
        params.put("appletTimeEnd",appletTimeEnd);
        params.put("apiCodeList",apiCodeList);
        params.put("userTypeList",userTypeList);

        PageHelper.startPage(current, size);
        List<MarketingSyncReportVO> list = syncReportMapper.selectList(params);

        return PageResultReturn.setPageResult(list, current,size);
    }

    @Override
    public Map getReportListTotal(String cidOrName, String appletTimeStart, String appletTimeEnd, String apiCodes, String userTypes) {
        if (StringUtils.isNotEmpty(appletTimeEnd)){
            appletTimeEnd = DateUtils.format(addDay(appletTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }

        if (StringUtils.isNotEmpty(cidOrName) && cidOrName.contains("_")){
            cidOrName = cidOrName.replace("_", "\\_");
        }

        List<String> apiCodeList = new ArrayList<>();
        List<String> userTypeList = new ArrayList<>();
        if(apiCodes != null && !"".equals(apiCodes)){
            String[] split = apiCodes.split(",");
            for(String item : split){
                apiCodeList.add(item);
            }
        }
        if(userTypes != null && !"".equals(userTypes)){
            String[] split = userTypes.split(",");
            for(String item : split){
                userTypeList.add(item);
            }
        }
        Map params = new HashMap();
        params.put("cidOrName",cidOrName);
        params.put("appletTimeStart",appletTimeStart);
        params.put("appletTimeEnd",appletTimeEnd);
        params.put("apiCodeList",apiCodeList);
        params.put("userTypeList",userTypeList);

        Map map = new HashMap();
        Integer normalNumTotal = 0;
        Integer duplicateRemovalNumTotal = 0;
        List<MarketingSyncReportNumVO> listTotal = syncReportMapper.getReportListTotaltiflash_(params);
        if (!CollectionUtils.isEmpty(listTotal)) {
            //数据正常入库条数
            normalNumTotal = listTotal.stream().collect(Collectors.summingInt(MarketingSyncReportNumVO::getNormalNumTotal));
            //去重后数据量
            duplicateRemovalNumTotal = listTotal.stream().collect(Collectors.summingInt(MarketingSyncReportNumVO::getDuplicateRemovalNumTotal));
        }
        map.put("normalNumTotal", normalNumTotal);
        map.put("duplicateRemovalNumTotal", duplicateRemovalNumTotal);
        return map;
    }

    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

    @Override
    public void deleteReportByAppletDate(String mes) {
        JSONObject jsonObject = JSON.parseObject(mes);
        JSONArray dataArray = jsonObject.getJSONArray("dataArray");
        if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataJson = dataArray.getJSONObject(i);
                String apiCode = dataJson.getString("apiCode");
                String appletDate = dataJson.getString("appletDate");
                syncReportMapper.deleteByAppletDate(apiCode, appletDate);
            }
        }

    }
}
