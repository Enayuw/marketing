package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotDataVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.XieChengSmsPushToTransferService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @Description XieChengSmsPushToTransferServiceImpl
 * @Author hong.chen
 * @CreateTime 2023/07/13
 */
@Service
@Slf4j
public class XieChengSmsPushToTransferServiceImpl implements XieChengSmsPushToTransferService {
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    @Qualifier("xieChengSmsThreadPool")
    ThreadPoolExecutor pool;

    @Override
    public Result consumerXiechengSmsCollidingVtUser(String msg) {
        long start = System.currentTimeMillis();
        JSONArray jsonArray = JSON.parseArray(msg);

        Date nowDayEndTime = DateHelper.getNowDayEndTime();
        String xieChengSmsApiCode = marketingCommonConfig.getXieChengSmsApiCode();
        String cid = tableCreateService.getCId(xieChengSmsApiCode);

        List<String> sha256CodeList = new ArrayList<>();
        for (Object o : jsonArray) {
            sha256CodeList.add(o.toString());
        }

        if (CollectionUtils.isEmpty(sha256CodeList)) {
            // ack
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }

        // 推送客服数据集合
        List<ConversionData> conversionDataList = new CopyOnWriteArrayList<>();

        // 动态修改线程池
        modifyCorePoolSize();

        CountDownLatch countDownLatch = new CountDownLatch(sha256CodeList.size());
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (String sha256Code : sha256CodeList) {
            long hashCodeLong = sha256Code.hashCode() & 0x7FFFFFFFL;
            String dataId = hashCodeLong + currentDate;
            pool.submit(() -> buildConversionDataList(nowDayEndTime, cid, sha256Code, dataId, countDownLatch, conversionDataList));
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("携程新场景短信撞库：" + e.getMessage(), e);
        }

        if (CollectionUtils.isEmpty(conversionDataList)) {
            // ack
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        // 每500条数据一个批次
        int pageSize = 500;
        int totalCount = conversionDataList.size();
        int pageCount = totalCount % pageSize == 0 ? (totalCount / pageSize) : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<ConversionData> subList;

            if (i == pageCount) {
                subList = conversionDataList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = conversionDataList.subList((i - 1) * pageSize, pageSize * (i));
            }

            HashMap<String, List<String>> hashMap = marketingCommonConfig.getXiechengSmsCustomerTransferApiCodes();
            // 同一批次数据，推送多个apiCode
            for (String pushApiCode : hashMap.get(xieChengSmsApiCode)) {
                TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
                robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList, null));
                robotOutboundDTO.setApiCode(pushApiCode);
                Result<TransferRobotOutboundVO<TransferRobotDataVO>> result =
                        methodRetryHandlerService.xieChengSmsCallCustomerTransfer(robotOutboundDTO, 0);

                if (result.getCode() == ResultCode.INTERNAL_SERVER_ERROR.getValue()) {
                    // unack
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
            }
        }
        log.warn("携程新场景短信撞库result=false分发多apicode推送至客服,mq消费并推送成功,耗时：{}，推送cell的数量为：{}", System.currentTimeMillis() - start, conversionDataList.size());
        // ack
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }

    private void modifyCorePoolSize() {
        Integer threadNum = marketingCommonConfig.getXieChengSmsMqPushCustomerThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
    }

    private void buildConversionDataList(Date nowDayEndTime, String cid, String sha256Code,
                                         String dataId, CountDownLatch countDownLatch, List<ConversionData> conversionDataList) {
        try {
            ConversionData conversionData = new ConversionData();
            conversionData.setDataId(dataId);
            conversionData.setCid(cid);
            SimpleDateFormat dateFormat = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
            String expireDate = dateFormat.format(nowDayEndTime);
            conversionData.setExpireDate(expireDate);
            conversionData.setInversionStatus("0");
            String query = RpcClientProxy.decode(sha256Code, "cell", "sha", "");
            conversionData.setPhone(query);
            conversionData.setInversionInfo("{}");
            conversionData.setPartnerProcessDate(DateUtils.format(new Date(), YYYY_MM_DD_HH_MM_SS));

            if (StringUtils.isEmpty(query)) {
                log.error("携程新场景短信撞库，sha256解密失败：{},dataId：{}", sha256Code, dataId);
            } else {
                conversionDataList.add(conversionData);
            }
        } catch (Exception e) {
            log.error("携程新场景短信撞库,Error occurred in buildConversionDataList: {}", e.getMessage(), e);
        } finally {
            countDownLatch.countDown();
        }
    }
}
