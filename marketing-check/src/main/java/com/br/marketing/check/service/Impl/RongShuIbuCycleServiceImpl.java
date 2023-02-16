package com.br.marketing.check.service.Impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.service.RongShuIbuCycleService;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.RongshuCycleDataMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.IRongShuPushDaasService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.ArtificalIbuHandler;
import com.google.api.client.util.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 榕树周期性数据推送电销 业务实现
 *
 * @author Lizhen
 * @dateTime 2023/02/14 14:32
 */
@Service
@Slf4j
public class RongShuIbuCycleServiceImpl implements RongShuIbuCycleService {

    @Autowired
    private IRongShuPushDaasService iRongShuPushDaasService;

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private RongshuCycleDataMapper rongshuCycleDataMapper;

    @Resource
    private IPeriodOfValidityService iPeriodOfValidityService;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private ArtificalIbuHandler artificalIbuHandler;


    @Override
    public void pushCycleDataToDaas() {
        List<Integer> dayList = marketingCommonConfig.getRongShuCyclePushDays();
        List<String> pushDateList = Lists.newArrayList();
        dayList.forEach(day -> {
            pushDateList.add(LocalDate.now().minusDays(day).toString());
        });
        List<RongshuCycleData> rongshuCycleDataList = rongshuCycleDataMapper.getCycleData(pushDateList);
        String tcId = tableCreateService.getTcId(rongshuCycleDataList.get(0).getApiCode());
        for (Iterator<RongshuCycleData> iterator = rongshuCycleDataList.iterator(); iterator.hasNext(); ) {
            RongshuCycleData rongshuCycleData = iterator.next();
            //进行条件剔除
            if (iRongShuPushDaasService.isFilter(rongshuCycleData.getApiCode(), rongshuCycleData.getCustNum(), tcId)) {
                iterator.remove();
            }
            //TODO： 组装数据
            //artificalIbuHandler.call();
        }
    }
}
