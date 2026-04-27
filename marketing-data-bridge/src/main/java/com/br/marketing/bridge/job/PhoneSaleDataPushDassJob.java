package com.br.marketing.bridge.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.ZhongYuanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Description 电销表数据推送dass人工，承接 SftpToDbByResultDataJob
 * @Author hong.chen
 * @CreateTime 2024/09/10
 */
@Component
@Slf4j
public class PhoneSaleDataPushDassJob extends AbstractSimpleElasticJob {
    @Autowired
    PushDataService pushDataService;

    @Autowired
    LocalFileMapper localFileMapper;

    @Autowired
    ZhongYuanService zhongYuanService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        LocalFileExample localFileExample = new LocalFileExample();
        localFileExample.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.DX.getValue()).andPushStatusEqualTo("0")
                .andStatusEqualTo("1").andCompleteIn(Lists.newArrayList("1", "3"));
        List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
        if (CollectionUtils.isEmpty(localFiles)) {
            return;
        }

        HashMap<String, List<String>> dxFileCustomize = marketingCommonConfig.getDxFileCustomize();
        JSONObject daasConfig = marketingCommonConfig.getDaasConfig();
        List zhongYuanList = dxFileCustomize.get("zhongYuan");

        // 获取special文件名前缀配置集合
        List<String> specialFileNamePrefixes = Lists.newArrayList();
        if (daasConfig != null && daasConfig.containsKey("specialFileNamePrefixes")) {
            specialFileNamePrefixes = daasConfig.getJSONArray("specialFileNamePrefixes").toJavaList(String.class);
        }

        for (LocalFile localFile : localFiles) {
            // 更新推送状态为推送中
            LocalFile updateFile = new LocalFile();
            updateFile.setId(localFile.getId());
            updateFile.setPushStatus("1");
            localFileMapper.updateByPrimaryKeySelective(updateFile);

            String fileName = localFile.getFileName();
            // 判断文件名是否以配置的任意前缀开始，并获取对应的配置
            String matchedPrefix = findMatchedPrefix(fileName, specialFileNamePrefixes);
            //文件名以csosnew开头，推送财富Daas接口
            if(fileName.startsWith("csosnew")){
                pushDataService.pushCsosDassData(localFile.getId());
            } else if (fileName.startsWith("update")) {
                pushDataService.pushUpdateDassData(localFile.getId());
            } else if (fileName.startsWith("weizhong")) {
                pushDataService.pushWeiZhongDassData(localFile.getId());
            } else if (matchedPrefix != null) {
                // 获取该前缀对应的配置（此时daasConfig必定不为空，因为matchedPrefix来自specialFileNamePrefixes）
                JSONObject prefixConfig = daasConfig.getJSONObject(matchedPrefix);
                
                // 判断是否使用新的动态分组逻辑（检查配置中是否包含groupByField）
                if (prefixConfig != null && prefixConfig.containsKey("groupByField")) {
                    pushDataService.pushDynamicGroupDassData(localFile.getId(), matchedPrefix, prefixConfig);
                } else {
                    // 兼容旧逻辑
                    pushDataService.pushSpecialDassData(localFile.getId(), matchedPrefix, prefixConfig);
                }
            } else {
                pushDataService.pushDassData(localFile.getId());
            }
            if (zhongYuanList.contains(localFile.getApiCode())) {
                zhongYuanService.pushOutBoundData(localFile.getId());
            }

            // 更新推送状态为推送完成
            updateFile.setPushStatus("2");
            localFileMapper.updateByPrimaryKeySelective(updateFile);
        }
    }

    /**
     * 查找匹配的文件名前缀
     * @param fileName 文件名
     * @param prefixes 前缀列表
     * @return 匹配的前缀，如果没有匹配则返回null
     */
    private String findMatchedPrefix(String fileName, List<String> prefixes) {
        if (CollectionUtils.isEmpty(prefixes) || fileName == null) {
            return null;
        }

        for (String prefix : prefixes) {
            if (fileName.startsWith(prefix)) {
                return prefix;
            }
        }

        return null;
    }
}
