package com.br.marketing.elasticshard;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.spring.ContainerContext;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategy;
import com.dangdang.ddframe.job.internal.sharding.strategy.JobShardingStrategyOption;
import com.dangdang.ddframe.job.plugin.sharding.strategy.AverageAllocationJobShardingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class K8sClusterShardingStrategy implements JobShardingStrategy  {


    private AverageAllocationJobShardingStrategy averageAllocationJobShardingStrategy = new AverageAllocationJobShardingStrategy();


    @Override
    public Map<String, List<Integer>> sharding(List<String> serversList, JobShardingStrategyOption option) {

        String cluster = "";
        Map<String, MarketingCommonConfig> beansOfType = ContainerContext.applicationContext.getBeansOfType(MarketingCommonConfig.class);
        MarketingCommonConfig marketingCommonConfig = beansOfType.get("marketingCommonConfig");
        String jobCluster = marketingCommonConfig.getJobCluster().get(option.getJobName().toLowerCase());
        cluster = StringUtils.isNotBlank(jobCluster)?jobCluster:"";

        if(StringUtils.isBlank(cluster)){
            String defaultCluster = marketingCommonConfig.getJobCluster().get("default");
            cluster = StringUtils.isNotBlank(defaultCluster)?defaultCluster:"";
        }

        List<String> filterServiceList = new ArrayList<>();
        if (StringUtils.isBlank(cluster)) {
            filterServiceList = serversList;
        }else{
            switch (cluster){
                case "zw":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("10.5")||t.startsWith("10.3")).collect(Collectors.toList());
                    break;
                case "yz":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("10.7")||t.startsWith("10.61")).collect(Collectors.toList());
                    break;
                case "zwpro":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("10.5")).collect(Collectors.toList());
                    break;
                case "yzpro":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("10.7")).collect(Collectors.toList());
                    break;
                case "zwfz":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("10.3")).collect(Collectors.toList());
                    break;
                case "yzfz":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("10.61")).collect(Collectors.toList());
                    break;
                case "pre":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("172.98")).collect(Collectors.toList());
                    break;
                case "dev":
                    filterServiceList = serversList.stream().filter(t->t.startsWith("172.0")).collect(Collectors.toList());
                    break;
                case "all":
                    filterServiceList = serversList;
                    break;
                default:
                    filterServiceList = serversList;
                    break;

            }
        }
        long jobNameHash = option.getJobName().hashCode();
        if (0 == jobNameHash % 2) {
            Collections.reverse(filterServiceList);
        }
        return averageAllocationJobShardingStrategy.sharding(filterServiceList, option);
    }


}
