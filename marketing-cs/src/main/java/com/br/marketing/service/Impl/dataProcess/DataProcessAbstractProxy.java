package com.br.marketing.service.Impl.dataProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.PullCustomerFileDataExample;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PullCustomerFileDataMapper;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description 文件数据处理通用抽象类
 * @Author hong.chen
 * @CreateTime 2023/11/13
 */
@Slf4j
public abstract class DataProcessAbstractProxy {
    @Autowired
    MarketingApiService marketingApiService;
    @Resource
    private PullCustomerFileDataMapper customerFileDataMapper;

    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private TrackingService trackingService;

    public final void doProcess(DataProcessingConfig config) {
        if (!canStart(config)) {
            log.warn("数据处理任务cannot start，apiCode:{}，fileName:{}", config.getApiCode(), config.getLocalFile().getFileName());
            return;
        }

        // 任务开始：b_local_file表push_status置为1
        Long localFileId = config.getLocalFile().getId();
        LocalFile localFile = localFileMapper.selectByPrimaryKey(localFileId);
        localFile.setPushStatus("1");
        localFileMapper.updateByPrimaryKeySelective(localFile);

        AtomicInteger errorMark = new AtomicInteger(0);
        // 数据处理
        dataProcessLoop(config, localFileId,errorMark);

        // 任务结束：push_status置为2,有失败的，置为3
        if (errorMark.get() > 0) {
            log.error("清洗通用流程调用上传或转化接口失败，失败量级={}",errorMark.get());
            localFile.setPushStatus("3");
        } else {
            localFile.setPushStatus("2");
        }
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    /**
     * 多线程数据处理
     * @param config
     * @param localFileId
     */
    private void dataProcessLoop(DataProcessingConfig config, Long localFileId,AtomicInteger errorMark) {
        // 获取线程数配置
        Integer threadNum = getThreadNum(config);
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(threadNum, threadNum);

        Long id = null;
        PullCustomerFileDataExample pullCustomerFileDataExample = new PullCustomerFileDataExample();
        // 查询b_pull_customer_file_data,条件：local_id且data_status=1
        buildExample(localFileId, id, pullCustomerFileDataExample);
        AtomicLong total = new AtomicLong(0L);
        while (true) {
            List<PullCustomerFileData> customerFileDataList = customerFileDataMapper.selectPageListByExampletikv_(pullCustomerFileDataExample);
            if (customerFileDataList.isEmpty()) {
                break;
            }

            id = customerFileDataList.get(customerFileDataList.size() - 1).getId();

            try {
                total.addAndGet(customerFileDataList.size());
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                                , ex.getMessage()
                                , "埋点异常")
                        , ex);
            }

            pullCustomerFileDataExample.clear();
            buildExample(localFileId, id, pullCustomerFileDataExample);

            pool.submit(() -> result(customerFileDataList, config,errorMark));
        }

        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            String remark = String.format("文件数据处理通用流程（客户数据清洗等）,配置id：%s"
                    , config.getId());
            trackingService.trackPointLog(DataFlowDirection.OUT
                    , config.getApiCode()
                    , "文件数据处理通用流程-"+config.getProxyName()
                    , total.get()
                    , remark
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }
    }

    /**
     * 通用模板方法
     * @param customerFileDataList
     * @param config
     */
    private void result(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config,AtomicInteger errorMark) {
        try {
            Object assembleData = assembleData(customerFileDataList, config);
            Object result = call(assembleData, config,errorMark);
            assembleResult(result);
        } catch (Exception e) {
            log.error("数据处理任务异常,配置表id:{},apiCode:{}", config.getId(), config.getApiCode(), e.getMessage(), e);
        }
    }

    /**
     * 判断是否可以开始（子类可重写）
     * @param config
     * @return 默认返回true：可以开始
     */
    boolean canStart(DataProcessingConfig config) {
        return true;
    }

    /**
     * 封装调用前的请求数据（子类必须实现）
     * @param customerFileDataList
     * @param config
     * @return
     */
    abstract Object assembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config);


    /**
     * 调用接口或方法（子类必须实现）
     * @param data
     * @param config
     * @return
     */
    abstract Object call(Object data, DataProcessingConfig config ,AtomicInteger errorMark);

    /**
     * 结果处理（子类可重写）
     * @param data
     * @return
     */
    Object assembleResult(Object data) {
        return data;
    }

    /**
     * 获取配置表中的线程数。若没配置，使用默认值20
     * @param config
     * @return
     */
    private Integer getThreadNum(DataProcessingConfig config) {
        Integer threadNum = 20;
        String extendField = config.getExtendField();
        if (StringUtils.isEmpty(extendField)) {
            return threadNum;
        }

        JSONObject configJson = JSON.parseObject(extendField);
        Integer threadNumConfig = configJson.getInteger("threadNum");
        if (StringUtils.isNotEmpty(threadNumConfig)) {
            threadNum = threadNumConfig;
        }

        return threadNum;
    }

    /**
     * 构建分页查询的参数
     * @param localFileId
     * @param id
     * @param pullCustomerFileDataExample
     */
    private void buildExample(Long localFileId, Long id, PullCustomerFileDataExample pullCustomerFileDataExample) {
        PullCustomerFileDataExample.Criteria criteria =
                pullCustomerFileDataExample.createCriteria().andDataStatusEqualTo(1).andLocalFileIdEqualTo(localFileId);
        if (id != null) {
            criteria.andIdGreaterThan(id);
        }
        pullCustomerFileDataExample.setOrderByClause("id asc");
    }
}
