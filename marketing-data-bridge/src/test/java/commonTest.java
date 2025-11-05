import com.br.marketing.bridge.DataBridgeApplication;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.mapper.SyncConfigMapper;
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

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * commonTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {DataBridgeApplication.class})
@WebAppConfiguration
@Slf4j
public class commonTest implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        DataBridgeApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }

    @Test
    public void test(){
        log.info("123456, 上山打老虎");
    }

    @Test
    public void test0011() {
        ArrayList<String> list = new ArrayList<>();
        list.add("2026-01-27 23:59:59");
        list.add("2099-12-31");
        list.add("25-11-05");
        for (String s : list) {
            Date date = DateHelper.stringToDate(s);
            System.out.println(date);
        }
    }

}
