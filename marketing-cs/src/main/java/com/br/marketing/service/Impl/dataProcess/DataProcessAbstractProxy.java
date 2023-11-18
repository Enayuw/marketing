package com.br.marketing.service.Impl.dataProcess;

import com.br.marketing.client.marketingapi.MarketingApiService;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.PullCustomerFileDataExample;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PullCustomerFileDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description DataProcessAbstractProxy
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
    MarketingCommonConfig marketingCommonConfig;

//    @Autowired
//    private RestTemplate restTemplate;

//    @Qualifier("interfaceLogDbpool")
//    @Autowired
//    private ThreadPoolExecutor interfaceLogDbpool;

//    @Autowired
//    private InterfaceLogMapper interfaceLogMapper;

//    public InterfaceLogMapper getInterfaceLogMapper() {
//        return interfaceLogMapper;
//    }

    // 开启线程池，遍历b_pull_customer_file_data，查询要处理的数据，单个线程2000条，b_local_file表push_status记为1（任务开始）（子类可重写）
    // 组装参数：将客户字段映射到标准接口字段（子类必须实现）
    // 调用上传接口（子类可重写）
    // 可以拓展结果处理（子类可重写）
    // b_local_file表push_status记为2（任务结束）
    public final void doProcess(DataProcessingConfig config) {
        if (!canStart(config)) {
            return;
        }

        Long localFileId = config.getLocalFile().getId();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(marketingCommonConfig.getDataProcessAnTaskThreadNum(), marketingCommonConfig.getDataProcessAnTaskThreadNum());
        // b_local_file表push_status记为1（任务开始）
        LocalFile localFile = localFileMapper.selectByPrimaryKey(localFileId);
        localFile.setPushStatus("1");
        localFileMapper.updateByPrimaryKeySelective(localFile);

        Long id = null;
        PullCustomerFileDataExample pullCustomerFileDataExample = new PullCustomerFileDataExample();
        buildExample(localFileId, id, pullCustomerFileDataExample);
        while (true) {
            // 调整线程数
            modifyCorePoolSize(pool);
            // 查询b_pull_customer_file_data,条件：local_id且data_status=0
            List<PullCustomerFileData> customerFileDataList = customerFileDataMapper.selectPageListByExampletikv_(pullCustomerFileDataExample);
            if (customerFileDataList.isEmpty()) {
                break;
            }

            id = customerFileDataList.get(customerFileDataList.size() - 1).getId();
            pullCustomerFileDataExample.clear();
            buildExample(localFileId, id, pullCustomerFileDataExample);

            pool.submit(() -> result(customerFileDataList,config));
        }

        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                // todo
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        // push_status置为任务结束
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

    private void buildExample(Long localFileId, Long id, PullCustomerFileDataExample pullCustomerFileDataExample) {
        PullCustomerFileDataExample.Criteria criteria =
                pullCustomerFileDataExample.createCriteria().andDataStatusEqualTo(0).andLocalFileIdEqualTo(localFileId);
        if (id != null) {
            criteria.andIdGreaterThan(id);
        }
        pullCustomerFileDataExample.setOrderByClause("id asc");
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum = marketingCommonConfig.getDataProcessAnTaskThreadNum();
        pool.setCorePoolSize(threadNum);
        pool.setMaximumPoolSize(threadNum);
    }

    abstract Object assembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config);

    private void result(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        Object assembleData = assembleData(customerFileDataList,config);
        Object result = call(assembleData, config);
        assembleResult(result);
    }

    abstract Object call(Object data, DataProcessingConfig config);

    public Object assembleResult(Object data) {
        return data;
    }

    public Boolean canStart(DataProcessingConfig config){
        return true;
    }
}
