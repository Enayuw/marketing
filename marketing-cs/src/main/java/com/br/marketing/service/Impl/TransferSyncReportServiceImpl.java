package com.br.marketing.service.Impl;

import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TransferSyncReportMapper;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.service.TransferSyncReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.TransferSyncReportNumVO;
import com.br.marketing.vo.TransferSyncReportVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 转化数据报表实现
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/29 10:30
 */
@Service
@Slf4j
public class TransferSyncReportServiceImpl implements TransferSyncReportService {
    @Resource
    private TransferSyncReportMapper transferSyncReportMapper;

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Resource
    private VariableDicMapper variableDicMapper;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    
    @Autowired
    ICompatibleService iCompatibleService;

    @Override
    public void reportProcess(Set<String> dateStrSet, int shardingTotalCount, List<Integer> shardingItems,String JobName) {
        long l = System.currentTimeMillis();
        // 分片获取所有客户
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(AuthShowProductor.NORMAL.getCode().byteValue());
        List<MarketingCustomer> customers = marketingCustomerMapper.selectByExampleAndShard(customerExample
                , shardingTotalCount, shardingItems);
        Map<String, Set<String>> userTypeMapByApiCode = getUserTypeMapByApiCode("");
        String other = "";
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(50, 50);
        List<String> smyApiCodes = (marketingCommonConfig.getSaMoYeTransferFileApiCodes() == null
                || marketingCommonConfig.getSaMoYeTransferFileApiCodes().size() <= 0)
                ? Arrays.asList("3710013")
                : marketingCommonConfig.getSaMoYeTransferFileApiCodes();
        for (String dateStr : dateStrSet) {
            for (MarketingCustomer customer : customers) {
                if(StringUtils.isNoneBlank(JobName)){
                    Boolean action = iCompatibleService.isAction(customer.getExtendConfigInfo(),JobName);
                    if(!action){
                        continue;
                    }
                }
                String apiCode = customer.getApiCode();
                String tCid = Optional.ofNullable(customer.getCid()).orElse(other).replace("-", other);
                boolean smy = smyApiCodes.contains(apiCode);
                // 获取场景
                Set<String> userTypeSet = userTypeMapByApiCode.getOrDefault(apiCode, Collections.emptySet());
                for (String userType : userTypeSet) {
                    String startDate = dateStr;
                    String endDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    List<String> requestDateList = new ArrayList<>();
                    try {
                        requestDateList = smy ? Arrays.asList(startDate) : transferSyncReportMapper.requestDatetikv_(tCid, apiCode, startDate, endDate, userType);
                    } catch (Exception ex) {
                        continue;
                    }
                    for (String requestDate : requestDateList) {
                        threadPool.submit(() -> {
                            TransferSyncReport report = smy ? transferSyncReportMapper.dateTimeMinMaxCountSMYtiflash_(apiCode, requestDate, userType)
                                    : transferSyncReportMapper.dateTimeMinMaxCounttiflash_(tCid, apiCode, requestDate, userType);
                            try {
                                Date appletBeginTime = report.getAppletBeginTime();
                                Date appletEndTime = report.getAppletEndTime();
                                Integer dataCount = report.getDataCount();
                                if (appletBeginTime == null || appletEndTime == null || dataCount == null || dataCount < 1) {
                                    return;
                                }
                                // 检索历史记录
                                TransferSyncReportExample example = new TransferSyncReportExample();
                                example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                                        .andAppletDateEqualTo(requestDate);
                                List<TransferSyncReport> list = findTransferSyncReportList(example);
                                if (CollectionUtils.isEmpty(list)) {
                                    // 添加新记录
                                    report.setUserType(userType);
                                    report.setAppletDate(requestDate);
                                    report.setCid(customer.getCid());
                                    report.setApiCode(apiCode);
                                    report.setShortName(customer.getShortName());
                                    report.setCreateTime(new Date());
                                    report.setUpdateTime(new Date());
                                    transferSyncReportMapper.insertSelective(report);
                                } else {
                                    // 更新历史记录
                                    TransferSyncReport transferSyncReport = list.get(0);
                                    if (appletBeginTime.equals(transferSyncReport.getAppletBeginTime())) {
                                        report.setAppletBeginTime(null);
                                    }
                                    if (appletEndTime.equals(transferSyncReport.getAppletEndTime())) {
                                        report.setAppletEndTime(null);
                                    }
                                    if (dataCount.equals(transferSyncReport.getDataCount())) {
                                        report.setDataCount(null);
                                    }
                                    if (report.getAppletBeginTime() != null || report.getAppletEndTime() != null
                                            || report.getDataCount() != null) {
                                        report.setId(transferSyncReport.getId());
                                        report.setUpdateTime(new Date());
                                        transferSyncReportMapper.updateByPrimaryKeySelective(report);
                                    }
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                            }
                        });
                    }

                }
            }
        }
        threadPool.shutdown();
        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (Exception e) {

            }
        }
        log.warn("转化记录-同步记录操作执行完成，耗时{}s", (System.currentTimeMillis() - l) / 1000);
    }

