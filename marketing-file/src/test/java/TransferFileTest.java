import com.br.marketing.file.FileApplication;
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

/**
 * TransferFileTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {FileApplication.class})
@WebAppConfiguration
@Slf4j
public class TransferFileTest implements ApplicationContextAware {

//    @Autowired
//    FileSyncService fileSyncService;
    @Resource
    SyncConfigMapper syncConfigMapper;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        FileApplication.ac = (ConfigurableApplicationContext) applicationContext;
    }

    @Test
    public void test(){
//        SyncConfig config = new SyncConfig();
//        config.setType(1);
//        config.setApiCode("3710065");
//        config.setDataType(6);
//        SyncConfig queryConfig = syncConfigMapper.queryConfigByConditaion(config);
//        List<SyncConfig> syncConfigList = new ArrayList<>();
//        syncConfigList.add(queryConfig);
//        fileSyncService.pullFromSftp();
    }

}
