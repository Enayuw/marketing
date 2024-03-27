package com.br.marketing.xc.job;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.service.Impl.xc.XieChengRobDataCollidingService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class XieChengRobDataCollidingJob extends AbstractSimpleElasticJob {

    @Resource
    private XieChengRobDataCollidingService xieChengRobDataCollidingService;
    @Resource
    private XieChengCollidingDataPackageMapper xieChengCollidingDataPackageMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        XieChengCollidingDataPackageExample example = new XieChengCollidingDataPackageExample();
        example.createCriteria().andCollidingTimeLessThanOrEqualTo(new Date()).andIsDeleteEqualTo(0);
        example.setOrderByClause("priority desc");
        List<XieChengCollidingDataPackage> xieChengCollidingDataPackages = xieChengCollidingDataPackageMapper.selectByExample(example);
        List<Long> packageIds = xieChengCollidingDataPackages.stream().map(XieChengCollidingDataPackage::getId).collect(Collectors.toList());
        String param = context.getJobParameter();
        if (StringUtils.isNotEmpty(param)) {
            packageIds = Splitter.on(",").splitToList(param).stream().map(Long::parseLong).collect(Collectors.toList());
        }
        xieChengRobDataCollidingService.collidingData(packageIds);
    }

}
