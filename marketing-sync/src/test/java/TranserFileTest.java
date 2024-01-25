import com.br.marketing.entity.SyncConfig;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.sync.SyncApplication;
import com.br.marketing.sync.service.impl.SyncServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

/**
 * xiechengTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {SyncApplication.class})
@WebAppConfiguration
public class TranserFileTest implements ApplicationContextAware {
    protected final static Logger log = LoggerFactory.getLogger(TranserFileTest.class);

    @Autowired
    SyncServiceImpl syncService;
    @Resource
    SyncConfigMapper syncConfigMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SyncApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }

    @Test
    public void test(){
        SyncConfig config = new SyncConfig();
        config.setType(1);
        config.setApiCode("3710065");
        config.setDataType(6);
        SyncConfig queryConfig = syncConfigMapper.queryConfigByConditaion(config);
        List<SyncConfig> loanSyncConfigs = new ArrayList<>();
        loanSyncConfigs.add(queryConfig);
        syncService.sync(loanSyncConfigs);
    }

}
