package com.br.marketing.service.tccpa.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrCpaFailData;
import com.br.marketing.entity.MarketingTcyrCpaFailFile;
import com.br.marketing.entity.MarketingTcyrCpaFailFileExample;
import com.br.marketing.entity.TcyrCpaCollectTask;
import com.br.marketing.enums.*;
import com.br.marketing.mapper.MarketingTcyrCpaFailDataMapper;
import com.br.marketing.mapper.MarketingTcyrCpaFailFileMapper;
import com.br.marketing.mapper.TcyrCpaCollectTaskMapper;
import com.br.marketing.service.tccpa.TcCpaCollidingFailDealService;
import com.br.marketing.service.tccpa.TcCpaCustCellMappingService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class TcCpaCollidingFailDealServiceImpl implements TcCpaCollidingFailDealService {

    private final static String TITLE = "【同程易融CPA-撞库失败数据处理任务】";

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingTcyrCpaFailFileMapper tcyrCpaFailFileMapper;

    @Resource
    private MarketingTcyrCpaFailDataMapper tcyrCpaFailDataMapper;

    @Resource
    private TcyrCpaCollectTaskMapper tcyrCpaCollectTaskMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaCustCellMappingService custCellMappingService;

    private static final Set<Integer> excludedLengths = Set.of(19, 10, 8);

    @Override
    public void process(String apiCode) {
        String lockKey = RedisKeyConstant.tcyrCpaCollidingFailDeal.concat(apiCode);
        String lockValue = UUID.randomUUID().toString();
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_COLLIDING_FAIL_DEAL.getName(), 50, 50);
        try{
            for (;;) {
                //1.获取redis锁
                redisChgService.lockLoop(lockKey, lockValue, 5000l, null);
                //2.查询单条未处理的csvFile记录
                MarketingTcyrCpaFailFileExample failFileExample = new MarketingTcyrCpaFailFileExample();
                failFileExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andCollidingDataDealStatusEqualTo(TcCpaCollidingDealStatusEnum.DEAL_NO.getValue())
                        .andIsDelEqualTo(TcCpaIsDelEnum.DEL_NO.getValue());
                failFileExample.setOrderByClause("create_time asc limit 1");
                List<MarketingTcyrCpaFailFile> tcyrCpaFailFiles = tcyrCpaFailFileMapper.selectByExample(failFileExample);
                if (CollectionUtils.isEmpty(tcyrCpaFailFiles)) {
                    redisChgService.unlock(lockKey, lockValue);
                    break;
                }
                MarketingTcyrCpaFailFile tcyrCpaFailFile = tcyrCpaFailFiles.get(0);
                //3.修改csvFile记录状态为1-处理中
                MarketingTcyrCpaFailFile updateFile = new MarketingTcyrCpaFailFile();
                updateFile.setId(tcyrCpaFailFile.getId());
                updateFile.setCollidingDataDealStatus(TcCpaCollidingDealStatusEnum.DEAL_MIDDLE.getValue());
                tcyrCpaFailFileMapper.updateByPrimaryKeySelective(updateFile);
                //4.释放redis锁
                redisChgService.unlock(lockKey, lockValue);
                //5.处理csvFile
                csvFileDbDeal(tcyrCpaFailFile, updateFile, actionPool);
            }
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }finally {
            redisChgService.unlock(lockKey, lockValue);
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void csvFileDbDeal(MarketingTcyrCpaFailFile tcyrCpaFailFile, MarketingTcyrCpaFailFile updateFile, TpDynamicExecutor actionPool) {
        //1.判断文件存在
        File txtFile = new File(tcyrCpaFailFile.getFilePath());
        if (!txtFile.exists()) {
            updateFile.setCollidingDataDealStatus(TcCpaCollidingDealStatusEnum.NO_FILE.getValue());
            tcyrCpaFailFileMapper.updateByPrimaryKeySelective(updateFile);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    "文件不存在，文件id：" + tcyrCpaFailFile.getId(), TITLE));
        }
        //2.处理csvFile
        long totalCount = 0L;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            List<String> lines = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() == marketingCommonConfig.getTcyrCpaCollidingFailDealConfig().getInteger("lineReadSize")) {
                    if (marketingCommonConfig.getTcyrCpaCollidingFailDealConfig().getBoolean("jobStopSwitch")) {
                        lines.clear();
                        break;
                    }
                    List<String> threadLines = new ArrayList<>(lines);
                    futures.add(CompletableFuture.runAsync(() -> process(tcyrCpaFailFile, threadLines), actionPool));
                    lines.clear();
                }
                totalCount++;
            }
            if (!lines.isEmpty()) {
                futures.add(CompletableFuture.runAsync(() -> process(tcyrCpaFailFile, lines), actionPool));
            }
            //3.更新csvFile记录
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            updateFile.setTotalCount(totalCount);
            updateFile.setCollidingDataDealStatus(TcCpaCollidingDealStatusEnum.DEAL_SUCCESS.getValue());
            TcyrCpaCollectTask tcyrCpaCollectTask = TcyrCpaCollectTask.builder().batchNo(tcyrCpaFailFile.getBatchNo())
                    .status(TcCpaSyncDealStatusEnum.DEAL_NO.getValue()).sourceId(tcyrCpaFailFile.getId()).isDel(1)
                    .extend(tcyrCpaFailFile.getExtend()).sourceType(TcCpaCollidingSourceTypeEnum.FAIL.getValue())
                    .createTime(new Date()).updateTime(new Date())
                    .apiCode(marketingCommonConfig.getTcyrCpaApiCode()).build();
            tcyrCpaCollectTaskMapper.insert(tcyrCpaCollectTask);
            tcyrCpaFailFileMapper.updateByPrimaryKeySelective(updateFile);
        } catch (Exception e) {
            //4.修改quick_deal_status 异常状态
            updateFile.setCollidingDataDealStatus(TcCpaCollidingDealStatusEnum.DEAL_FAIL.getValue());
            tcyrCpaFailFileMapper.updateByPrimaryKeySelective(updateFile);
            log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }

    private void process(MarketingTcyrCpaFailFile tcyrCpaFailFile, List<String> lines) {
        List<List<String>> partitions =
                ListUtils.partition(lines, marketingCommonConfig.getTcyrCpaCollidingFailDealConfig().getInteger("dbPartSize"));
        for (List<String> partition : partitions) {
            try {
                List<MarketingTcyrCpaFailData> failDataList = new ArrayList<>();
                for (String line : partition) {
                    MarketingTcyrCpaFailData failData = new MarketingTcyrCpaFailData();
                    failData.setApiCode(tcyrCpaFailFile.getApiCode());
                    failData.setBatchNo(tcyrCpaFailFile.getBatchNo());
                    failData.setSyncFileId(tcyrCpaFailFile.getId());
                    failData.setOriginText(line);
                    List<String> lineData = StringUtils.splitAndLimit(line, "," , 3);
                    failData.setUserKey(lineData.get(0));
                    failData.setCell(custCellMappingService.selectCell(failData.getUserKey()));
                    failData.setFailMsg(lineData.get(1));
                    failData.setReleaseTime(DateHelper.stringToDate(lineData.get(2)));
                    failData.setCreateTime(new Date());
                    if (StringUtils.isEmpty(failData.getUserKey())
                            || StringUtils.isEmpty(failData.getCell())
                            || StringUtils.isEmpty(failData.getFailMsg())
                            || failData.getReleaseTime() == null
                            || !excludedLengths.contains(lineData.get(2).trim().length())) {
                        failData.setStatus(TcFileDataDealStatusEnum.STATUS_FAIL.getValue());
                        failData.setStatusMsg("数据异常");
                        failData.setExtend(line);
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                                "数据异常，fileId:" + tcyrCpaFailFile.getId() + "，line:" + line, TITLE));
                    } else {
                        if (lineData.size() > 3) {
                            failData.setStatusMsg("客户新增字段传输");
                            failData.setExtend(line);
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                                    "客户新增字段传输，fileId:" + tcyrCpaFailFile.getId() + "，line:" + line, TITLE));
                        }
                        failData.setStatus(TcFileDataDealStatusEnum.STATUS_SUCCESS.getValue());
                    }
                    failData.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
                    failDataList.add(failData);
                }
                tcyrCpaFailDataMapper.batchSave(failDataList);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                        "数据插入异常，fileId:" + tcyrCpaFailFile.getId() + "，lines:" + lines, TITLE), e);
            }
        }
    }

}
