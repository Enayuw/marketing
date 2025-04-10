import com.br.marketing.check.CkeckApplication;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.retry.DatabaseOperationService;
import com.br.marketing.service.Impl.transfertofile.*;
import com.br.marketing.service.SyncConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * xiechengTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class XieChengTest {
    protected final static Logger log = LoggerFactory.getLogger(XieChengTest.class);

    @Autowired
    SyncConfigService syncConfigService;

    @Resource
    TransferToFileByXieChengServiceImpl TransferToFileByXieChengServiceImpl;

    @Test
    public void testActionTransferToFile(){
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("3710058");
        transferFileTask.setFileType(2);
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        transferFileTask.setStartDate(yyyyMMdd);
        log.warn("携程转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        // TransferToFileByXieChengServiceImpl.actionTransferToFile(transferFileTask,"");
    }

    @Resource
    private DatabaseOperationService dbService;

    @Resource
    private XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;

    @Test
    public void test04() {

        try {
            DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
            dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                @Override
                public void execute() {
                    XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
                    taskExample.createCriteria().andIdEqualTo(1L);
                    List<XiechengCollidingDataProcessTask> xiechengCollidingDataProcessTasks = xiechengCollidingDataProcessTaskMapper.selectByExample(taskExample);
                }

                @Override
                public Object getParams() {
                    XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
                    taskExample.createCriteria().andIdEqualTo(1L);
                    return taskExample;
                }

                @Override
                public String getMapperClass() {
                    return "com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapperBase";
                }

                @Override
                public String getMapperMethod() {
                    return "selectByExample";
                }
            },"",config);
        } catch (Exception e) {

        }
    }

}
