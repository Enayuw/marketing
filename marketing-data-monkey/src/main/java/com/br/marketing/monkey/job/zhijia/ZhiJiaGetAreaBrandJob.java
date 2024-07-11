package com.br.marketing.monkey.job.zhijia;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.zhijia.ZhiJiaClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.ZhijiaCityConfig;
import com.br.marketing.entity.ZhijiaCityConfigExample;
import com.br.marketing.entity.ZhijiaCountyConfig;
import com.br.marketing.entity.ZhijiaCountyConfigExample;
import com.br.marketing.mapper.ZhijiaCityConfigMapper;
import com.br.marketing.mapper.ZhijiaCountyConfigBMapper;
import com.br.marketing.service.Impl.zhijia.ZhiJiaDataProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * @ClassName ZhiJiaGetAreaBrandJob
 * @Description 之家获取区域及品牌人任务
 * @Author zhen.Li1
 * @Date 2024/7/10 16:02
 */
@Component
@Slf4j
public class ZhiJiaGetAreaBrandJob extends AbstractSimpleElasticJob {

    @Autowired
    ZhiJiaDataProcessService zhiJiaDataProcessService;

    @Resource
    private ZhiJiaClient zhiJiaClient;

    @Resource
    ZhijiaCityConfigMapper zhijiaCityConfigMapper;

    @Resource
    ZhijiaCountyConfigBMapper zhijiaCountyConfigBMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        //获取市区县
        getCity();
        //获取品牌和车系
        zhiJiaDataProcessService.getBrandAndseries();

    }

    private void getCity() {
        String token = zhiJiaDataProcessService.getToken();
        if (StringUtils.isEmpty(token)) {
            log.error("获取token异常");
            return;
        }
        JSONObject jsonObject = new JSONObject();
        Result<JSONObject> result = zhiJiaClient.getCityAndCounty(token);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            jsonObject = result.getData();
        } else {
            log.error("之家省市区调用异常,result= {}", result.getMessage());
        }
        JSONObject resultJson = jsonObject.getJSONObject("result");
        JSONArray cityList = resultJson.getJSONArray("city");
        cityList.forEach(cityJson -> {
            JSONObject city = (JSONObject) cityJson;
            Integer cid = city.getInteger("cid");
            String cname = city.getString("cname");
            ZhijiaCityConfigExample zhijiaCityConfigExample = new ZhijiaCityConfigExample();
            zhijiaCityConfigExample.createCriteria()
                    .andCIdEqualTo(cid);
            List<ZhijiaCityConfig> zhijiaCityConfig = zhijiaCityConfigMapper.selectByExample(zhijiaCityConfigExample);
            if (CollectionUtils.isEmpty(zhijiaCityConfig)) {
                ZhijiaCityConfig cityConfig = new ZhijiaCityConfig();
                cityConfig.setCId(cid);
                cityConfig.setCName(cname);
                cityConfig.setCreateTime(new Date());
                cityConfig.setUpdateTime(new Date());
                cityConfig.setUploadDate(LocalDate.now().toString());
                zhijiaCityConfigMapper.insert(cityConfig);
            } else {
                ZhijiaCityConfig cityConfig = new ZhijiaCityConfig();
                cityConfig.setCName(zhijiaCityConfig.get(0).getCName());
                cityConfig.setUpdateTime(new Date());
                cityConfig.setUploadDate(LocalDate.now().toString());
                cityConfig.setId(zhijiaCityConfig.get(0).getId());
                zhijiaCityConfigMapper.updateByPrimaryKeySelective(cityConfig);
            }
        });
        JSONArray countyList = resultJson.getJSONArray("county");
        countyList.forEach(countyJson -> {
            JSONObject county = (JSONObject) countyJson;
            Integer cid = county.getInteger("cid");
            Integer countyid = county.getInteger("countyid");
            String countyname = county.getString("countyname");
            ZhijiaCountyConfigExample zhijiaCountyConfigExample = new ZhijiaCountyConfigExample();
            zhijiaCountyConfigExample.createCriteria()
                    .andCountyIdEqualTo(countyid);
            List<ZhijiaCountyConfig> countyConfigList = zhijiaCountyConfigBMapper.selectByExample(zhijiaCountyConfigExample);
            if (CollectionUtils.isEmpty(countyConfigList)) {
                ZhijiaCountyConfig countyConfig = new ZhijiaCountyConfig();
                countyConfig.setCId(cid);
                countyConfig.setCountyId(countyid);
                countyConfig.setCountyName(countyname);
                countyConfig.setCreateTime(new Date());
                countyConfig.setUpdateTime(new Date());
                countyConfig.setUploadDate(LocalDate.now().toString());
                zhijiaCountyConfigBMapper.insert(countyConfig);
            } else {
                ZhijiaCountyConfig countyConfig = new ZhijiaCountyConfig();
                countyConfig.setCountyName(countyConfigList.get(0).getCountyName());
                countyConfig.setCId(cid);
                countyConfig.setUpdateTime(new Date());
                countyConfig.setUploadDate(LocalDate.now().toString());
                countyConfig.setId(countyConfigList.get(0).getId());
                zhijiaCountyConfigBMapper.updateByPrimaryKeySelective(countyConfig);
            }
        });

    }
}
