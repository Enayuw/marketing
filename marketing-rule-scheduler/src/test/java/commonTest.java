import com.br.marketing.rule.RuleSchedulerApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * commonTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {RuleSchedulerApplication.class})
@WebAppConfiguration
@Slf4j
public class commonTest implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RuleSchedulerApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }

    @Test
    public void test(){
        log.info("123456, 上山打老虎");
    }

}
