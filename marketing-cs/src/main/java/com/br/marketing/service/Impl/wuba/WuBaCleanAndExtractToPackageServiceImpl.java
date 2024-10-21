package com.br.marketing.service.Impl.wuba;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.mapper.WubaCollidingBatchNoMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @description 58新客撞库日志清洗及数据提取实现
 * @author hedongshuo
 * @date 2024/10/18 13:59
 **/
@Service
@Slf4j
public class WuBaCleanAndExtractToPackageServiceImpl implements WuBaCleanAndExtractToPackageService{

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    WubaCollidingBatchNoMapper wubaCollidingBatchNoMapper;

    private static final String DATA_SOURCE_TYPE = "data_source_type";

    private static final String CREATE_TIME = "create_time";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        List<String> apiCodes = marketingCommonConfig.getWubaCollidingApiCodes();
        apiCodes.forEach((String apiCode) ->{
            process(apiCode);
        });
    }

    private void process(String apiCode) {
        HashMap<String, Object> config = marketingCommonConfig.getWuBaCleanAndExtractToPackageConfig();
        JSONArray periods = (JSONArray)config.get("periods");
        String periodStartDate = config.get("startDate").toString();
        int threadNum = Integer.parseInt(config.get("threadNum").toString());
        int partitionSize = Integer.parseInt(config.get("partitionSize").toString());
        //1.查询【b_wuba_colliding_data_batch_no】
        LocalDate localDate = LocalDate.now();
        StringBuilder condition = new StringBuilder("and (");
        for (int i = 0; i < periods.size(); i++) {
            if (i > 0) {
                condition.append("or");
            }
            Object periodObject = periods.get(i);
            JSONObject periodJson = (JSONObject) JSONObject.toJSON(periodObject);
            String dataSourceType = periodJson.getString("dataSourceType");
            String period = periodJson.getString("period");
            condition.append("(").append(DATA_SOURCE_TYPE).append(" = '").append(dataSourceType).append("'");
            if (StringUtils.isNotEmpty(period)) {
                String periodEndDate = localDate.minusDays(Integer.parseInt(period)).toString();
                condition.append(" and ").append(CREATE_TIME).append(" < '").append(periodEndDate).append("'");
            }
            condition.append(")");
        }
        condition.append(")");
        condition.append(CREATE_TIME).append(" >= '").append(periodStartDate).append("'");
        List<Map<String, Object>> batchNos = wubaCollidingBatchNoMapper.selectBatchNoByTimeRange(apiCode, condition.toString());
        if (CollectionUtils.isEmpty(batchNos)) {
            log.warn("58新客撞库日志清洗及数据提取-无可提取的批次");
            return;
        }
        //2.将结果集，按pushDate分组
        Map<String, List<Map<String, Object>>> batchNosByPushDate = batchNos.stream()
                .collect(Collectors.groupingBy(((Map<String, Object> batchData) -> batchData.get("pushDate").toString())));
        Set<Map.Entry<String, List<Map<String, Object>>>> batchNosEntries = batchNosByPushDate.entrySet();
        for (Map.Entry<String, List<Map<String, Object>>> batchNoEntry : batchNosEntries) {
            String pushDate = batchNoEntry.getKey();
            List<Map<String, Object>> batchNosForPushDate = batchNoEntry.getValue();
            //查询pushDate
            String pushEndDate = DateHelper.strToLocalDate(pushDate).plusDays(1).toString();

        }


    }
}
