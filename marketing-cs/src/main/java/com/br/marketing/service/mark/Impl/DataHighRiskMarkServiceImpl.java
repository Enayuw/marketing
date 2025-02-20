package com.br.marketing.service.mark.Impl;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.mark.FlagDataCarryLogCell;
import com.br.marketing.entity.FlagData;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.StraHisFileExample;
import com.br.marketing.enums.DataMarkEnum;
import com.br.marketing.mapper.FlagDataMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.mark.DataHighRiskMarkService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @description 高风险打标实现
 * @author hedongshuo
 * @date 2025/2/19 21:30
 **/
@Service
@Slf4j
public class DataHighRiskMarkServiceImpl implements DataHighRiskMarkService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Resource
    RedisChgService redisChgService;

    @Resource
    FlagDataMapper flagDataMapper;

    @Override
    public void process() {
        marketingCommonConfig.getDataMarkApiCodes().forEach((String apiCode) -> {
            //1.查询跑分任务表
            StraHisFile straHisFile = getStraHisFile(apiCode);
            if (null == straHisFile) {
                return;
            }
            //2.创建线程池
            Integer threadPoolSize = marketingCommonConfig.getDataMarkThreadNum();
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadPoolSize, threadPoolSize);
            //3.打标主流程
            markProcess(apiCode, straHisFile, threadPool);
        });




    }

    /**
     * @description 打标主流程
     * @param apiCode
     * @param straHisFile
     * @param threadPool
     */
    private void markProcess(String apiCode, StraHisFile straHisFile, ThreadPoolExecutor threadPool) {
        String key = RedisKeyConstant.prefix.concat(DataMarkEnum.MARK_HIGHRISK.getMarkRedisKey()).concat(":").concat(apiCode);
        for (; ; ) {
            String lockValue = UUID.randomUUID().toString();
            try{
                //1.抢锁
                redisChgService.lock(key, lockValue);
                //2.查数据
                List<FlagDataCarryLogCell> flagDataList = getFlagData(apiCode, straHisFile);
                //3.查询es

            } catch (Exception e) {

            }

        }
    }

    /**
     * @description 查询当天未打标的数据
     * @param apiCode
     * @param straHisFile
     * @return java.util.List<com.br.marketing.entity.FlagData>
     * @author hedongshuo
     * @date 2025/2/20 17:36
     **/
    private List<FlagDataCarryLogCell> getFlagData(String apiCode, StraHisFile straHisFile) {
        Integer dataMarkPageSize = marketingCommonConfig.getDataMarkPageSize();
        return flagDataMapper.queryLogCellByDatebI_(
                apiCode,
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                dataMarkPageSize);
    }

    /**
     * @description 获取当天最新的跑分文件
     * @param apiCode
     * @return void
     * @author hedongshuo
     * @date 2025/2/20 16:18
     **/
    private StraHisFile getStraHisFile(String apiCode) {
        Date beginTimeOfDay = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        StraHisFileExample straHisFileExample = new StraHisFileExample();
        straHisFileExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andStatusEqualTo(2)
                .andTypeEqualTo(2)
                .andCreateTimeGreaterThanOrEqualTo(beginTimeOfDay);
        straHisFileExample.setOrderByClause("create_time desc limit 1");
        List<StraHisFile> straHisFiles = straHisFileMapper.selectByExample(straHisFileExample);
        if (CollectionUtils.isEmpty(straHisFiles)) {
            return null;
        }
        return straHisFiles.get(0);
    }
}
