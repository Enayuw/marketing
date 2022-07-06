package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.RedisService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.HaloHistoryCleanService;
import com.br.marketing.strategy.HaloCleanHistoryHandler;
import com.br.marketing.thread.HaloCleanHistoryThread;
import com.br.marketing.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * halo洗数实现
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.service.Impl
 * @Description: halo洗数实现
 * @CreateTime: 2022-07-01 10 :35
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Service
@Slf4j
public class HaloHistoryCleanServiceImpl implements HaloHistoryCleanService {

    @Autowired
    RedisChgService redisChgService;


    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Autowired
    private RabbitMqProducter producter;

    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.outsideSecretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.outsideAppName:00}")
    private String appName;



    @Override
    public ApiResult<Boolean> cleanHistory(String jsonData) {

        log.warn("清洗数据接口入参：{}",jsonData);
        //String apiCode = '';
        JSONObject jsonObject = JSON.parseObject(jsonData);
        String cid = jsonObject.getString("cid");
        boolean exists = redisChgService.exists("cid-halo-button" + cid);
        if (exists) {
            return new ApiResult<Boolean>().fail(ServiceResultEnum.HALOBUTTONDISABLE);
        }
        JSONArray dataArray = jsonObject.getJSONArray("dataArray");
        List<MarketingSyncUser> marketingSyncUserList = new ArrayList<>();
        if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataJson = dataArray.getJSONObject(i);
                String apiCode = dataJson.getString("apiCode");
                String appletDate = dataJson.getString("appletDate");
                MarketingSyncUser marketingSyncUserMaxId = marketingSyncInfoMapper.getMarketingSyncMaxIdByAppletDate(apiCode,appletDate);
                if(marketingSyncUserMaxId!=null){
                    marketingSyncUserList.add(marketingSyncUserMaxId);
                }
            }
            if (marketingSyncUserList.size() == 0 && marketingSyncUserList.isEmpty()) {
                return new ApiResult<Boolean>().fail(ServiceResultEnum.HALO_NO_DATA);
            }
        }else {
            return new ApiResult<Boolean>().fail("入参数据异常");
        }
        redisChgService.setex("cid-halo-button"+cid,cid, TimeUtils.getRemainSecondsOneDay(new Date()));
        //redisAuthService.set("cid-halo-button" + cid, cid, 120);
        producter.send(MQConstants.ROUTING_KEY_MARKETING_HALUO_CLEAN_HISTORY, jsonData);
        return new ApiResult<Boolean>().success().setData(true);
    }

    @Override
    public void handlerCleanHistory(String jsonData) {
        JSONObject jsonObject = JSON.parseObject(jsonData);
        JSONArray dataArray = jsonObject.getJSONArray("dataArray");
        if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
                ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100);
                JSONObject dataJson = dataArray.getJSONObject(i);
                String apiCode = dataJson.getString("apiCode");
                String appletDate = dataJson.getString("appletDate");
                StringBuilder content = new StringBuilder();
                int  allCount =  marketingSyncInfoMapper.selectCountError(apiCode,appletDate);
                handlerCleanHistory(appletDate, apiCode, threadPool);
                //关闭线程池
                threadPool.shutdown();
                //当调用shutdown()方法后，并且所有提交的任务完成后返回为true;
                while (!threadPool.isTerminated()) ;

                int errorCount =  marketingSyncInfoMapper.selectCountError(apiCode,appletDate);
                content.append("apiCode：".concat(apiCode).concat("，"))
                        .append("清洗数据量：".concat(String.valueOf(allCount)).concat("，"))
                        .append("错误数量：".concat(String.valueOf(errorCount)).concat("\r\n"));
                alarmClient.sendAlarm(content.toString(), "哈啰洗库", appName, secretKey,
                        Constants.sendCodeMap.get("uploadSuccess"));
            }
        }
        log.info("所有线程都执行结束");
    }

    private void handlerCleanHistory(String appletDate, String apiCode, ThreadPoolExecutor threadPool) {

        // 查询上传时间内有问题的最大的id  即 循环结束的id
        MarketingSyncUser marketingSyncUserMaxId = marketingSyncInfoMapper.getMarketingSyncMaxIdByAppletDate(apiCode,appletDate);
        if (marketingSyncUserMaxId != null) {
            // 最大值为结束id
            Long endId = marketingSyncUserMaxId.getId();

            // 查询上传时间内有问题的最小的id  即 循环开始的id
            MarketingSyncUser marketingSyncUserMinId = marketingSyncInfoMapper.getMarketingSyncMinIdByAppletDate(apiCode,appletDate);

            // 记录开始id
            Long beginId = 0L;
            if (marketingSyncUserMinId != null) {
                beginId = marketingSyncUserMinId.getId();
            }

            log.warn("halo历史数据范围 beginId:{} endId:{}", beginId, endId);

            // 循环开关
            boolean pageFlag = true;

            // 记录结束id
            Long endIdLe;


            AtomicInteger errorMark = new AtomicInteger(0);
            while (pageFlag) {
                endIdLe = beginId + 5000;

                // 增加5000 后的id  大于 结束id ，即超出范围，将结束id 作为循环结束id
                if (endIdLe > endId) {
                    endIdLe = endId;
                }

                // 如果开始的id大于 结束的id 说明循环结束
                if (beginId > endIdLe) {
                    pageFlag = false;
                }
                //根据beginId 和 endIdLe查询 cell -1 即 status= 2 的n条数据
                List<MarketingSyncUser> marketingSyncUserByMaxIdAndMinIdList = marketingSyncInfoMapper.getByMaxIdAndMinId(apiCode, beginId, endIdLe);
                if (marketingSyncUserByMaxIdAndMinIdList != null && !marketingSyncUserByMaxIdAndMinIdList.isEmpty()) {

                    for (MarketingSyncUser user : marketingSyncUserByMaxIdAndMinIdList) {
                        Long id = user.getId();
                        //获取最大beginId
                        if (id.equals(endId)) {
                            pageFlag = false;
                        } else if (id > beginId) {
                            beginId = id;
                        }
                        // 查询离当前时间最近的有cell的数据 正常的数据
                        MarketingSyncUser cellFromCurrentUser = marketingSyncInfoMapper.getCellFromCurrent(apiCode, user.getCustNum());
                        threadPool.submit(new HaloCleanHistoryThread(user, marketingSyncInfoMapper,cellFromCurrentUser));
                    }
                } else {
                    beginId = beginId + 5000;
                }
            }


        }
    }

}
