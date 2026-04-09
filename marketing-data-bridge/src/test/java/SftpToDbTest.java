import com.br.marketing.bridge.DataBridgeApplication;
import com.br.marketing.bridge.job.SftpToDbByCommonJob;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.config.biz.TcyrCpaConfigManager;
import com.br.marketing.entity.MarketingTcyrCpaFailData;
import com.br.marketing.entity.TcyrCpaInvalueData;
import com.br.marketing.entity.TcyrCpaLockData;
import com.br.marketing.mapper.MarketingTcyrCpaFailDataMapper;
import com.br.marketing.mapper.TcyrCpaInvalueDataMapper;
import com.br.marketing.mapper.TcyrCpaLockDataMapper;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SftpToDbTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DataBridgeApplication.class})
@WebAppConfiguration
@Slf4j
public class SftpToDbTest implements ApplicationContextAware {

    @Autowired
    SftpToDbByCommonJob sftpToDbByCommonJob;

    @Resource
    private TcyrCpaLockDataMapper tcyrCpaLockDataMapper;

    @Resource
    private TcyrCpaInvalueDataMapper tcyrCpaInvalueDataMapper;

    @Resource
    private MarketingTcyrCpaFailDataMapper marketingTcyrCpaFailDataMapper;

    @Resource
    TcyrCpaConfigManager tcyrCpaConfigManager;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DataBridgeApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }
    @Test
    public void testActionTransferToFile(){
        JobExecutionMultipleShardingContext context = new JobExecutionMultipleShardingContext();
        context.setJobName("SftpToDbByCommonJob");
        sftpToDbByCommonJob.process(context);
    }

    @Test
    public void test() {
        List<MarketingTcyrCpaFailData> batchData = marketingTcyrCpaFailDataMapper
                .selectBySyncFileIdAndIdRange(20260109001l, 34462355l, 34462362l, 10);
        Map<String, Integer> failMsgToLbMap = tcyrCpaConfigManager.getFailMsgToBlMapVT();
        processBatchData(batchData, 123456l, failMsgToLbMap);
    }

    private void processBatchData(List<MarketingTcyrCpaFailData> batchData,
                                  Long taskId,
                                  Map<String, Integer> failMsgToLbMap) {
        //进【b_tcyr_cpa_lock_data】的数据
        List<TcyrCpaLockData> lockData = new ArrayList<>();
        //进【b_tcyr_cpa_invalue_data】的数据
        List<TcyrCpaInvalueData> invalueData = new ArrayList<>();
        for (MarketingTcyrCpaFailData datum : batchData) {
            if (failMsgToLbMap.containsKey(datum.getFailMsg())) {
                lockData.add(getTcyrCpaLockData(datum, taskId, failMsgToLbMap.get(datum.getFailMsg())));
            } else {
                invalueData.add(getTcyrCpaInvalueData(datum, taskId));
            }
        }
        if (CollectionUtils.isNotEmpty(lockData)) {
            tcyrCpaLockDataMapper.batchSave(lockData);
        }
        if (CollectionUtils.isNotEmpty(invalueData)) {
            tcyrCpaInvalueDataMapper.batchSave(invalueData);
        }
    }

    private TcyrCpaLockData getTcyrCpaLockData(MarketingTcyrCpaFailData failData,
                                               Long taskId,
                                               Integer lockBelong) {
        TcyrCpaLockData lockData = new TcyrCpaLockData();
        BeanUtils.copyProperties(failData, lockData);
        lockData.setReleaseTime(failData.getReleaseTime());
        lockData.setTaskId(taskId);
        lockData.setLockBelong(lockBelong);
        lockData.setIsDel(Constants.DATA_VALID);
        lockData.setExtend(failData.getExtend());
        lockData.setCreateTime(new Date());
        lockData.setUpdateTime(new Date());
        return lockData;
    }

    private TcyrCpaInvalueData getTcyrCpaInvalueData(MarketingTcyrCpaFailData failData,
                                                     Long taskId) {
        TcyrCpaInvalueData invalue = new TcyrCpaInvalueData();
        BeanUtils.copyProperties(failData, invalue);
        invalue.setReleaseTime(failData.getReleaseTime());
        invalue.setFailMsg(failData.getFailMsg());
        invalue.setTaskId(taskId);
        invalue.setExtend(failData.getExtend());
        invalue.setCreateTime(new Date());
        invalue.setUpdateTime(new Date());
        return invalue;
    }

}
