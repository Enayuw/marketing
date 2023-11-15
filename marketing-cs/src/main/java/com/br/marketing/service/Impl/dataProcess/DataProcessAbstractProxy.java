package com.br.marketing.service.Impl.dataProcess;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.PullCustomerFileDataExample;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.PullCustomerFileDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;

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
    @Resource
    PullCustomerFileDataMapper customerFileDataMapper;

    @Resource
    LocalFileMapper localFileMapper;
    @Autowired
    RestTemplate restTemplate;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;


    // 开启线程池，遍历b_pull_customer_file_data，查询要处理的数据，单个线程2000条，b_local_file表push_status记为1（任务开始）（子类可重写）
    // 组装参数：将客户字段映射到标准接口字段（子类必须实现）
    // 调用上传接口（子类可重写）
    // 可以拓展结果处理（子类可重写）
    // b_local_file表push_status记为2（任务结束）
    public final void doProcess(DataProcessingConfig config) {
        String apiCode = config.getApiCode();
        Long localFileId = config.getLocalFileId();
        String url = config.getUrl();
        ThreadPoolExecutor pool = BrExecutors.getThreadPool(2, 2);
        // b_local_file表push_status记为1（任务开始）
        LocalFile localFile = localFileMapper.selectByPrimaryKey(localFileId);
        localFile.setPushStatus("1");
        localFileMapper.updateByPrimaryKeySelective(localFile);

        String fileName = localFile.getFileName();
        Long id = null;
        PullCustomerFileDataExample pullCustomerFileDataExample = new PullCustomerFileDataExample();
        buildExample(localFileId, id, pullCustomerFileDataExample);
        while (true) {
            // 调整线程数 todo
            // 遍历b_pull_customer_file_data
            List<PullCustomerFileData> customerFileDataList = customerFileDataMapper.selectPageListByExampletikv_(pullCustomerFileDataExample);
            if (customerFileDataList.isEmpty()) {
                break;
            }

            id = customerFileDataList.get(customerFileDataList.size() - 1).getId();
            pullCustomerFileDataExample.clear();
            buildExample(localFileId, id, pullCustomerFileDataExample);

            pool.submit(() -> result(apiCode, customerFileDataList,config,fileName));
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

    abstract Object assembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config, String fileName);

    private void result(String apiCode, List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config, String fileName) {
        Object assembleData = assembleData(customerFileDataList,config,fileName);
        Object result = call(apiCode, assembleData, config.getUrl());
        assembleResult(result);
    }

    /**
     * 调用接口或方法
     * @param apiCode
     * @param data
     * @param url
     * @return
     */
    abstract Object call(String apiCode, Object data, String url);

    public Object assembleResult(Object data) {
        return data;
    }
}
