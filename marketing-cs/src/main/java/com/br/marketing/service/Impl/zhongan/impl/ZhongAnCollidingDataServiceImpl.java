package com.br.marketing.service.Impl.zhongan.impl;

import com.br.marketing.bo.ZhongAnCollidingDataBO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ZhongAnCollidingConfig;
import com.br.marketing.mapper.ZhongAnCollidingConfigMapper;
import com.br.marketing.service.Impl.zhongan.ZhongAnCollidingDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class ZhongAnCollidingDataServiceImpl implements ZhongAnCollidingDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ZhongAnCollidingConfigMapper zhongAnCollidingConfigMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String apiCode = "3710048";
        String bizDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String parameter = context.getJobParameter();
        if (StringUtils.isNotBlank(parameter)) {
            String[] split = parameter.split("#");
            apiCode = split[0];
            bizDate = split[1];
        }
        Integer limit = marketingCommonConfig.getWuBaCollidingDataSubmitPageSize();
        // 获取待上报数据
        List<ZhongAnCollidingConfig> configs = zhongAnCollidingConfigMapper.queryZhongAnCollidingConfigByPriority();
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (ZhongAnCollidingConfig config : configs) {
            String configSql = config.getQuerySql();
            String replaceSql = configSql.replace("#{apiCode}", "\"" + apiCode + "\"").replace("#{bizDate}", bizDate);
            String completeSql = replaceSql.concat(" limit " + limit);
            boolean flag = true;
            while (flag) {
                List<ZhongAnCollidingDataBO> collidingDatas = zhongAnCollidingConfigMapper.queryCollidingDataByConfigSql(completeSql);
                if (CollectionUtils.isEmpty(collidingDatas)) {
                    flag = false;
                }
                List<String> cells = collidingDatas.stream().map(ZhongAnCollidingDataBO::getMobileMd5).collect(Collectors.toList());
                cells.forEach(mobileMd5 -> {

                });

            }
        }
    }

}
