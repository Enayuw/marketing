import com.br.marketing.monkey.MarketingDataMonkeyApplication;
import com.br.marketing.monkey.job.zhongan.ZhongAnPushRosterLockingDataJob;
import com.br.marketing.util.SHAUtils;
import com.br.marketing.util.SignUtils;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("appId", "42935819");
        String timestamp = System.currentTimeMillis() + "";
        System.out.println(timestamp);
        paramMap.put("timestamp", timestamp);
        paramMap.put("signType", "md5");
        paramMap.put("version", "1");
        String sign = SignUtils.yunKeSign(paramMap, "c2300278153c40e081cfb39462ff8db3");
        System.out.println(sign);
        //todo 完事删掉手机号
        List<String> list = new ArrayList<>();
        List<String> checkData = new ArrayList<>();
        list.add("19851670771");
        list.add("13471981003");
        list.add("13722559944");
        list.add("15554343888");
        list.add("13905134215");
        for(int i=0;i<list.size();i++){
            checkData.add(SHAUtils.encryptSHA1(list.get(i)));
        }
        System.out.println(checkData);

    }

}
