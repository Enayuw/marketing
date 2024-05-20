import com.br.marketing.monkey.MarketingDataMonkeyApplication;
import com.br.marketing.monkey.job.ZhongAnPushRosterLockingDataJob;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * JobTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MarketingDataMonkeyApplication.class})
@WebAppConfiguration
@Slf4j
public class JobTest implements ApplicationContextAware {

    @Autowired
    ZhongAnPushRosterLockingDataJob zhongAnPushRosterLockingDataJob;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MarketingDataMonkeyApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }
    @Test
    public void testActionTransferToFile(){
        JobExecutionMultipleShardingContext context = new JobExecutionMultipleShardingContext();
        context.setJobName("zhongAnPushRosterLockingDataJob");
        context.setJobParameter("7410906#2024-03-12");
        zhongAnPushRosterLockingDataJob.process(context);
    }

}
