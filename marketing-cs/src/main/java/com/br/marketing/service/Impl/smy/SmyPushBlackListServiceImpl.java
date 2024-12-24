package com.br.marketing.service.Impl.smy;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.smy.input.SmyCommReqDto;
import com.br.marketing.client.smy.input.SmyModelTagDto;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.smy.SmyBlacklistData;
import com.br.marketing.entity.smy.SmyBlacklistDataExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.smy.SmyBlacklistDataMapper;
import com.br.marketing.service.smy.ISmyPushBlackListService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import shaded.com.google.common.collect.Lists;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @Description 萨摩耶黑名单传输业务处理类
 * @Author bin.li1
 * @CreateTime 2024-12-23
 */
@Slf4j
@Service
public class SmyPushBlackListServiceImpl implements ISmyPushBlackListService {
    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private SmyBlacklistDataMapper smyBlacklistDataMapper;
    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Override
    public void pushBlackList(String apiCode, LocalDate localDate,int pushStatus) {
        //查询待推送文件
        LocalFileExample example = new LocalFileExample();
        Date startTime =  Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date endTime = Date.from(localDate.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        example.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.SMY_PUSH_BLACK_LIST.getValue()).andApiCodeEqualTo(apiCode)
                .andCompleteEqualTo("1").andStatusEqualTo("2").andPushStatusIsNull()
                //todo 取filename的日期吗?
                .andCreateTimeGreaterThanOrEqualTo(startTime).andCreateTimeLessThan(endTime);
        example.or().andFileTypeEqualTo(SftpFileTypeEnum.SMY_PUSH_BLACK_LIST.getValue()).andApiCodeEqualTo(apiCode)
                .andCompleteEqualTo("1").andStatusEqualTo("2").andPushStatusEqualTo("1")
                .andCreateTimeGreaterThanOrEqualTo(startTime).andCreateTimeLessThan(endTime);
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            log.warn("萨摩耶黑名单数据推送，没有需要处理的文件，apiCode:{}，localDate:{}"
                    , apiCode, localDate);
            return;
        }
        ThreadPoolExecutor poolExecutor = BrExecutors.getThreadPool(1, 1, "job-smy-blacklist");
        for (LocalFile localFile : localFiles) {
            log.warn("萨摩耶黑名单数据推送，开始处理{}文件，文件id:{}，apiCode:{}，localDate:{}", localFile.getFileName()
                    , localFile.getId(), apiCode, localDate);

            LocalFile localFileNew = new LocalFile();
            localFileNew.setId(localFile.getId());
            localFileNew.setPushStartTime(new Date());
            Long indexId = 0L;
            int autualNum = 0;
            List<Future<Integer>> futureList = new ArrayList<>();
            while (true) {
                SmyBlacklistDataExample smyExample =  new SmyBlacklistDataExample();
                smyExample.createCriteria().andLocalIdEqualTo(localFile.getId()).
                        andStatusEqualTo(1).andPushStatusEqualTo(pushStatus)
                        .andNameValueIsNotNull().andNameValueNotEqualTo("").andIdGreaterThan(indexId);
                smyExample.setOrderByClause("id asc limit 2000");
                List<SmyBlacklistData> list = smyBlacklistDataMapper.selectByExample(smyExample);
                if (CollectionUtils.isEmpty(list)) {
                    break;
                }
                autualNum += list.size();
                indexId = list.get(list.size() - 1).getId();
                List<List<SmyBlacklistData>> partition = Lists.partition(list, 500);
                updatePoolSize(poolExecutor);
                for(List<SmyBlacklistData> smyBlackList : partition){
                    futureList.add(
                            poolExecutor.submit(() -> {
                                    return sendSmyBlackList(smyBlackList);
                            })
                    );
                }
            }
            int errorActualNumber = 0;
            for (Future<Integer> future : futureList) {
                try {
                    errorActualNumber += future.get(1, TimeUnit.MINUTES);
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    log.error(e.getMessage(), e);
                }
            }
            localFileNew.setPushEndTime(new Date());
            if (errorActualNumber == 0) {
                localFileNew.setPushStatus("2");
                localFileNew.setPushNumber(autualNum-errorActualNumber);
                localFileNew.setActualNumber(autualNum);
            } else {
                localFileNew.setPushStatus("1");
                localFileNew.setErrorActualNumber(errorActualNumber);
                localFileNew.setPushNumber(autualNum-errorActualNumber);
                localFileNew.setActualNumber(autualNum);
            }
            // 更新文件状态
            localFileMapper.updateByPrimaryKeySelective(localFileNew);
        }
        try {
            poolExecutor.shutdown();
            while (!poolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("萨摩耶推送黑名单线程执行情况：TaskCount:{},ActiveCount:{}"
                        , poolExecutor.getTaskCount(), poolExecutor.getActiveCount());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 发送萨摩耶黑名单数据
     * @param list
     * @return errorNum
     */
    private int sendSmyBlackList(List<SmyBlacklistData> list ){
        int errorNum = 0;
        List<SmyModelTagDto.BatchHitValue> batchHitValueList = new ArrayList<>(500);
        List<Long> ids = new ArrayList<>(500);
        list.stream().forEach((SmyBlacklistData data) -> {
            batchHitValueList.add(new SmyModelTagDto.BatchHitValue(data.getNameValue(),"wp_black_record"));
            ids.add(data.getId());
        });
        //todo time
        String marketTime = generalMarketingTime();
        String reqSeqNumber = UUID.randomUUID().toString().replaceAll("-", "");
        SmyModelTagDto smyModelTagDto = new SmyModelTagDto(marketTime,batchHitValueList);
        SmyCommReqDto commReqDto = new SmyCommReqDto();
        commReqDto.setReqSeqNumber(reqSeqNumber);
        commReqDto.setBizContent(JSON.toJSONString(smyModelTagDto));
        Result result = methodRetryHandlerService.sendSmyBlackList(commReqDto,null);

        SmyBlacklistDataExample smyExample =  new SmyBlacklistDataExample();
        smyExample.createCriteria().andIdIn(ids);
        SmyBlacklistData smyBlacklistData = new SmyBlacklistData();
        switch (result.getCode()) {
            case 1:
                //推送成功
                smyBlacklistData.setPushStatus(2);
                //todo 成功更新营销时间
                smyBlacklistData.setMarketingTime(marketTime);
                log.warn("萨摩耶推送黑名单成功reqSeqNumber：{};本批次涉及的数据id：{}",reqSeqNumber,ids);
                if(smyBlacklistDataMapper.updateByExampleSelective(smyBlacklistData,smyExample) < 1){
                    log.warn("萨摩耶推送黑名单后更新数据状态失败reqSeqNumber：{};",reqSeqNumber);
                }
                break;
            case 500:
                // 已推送,未成功，业务返回失败
                smyBlacklistData.setPushStatus(3);
                log.warn("萨摩耶推送黑名单失败reqSeqNumber：{};本批次涉及的数据id：{}",reqSeqNumber,ids);
                if(smyBlacklistDataMapper.updateByExampleSelective(smyBlacklistData,smyExample) < 1){
                    log.warn("萨摩耶推送黑名单后更新数据状态失败reqSeqNumber：{};",reqSeqNumber);
                }
                errorNum = list.size();
                break;
            default:
                // 未推送,发送前签名失败 或 返回业务报错，验签失败等 todo
                errorNum = list.size();

        }
        return errorNum;
    }
    private String generalMarketingTime(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * 2024-10-30 12:40
     * 设置线程数据
     */
    private void updatePoolSize(ThreadPoolExecutor poolExecutor) {
           int poolCoreSize = Integer.parseInt(marketingCommonConfig.getSmyBlacklistConfig().getOrDefault("poolCoreSize",1).toString());
            if (poolExecutor.getCorePoolSize() != poolCoreSize) {
                poolExecutor.setCorePoolSize(poolCoreSize);
            }
            int poolMaxSize = Integer.parseInt(marketingCommonConfig.getSmyBlacklistConfig().getOrDefault("poolMaxSize",1).toString());
            if (poolExecutor.getMaximumPoolSize() != poolMaxSize) {
                poolExecutor.setMaximumPoolSize(poolMaxSize);
            }
    }
}
