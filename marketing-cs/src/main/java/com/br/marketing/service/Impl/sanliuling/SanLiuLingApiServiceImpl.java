package com.br.marketing.service.Impl.sanliuling;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.sanliuling.SanLiuLingClient;
import com.br.marketing.client.sanliuling.SanLiuLingTrafficReq;
import com.br.marketing.client.sanliuling.SanLiuLingTrafficResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.SanLiuLingPpData;
import com.br.marketing.entity.SanLiuLingPpDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SanLiuLingPpDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ClassName SanLiuLingApiServiceImpl
 * @Author kongbx
 * @Date 2025/6/20 15:54
 */
@Service
@Slf4j
public class SanLiuLingApiServiceImpl implements SanLiuLingApiService {

    @Resource
    private SanLiuLingPpDataMapper sanLiuLingPpDataMapper;
    @Resource
    private SanLiuLingClient sanLiuLingClient;
    @Autowired
    LocalFileMapper localFileMapper;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【360-pp流量业务营销】";

    @Override
    public void pushTrafficData(LocalFile localFile) {
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5);
        localFile.setPushStartTime(new Date());
        boolean actionMark = true;
        Long minId = null;
        int total = 0;
        while (actionMark) {
            if (marketingCommonConfig.getSanLiuLingTrafficThreadNum() != null) {
                ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, marketingCommonConfig.getSanLiuLingTrafficThreadNum());
            }
            Long id = localFile.getId();

            // 排除当天已推送的手机号
            List<SanLiuLingPpData> dataList = sanLiuLingPpDataMapper.getTrafficData(id, minId);
            if (dataList.isEmpty()) {
                actionMark = false;
                continue;
            }

            minId = dataList.get(dataList.size() - 1).getId();

            // 去重
            List<SanLiuLingPpData> distinctList = new ArrayList<>(dataList.stream()
                    .collect(Collectors.toMap(
                            SanLiuLingPpData::getMobileNoMd5,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ))
                    .values());

            total += distinctList.size();
            List<List<SanLiuLingPpData>> partition = ListUtils.partition(distinctList, 100);

            partition.forEach((List<SanLiuLingPpData> p) -> {
                pool.submit(() -> buildDataAndPush(p, id));
            });

        }
        try {
            pool.shutdown();
            while (!pool.awaitTermination(5L, TimeUnit.SECONDS)) {
                log.warn(TITLE + "线程终止");
            }
        } catch (Exception ex) {
            pool.shutdownNow();
            log.error(TITLE + ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }

        SanLiuLingPpDataExample example = new SanLiuLingPpDataExample();
        example.createCriteria().andLocalIdEqualTo(localFile.getId()).andPushStatusEqualTo(2);
        int i = sanLiuLingPpDataMapper.countByExample(example);
        //全部更新成功
        if (total != 0 && total == i) {
            localFile.setPushEndTime(new Date());
            localFile.setPushNumber(total);
            localFile.setPushStatus("2");
            localFileMapper.updateByPrimaryKeySelective(localFile);
        }
    }

    private void buildDataAndPush(List<SanLiuLingPpData> p, Long id) {
        try {
            List<String> mobileMd5List = p.stream()
                    .map(SanLiuLingPpData::getMobileNoMd5)
                    .collect(Collectors.toList());

            SanLiuLingTrafficReq sanLiuLingTrafficReq = new SanLiuLingTrafficReq();
            sanLiuLingTrafficReq.setChannel("brllt");
            sanLiuLingTrafficReq.setMobile_md5(mobileMd5List);

            Result result = sanLiuLingClient.batchTrafficData(sanLiuLingTrafficReq);

            if (!Objects.equals(result.getCode(), ResultCode.SUCCESS.getValue())) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                        , TITLE + "调用360接口失败，失败原因：" + JSONObject.toJSONString(result)));
                return;
            }

            Object data = result.getData();
            SanLiuLingTrafficResp sanLiuLingTrafficResp = JSON.parseObject(data.toString(), SanLiuLingTrafficResp.class);

            if ("40404".equals(sanLiuLingTrafficResp.getCode())) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                        , TITLE + "超出每日请求次数，请手动关闭 SanLiuLingTrafficJob ！！！"));
                return;
            }

            if (!"200".equals(sanLiuLingTrafficResp.getCode())) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                        , TITLE + "调用360接口失败，失败原因：" + JSONObject.toJSONString(result)));
                return;
            }

            List<String> respMobileMd5List = sanLiuLingTrafficResp.getData().getMobile_md5();
            // 校验返回数量和请求数量一致
            if (respMobileMd5List.size() != p.size()) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                        , TITLE + "返回的mobileMd5数量与请求数量不一致,id:" + id + "，请求量级：" + p.size() + "，返回量级：" + respMobileMd5List.size()));
                return;
            }
            // 批量更新
            List<SanLiuLingPpData> updateList = new ArrayList<>();
            for (int i = 0; i < p.size(); i++) {
                SanLiuLingPpData updateData = new SanLiuLingPpData();
                updateData.setId(p.get(i).getId());
                updateData.setPushStatus(2);
                updateData.setMobileResult(respMobileMd5List.get(i));
                updateList.add(updateData);
            }
            if (!updateList.isEmpty()) {
                sanLiuLingPpDataMapper.batchUpdatePushStatusAndResult(updateList);
            }
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                    , TITLE + "调用360接口失败"));
        }

    }


}


