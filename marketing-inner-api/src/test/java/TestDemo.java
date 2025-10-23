import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.enums.XieChengConsumer;
import com.br.marketing.innerapi.MarketingInnerApiApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {MarketingInnerApiApplication.class})
@WebAppConfiguration
public class TestDemo {

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Autowired
    RedisChgService redisChgService;

    private void initializeConsumerQueue() {
        Long queueLength = redisChgService.llen(RedisKeyConstant.XIECHENG_REPORT_CONSUME_RNAME);
        if (queueLength == 0) {
            String[] consumers = Arrays.stream(XieChengConsumer.values())
                    .map(XieChengConsumer::getConsumerName)
                    .toArray(String[]::new);
            redisChgService.rpush(RedisKeyConstant.XIECHENG_REPORT_CONSUME_RNAME, consumers);
        }
    }

    @Test
    public void test() {
        initializeConsumerQueue();
        String consumerName = redisChgService.rpoplpush(RedisKeyConstant.XIECHENG_REPORT_CONSUME_RNAME);
        XieChengConsumer consumer = XieChengConsumer.fromName(consumerName);
        rocketMqSwitch.syncSend(consumer.getTopic(), consumer.getTag(), String.valueOf(7277679));
    }
}
