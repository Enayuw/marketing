package com.br.marketing.service.Impl;

import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TransferSyncReportMapper;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.TransferSyncReportService;
import com.br.marketing.vo.TransferSyncReportVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Resource
    private JdbcTemplate jdbcTemplate;


    @Override
    public void reportProcess(Set<String> dateStrSet, int shardingTotalCount, List<Integer> shardingItems) {
        // 分片获取所有客户
        MarketingCustomerExample customerExample = new MarketingCustomerExample();
        customerExample.createCriteria().andStatusEqualTo(AuthShowProductor.NORMAL.getCode().byteValue());
        List<MarketingCustomer> customers = marketingCustomerMapper.selectByExampleAndShard(customerExample
                , shardingTotalCount, shardingItems);
        Map<String, Set<String>> userTypeMapByApiCode = getUserTypeMapByApiCode("");
        log.warn("1#apiCode的场景：{}", userTypeMapByApiCode.toString());
        String other = "";
        // 获取全部转化表名
        List<String> tables = getTransferTableList();
        log.warn("1.1#tables：{}", Arrays.toString(tables.toArray()));
        for (String dateStr : dateStrSet) {
            log.warn("2#dateStr：{}", dateStr);
            for (MarketingCustomer customer : customers) {
                String apiCode = customer.getApiCode();
                String tCid = Optional.ofNullable(customer.getCid()).orElse(other).replace("-", other);
                log.warn("3#apiCode：{};tCid:{}", apiCode, tCid);
                boolean isSmy;
                if (tables.contains("b_marketing_transfer_sync_" + tCid)) {
                    isSmy = false;
                } else if (tables.contains("b_marketing_transfer_" + apiCode)) {
                    isSmy = true;
                } else {
                    continue;
                }
                // 获取场景
                Set<String> userTypeSet = userTypeMapByApiCode.getOrDefault(apiCode, Collections.emptySet());
                log.warn("4#userTypeSet：{}", userTypeSet.toArray());
                for (String userType : userTypeSet) {
                    try {
                        TransferSyncReport report;
                        if (isSmy) {
                            report = transferSyncReportMapper.dateTimeMinMaxCountSMY(apiCode, dateStr, userType);
                        } else {
                            report = transferSyncReportMapper.dateTimeMinMaxCount(tCid, apiCode, dateStr, userType);
                        }
                        Date appletBeginTime = report.getAppletBeginTime();
                        Date appletEndTime = report.getAppletEndTime();
                        Integer dataCount = report.getDataCount();
                        if (appletBeginTime == null || appletEndTime == null || dataCount == null || dataCount < 1) {
                            continue;
                        }
                        // 检索历史记录
                        TransferSyncReportExample example = new TransferSyncReportExample();
                        example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                                .andAppletDateEqualTo(dateStr);
                        List<TransferSyncReport> list = findTransferSyncReportList(example);
                        if (CollectionUtils.isEmpty(list)) {
                            // 添加新记录
                            report.setUserType(userType);
                            report.setAppletDate(dateStr);
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
                }
            }
        }
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

    /**
     * 获取全部转化表名
     *
     * @dateTime 2022/6/30 20:33
     */
    private List<String> getTransferTableList() {
        String queryTransferTableSql = "SHOW TABLES LIKE 'b\\_marketing\\_transfer\\_%'";
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(queryTransferTableSql);
        return maps.parallelStream().map(m -> m.values().iterator().next().toString()).collect(Collectors.toList());
    }

    @Override
    public void reportProcess(Set<String> dateStrSet) {
        reportProcess(dateStrSet, 1, Collections.singletonList(0));
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
        Long total = transferSyncReportMapper.getReportListTotal(params);
        map.put("numTotal", ObjectUtils.isEmpty(total) ? "0" : DecimalFormat.getNumberInstance().format(total));
        return map;
    }

    /**
     * 2022/6/30 22:07
     * 组装参数
     */
    private Map<String, Object> queryParams(String cidOrName, String appletTimeStart
            , String appletTimeEnd, String apiCodes, String userTypes) {
        if (StringUtils.isNotEmpty(appletTimeEnd)) {
            appletTimeEnd = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
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
