import com.br.marketing.check.CkeckApplication;
import com.br.marketing.check.job.SftpToDbByCommonJob;
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
 * SftpToDbTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
@Slf4j
public class SftpToDbTest implements ApplicationContextAware {

    @Autowired
    SftpToDbByCommonJob sftpToDbByCommonJob;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        CkeckApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }
    @Test
    public void testActionTransferToFile(){
        JobExecutionMultipleShardingContext context = new JobExecutionMultipleShardingContext();
        context.setJobName("SftpToDbByCommonJob");
        sftpToDbByCommonJob.process(context);
    }

}
