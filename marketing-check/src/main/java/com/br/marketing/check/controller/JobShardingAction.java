package com.br.marketing.check.controller;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.spring.ContainerContext;
import com.dangdang.ddframe.job.internal.job.AbstractElasticJob;
import com.dangdang.ddframe.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JobShardingAction {

    @Value("${SERVER_LISTS}")
    private String zklist;

    @Value("${NAMESPACE}")
    private String namespace;

    @GetMapping("/cleanSharding")
    public String cleanSharding(String jobName) {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(zklist, namespace);
        CoordinatorRegistryCenter result = new ZookeeperRegistryCenter(zkConfig);
        result.init();
        TreeCache treeCache = (TreeCache) result.getRawCache("/");
        if (treeCache == null) {
            result.addCacheData("/");
        }
        if(StringUtils.isNotBlank(jobName)){
            String path = "/".concat(jobName).concat("/leader/sharding/necessary");
            result.persist(path,"");
        }else {
            Map<String, AbstractElasticJob> beansOfType = ContainerContext.applicationContext.getBeansOfType(AbstractElasticJob.class);
            for (String key : beansOfType.keySet()) {
                result.persist("/".concat(key.substring(0,1).toUpperCase() + key.substring(1,key.length())).concat("/leader/sharding/necessary"),"");
            }
        }
//        result.persist("/TaskTransferSyncReportJob/leader/sharding/necessary","");
//        result.persist(new JobNodePath("taskTransferSyncReportJob").getExecutionNodePath(),"");
//        new CoordinatorRegistryCenter().getChildrenKeys()
//        ContainerContext.applicationContext.getBeansOfType(AbstractElasticJob.class).get("taskTransferSyncReportJob").getJobFacade()
        return "clean sharding success";
    }
}
