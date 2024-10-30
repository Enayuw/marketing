package com.br.marketing.service.Impl.guomei;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.GuoMeiTotalNumBO;
import com.br.marketing.client.guomei.base.AbstractUserListBase;
import com.br.marketing.client.guomei.userdata.GmUserDataCallBack;
import com.br.marketing.client.guomei.userdata.GmUserDataCallBackRequest;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.guomei.GuoMeiCallbackData;
import com.br.marketing.entity.guomei.GuoMeiCallbackDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.guomei.GuoMeiCallbackDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 国美回调
 *
 * @author Hua Qiang
 * @date 2024-10-29 17:58
 */
@Service
@Slf4j
public class GuoMeiDataCallbackServiceImpl implements IGuoMeiDataCallbackService {

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private GuoMeiCallbackDataMapper guoMeiCallbackDataMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void pushDataCallback(String apiCode, LocalDate localDate) {
        LocalFileExample example = new LocalFileExample();
        //查询已入库文件
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.GUO_MEI_DATA_CALLBACK.getValue())
                .andStatusEqualTo("2").andCompleteEqualTo("1").andApiCodeEqualTo(apiCode)
                .andPushEndTimeIsNull().andCreateTimeGreaterThanOrEqualTo(Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant()))
                .andCreateTimeLessThan(Date.from(localDate.plusDays(1).atStartOfDay()
                        .atZone(ZoneId.systemDefault()).toInstant()));
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (localFiles.size() == 0) {
            log.warn("国美用户数据回传，没有需要处理的文件，apiCode:{}，localDate:{}"
                    , apiCode, localDate);
            return;
        }
        ThreadPoolExecutor poolExecutor = BrExecutors.getThreadPool(Runtime.getRuntime().availableProcessors()
                , Runtime.getRuntime().availableProcessors() * 2, new SynchronousQueue<>()
                , "guoMei-push-data-callback");
        int limit = 1000;
        for (LocalFile localFile : localFiles) {
            LocalFile localFileNew = new LocalFile();
            localFileNew.setId(localFile.getId());
            localFileNew.setPushStartTime(new Date());
            List<GuoMeiTotalNumBO> guoMeiTotalNumBOList = guoMeiCallbackDataMapper.getBatchPlanIdUserTypeByList(apiCode
                    , localFile.getId());
            int pushNumber = 0;
            for (GuoMeiTotalNumBO totalNumBO : guoMeiTotalNumBOList) {
                Long maxId = null;
                long totalNum = totalNumBO.getTotalNum();
                Integer batch = totalNumBO.getBatch();
                Long planId = totalNumBO.getPlanId();
                Integer userType = totalNumBO.getUserType();
                GuoMeiCallbackDataExample dataExample = new GuoMeiCallbackDataExample();
                dataExample.createCriteria().andApiCodeEqualTo(apiCode).andBatchEqualTo(batch)
                        .andPlanIdEqualTo(planId).andUserTypeEqualTo(userType).andLocalIdEqualTo(localFile.getId())
                        .andIsDeletedEqualTo(0).andStatusEqualTo(1).andPushStatusEqualTo(0);
                while (!Thread.interrupted()) {
                    updatePoolSize(poolExecutor);
                    List<GuoMeiCallbackData> callbackDataList = guoMeiCallbackDataMapper.selectByMaxIdAndExample(
                            dataExample, maxId, limit);
                    int size = callbackDataList.size();
                    if (size == 0) {
                        break;
                    }
                    pushNumber += size;
                    GuoMeiCallbackData data = callbackDataList.get(size - 1);
                    maxId = data.getId();
                    poolExecutor.execute(() -> {
                        try {
                            GmUserDataCallBackRequest request = splicingDataCallBackData(callbackDataList, apiCode, batch, planId, userType, totalNum);
                            methodRetryHandlerService.sendUserDataCallBack(request, null);
                            updateCallbackData(callbackDataList, localFile);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    });
                    if (size < limit) {
                        break;
                    }
                }
            }
            localFileNew.setPushEndTime(new Date());
            localFileNew.setPushStatus("2");
            localFileNew.setPushNumber(pushNumber);
            // 更新文件状态
            localFileMapper.updateByPrimaryKeySelective(localFileNew);
        }
        try {
            poolExecutor.shutdown();
            while (!poolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("国美用户数据回调线程执行情况：TaskCount:{},ActiveCount:{}"
                        , poolExecutor.getTaskCount(), poolExecutor.getActiveCount());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 2024-10-30 12:40
     * 设置线程数据
     */
    private void updatePoolSize(ThreadPoolExecutor poolExecutor) {
        Object poolSize = marketingCommonConfig.getGuoMeiDataCallbackConfig().get("poolSize");
        if (poolSize != null) {
            int size = Integer.parseInt(poolSize.toString());
            if (poolExecutor.getCorePoolSize() != size) {
                poolExecutor.setCorePoolSize(size);
            }
            if (poolExecutor.getMaximumPoolSize() != size) {
                poolExecutor.setMaximumPoolSize(size);
            }
        }
    }


    /**
     * 2024-10-30 0:19
     * 更新数据状态，已推送
     */
    private void updateCallbackData(List<GuoMeiCallbackData> callbackDataList, LocalFile localFile) {
        GuoMeiCallbackData updateCallbackData = new GuoMeiCallbackData();
        updateCallbackData.setPushStatus(2);
        List<Long> ids = callbackDataList.stream().map(GuoMeiCallbackData::getId).collect(Collectors.toList());
        GuoMeiCallbackDataExample updateExample = new GuoMeiCallbackDataExample();
        updateExample.createCriteria().andIdIn(ids).andLocalIdEqualTo(localFile.getId());
        int i = guoMeiCallbackDataMapper.updateByExampleSelective(updateCallbackData, updateExample);
        if (i > 0) {
            log.warn("国美用户数据回调数据更新失败,localFile[name:{},id:{}]，ids:{}"
                    , localFile.getFileName(), localFile.getId(), ids.toArray());
        }
    }

    /**
     * 2024-10-30 0:26
     * 拼装数据
     */
    private GmUserDataCallBackRequest splicingDataCallBackData(List<GuoMeiCallbackData> callbackDataList, String apiCode, Integer batch
            , Long planId, Integer userType, Long totalNum) {
        Set<String> userIdSet = callbackDataList.stream().map(GuoMeiCallbackData::getUserId).collect(Collectors.toSet());
        List<MarketingSyncUser> userList = marketingSyncUserMapper.getSyncUserLastByCustNumsAndCusBatch(
                apiCode, userIdSet, batch, planId);
        List<AbstractUserListBase> callBackList = new ArrayList<>();
        for (MarketingSyncUser syncUser : userList) {
            GmUserDataCallBack callBack = new GmUserDataCallBack();
            callBack.setUserId(syncUser.getCustNum());
            try {
                JSONObject jsonObject = JSONObject.parseObject(syncUser.getReserveField1());
                callBack.setProperties(jsonObject.getJSONObject("properties"));
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
            callBackList.add(callBack);
        }
        GmUserDataCallBackRequest request = new GmUserDataCallBackRequest();
        request.setBatch(batch);
        request.setPlanId(planId);
        request.setUserType(userType);
        request.setTotalNum(totalNum);
        request.setUserList(callBackList);
        return request;
    }
}
