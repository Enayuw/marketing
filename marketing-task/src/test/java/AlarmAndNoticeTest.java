import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.mapper.LoanFileMapper;
import com.br.marketing.service.EmailService;
import com.br.marketing.task.Scheduler;
import com.br.marketing.task.utils.HxUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by Bairong on 2020/7/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {Scheduler.class})
@WebAppConfiguration
public class AlarmAndNoticeTest {
    protected final static Logger log = LoggerFactory.getLogger(AlarmAndNoticeTest.class);
    @Resource
    EmailService businessAlarmServiceImpl;
    @Resource
    EmailService reportServiceImpl;
    @Resource
    EmailService ppdNoticeServiceImpl;
    @Resource
    EmailService validDataAlarmServiceImpl;

    @Resource
    LoanFileMapper loanFileMapper;
    @Resource
    AlarmApiClient alarmApiClient;
    @Test
    public void hxflat(){
        String s="{\"swift_number\":\"4002511_20200818102903_836744281\",\"code\":\"00\",\"Flag\":{\"scoremconsonsncfclxmodel\":\"1\"},\"scoremconsonsncfclxmodel\":{\"score\":\"100\"}}";
        String s1 = HxUtil.hauXiangFlat(s);
        System.out.println(s1);
    }

    @Test
    public void test(){
        businessAlarmServiceImpl.closeDateAlarm();
        businessAlarmServiceImpl.monitoringExpirationAlarm();
    }
    @Test
    public void sendreportTest(){
        ppdNoticeServiceImpl.sendReport("4200777");
    }
    @Test
    public void resultVolumeCheckTest(){
        businessAlarmServiceImpl.resultVolumeCheck("4200333");
    }
    @Test
    public void ftpToSftpCheckTest(){
        businessAlarmServiceImpl.ftpToSftpCheck("4200777");
    }
    @Test
    public void fileSizeTest(){
        businessAlarmServiceImpl.fileSizeException("4200777","4200777_ceshi_2020071412312.zip,2097154");
    }
    @Test
    public void report(){
        reportServiceImpl.report();
    }
    @Test
    public void progressReport(){
        reportServiceImpl.progressReport();
    }
    @Test
    public void fileUploadFtpException(){
        businessAlarmServiceImpl.fileUploadFtpException("4200777","4200777_ceshi_2020071412312.zip,2097154,2097778");
    }

    @Test
    public void fileUpload(){
        validDataAlarmServiceImpl.fileUpload("4200777","4200777_20200806142007_8932");
    }
    @Test
    public void dataFileVolumn(){
        validDataAlarmServiceImpl.dataFileVolumn("4200333","4200333_ceshi_2020071412312.txt,123,456");
    }

    @Test
    public void deleteMonitor(){
        validDataAlarmServiceImpl.deleteMonitorFileUpload("4200333","4200333_p4_DeleteMonitor_202008284200333");
    }
}