    /**
     * 2022/6/29 17:46
     * 获取apiCode的场景
     * key:apiCode
     * value:userType set
     *
     * @param apiCode apicode
     */
    @SuppressWarnings("all")
    private Map<String, Set<String>> getUserTypeMapByApiCode(String apiCode) {
        VariableDicExample dic = new VariableDicExample();
        VariableDicExample.Criteria criteria = dic.createCriteria().andIsDelEqualTo(1);
        if (StringUtils.isNotBlank(apiCode)) {
            criteria.andApiCodeEqualTo(apiCode);
        }
        List<VariableDic> dicList = variableDicMapper.selectByExample(dic);
        return dicList.parallelStream().collect(Collectors.groupingBy(VariableDic::getApiCode
                , Collectors.mapping(VariableDic::getFieldValue, Collectors.toSet())));
    }

    @Override
    public void reportProcess(Set<String> dateStrSet) {
        reportProcess(dateStrSet, 1, Collections.singletonList(0),null);
    }

    @Override
    public List<TransferSyncReport> findTransferSyncReportList(TransferSyncReportExample example) {
        return transferSyncReportMapper.selectByExample(example);
    }

    @Override
    public PageResultReturn getTransferSyncReportList(int current, int size, String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes) {
        Map<String, Object> params = queryParams(cidOrName, appletTimeStart, appletTimeEnd, apiCodes, userTypes);
        PageHelper.startPage(current, size);
        List<TransferSyncReportVO> list = transferSyncReportMapper.selectList(params);
        return PageResultReturn.setPageResult(list, current, size);
    }

    @Override
    public Map<String, String> getTransferSyncReportListTotal(String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes) {
        Map<String, Object> params = queryParams(cidOrName, appletTimeStart, appletTimeEnd, apiCodes, userTypes);
        Map<String, String> map = new HashMap<>(2);
        List<TransferSyncReportNumVO> totalList = transferSyncReportMapper.getReportListTotaltiflash_(params);
        map.put("numTotal", totalList.stream().collect(Collectors.summingLong(TransferSyncReportNumVO::getNumTotal)).toString());
        return map;
    }

    /**
     * 2022/6/30 22:07
     * 组装参数
     */
    private Map<String, Object> queryParams(String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes) {
        if (StringUtils.isNotEmpty(appletTimeEnd)) {
            appletTimeEnd = LocalDate.parse(appletTimeEnd, DateTimeFormatter.ISO_LOCAL_DATE).plusDays(1)
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        if (StringUtils.isNotEmpty(cidOrName) && cidOrName.contains("_")) {
            cidOrName = cidOrName.replace("_", "\\_");
        }
        Map<String, Object> params = new HashMap<>(8);
        params.put("cidOrName", cidOrName);
        params.put("appletTimeEnd", appletTimeEnd);
        params.put("appletTimeStart", appletTimeStart);
        if (StringUtils.isNotBlank(apiCodes)) {
            String[] split = apiCodes.split(",");
            params.put("apiCodeList", Arrays.asList(split));
        }
        if (StringUtils.isNotBlank(userTypes)) {
            String[] split = userTypes.split(",");
            params.put("userTypeList", Arrays.asList(split));
        }
        return params;
    }
}
