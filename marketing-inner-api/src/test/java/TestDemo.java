import com.br.marketing.common.constants.rocketmq.MarketingXieChengConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.innerapi.MarketingInnerApiApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MarketingInnerApiApplication.class})
@WebAppConfiguration
public class TestDemo {

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Test
    public void test01() {
        Long id = 147959l;
        rocketMqSwitch.syncSend(
                MarketingXieChengConstants.TOPIC,
                MarketingXieChengConstants.TAG_MARKETING_XIECHENG_REPORT,
                id.toString());
    }
}
