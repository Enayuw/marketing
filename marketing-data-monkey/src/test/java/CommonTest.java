import com.br.marketing.monkey.MarketingDataMonkeyApplication;
import com.br.marketing.monkey.job.dewu.DewuCollidingDataToSendJob;
import com.br.marketing.monkey.job.tongcheng.TongChengOperationPushToCustomerJob;
import com.br.marketing.service.Impl.tongcheng.TongChengUndoListPushToCustomerService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * CommonTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MarketingDataMonkeyApplication.class})
@WebAppConfiguration
@Slf4j
public class CommonTest {

    @Resource
    private TongChengOperationPushToCustomerJob tongchengJob;

    @Resource
    private DewuCollidingDataToSendJob job;

    @Test
    public void testActionTransferToFile(){
        List<Long> localIdList = new ArrayList<>();
        localIdList.add(8610047L);
        localIdList.add(8610048L);
//        service.refreshLocalFile(localIdList);
    }

    @Test
    public void testDewuCollidingDataToSendJob(){
        JobExecutionMultipleShardingContext context = new JobExecutionMultipleShardingContext();
        context.setJobParameter("8820073");
        job.process(context);
    }

    @Test
    public void testTongchengJob(){
        JobExecutionMultipleShardingContext context = new JobExecutionMultipleShardingContext();
        tongchengJob.process(context);
    }

}
