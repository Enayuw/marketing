package com.br.marketing.service.Impl;

import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.TransferSyncReportMapper;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.TransferSyncReportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.constraints.Null;
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
        List<MarketingCustomer> customers = marketingCustomerMapper.selectByExampleAndShard(customerExample
                , shardingTotalCount, shardingItems);
        Map<String, Set<String>> userTypeMapByApiCode = getUserTypeMapByApiCode("");
        // 获取全部转化表名
        List<String> tables = getTransferTableList();
        for (String dateStr : dateStrSet) {
            for (MarketingCustomer customer : customers) {
                if (AuthShowProductor.NORMAL.getCode().byteValue() == customer.getStatus()) {
                    String apiCode = customer.getApiCode();
                    String tCid = Optional.ofNullable(customer.getCid()).orElse("")
                            .replace("-", "");
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
                    for (String userType : userTypeSet) {
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
    private Map<String, Set<String>> getUserTypeMapByApiCode(@Null String apiCode) {
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
        String queryTransferTableSql = "SHOW TABLES like 'b_marketing_transfer_%'";
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
}
