import com.br.marketing.client.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by Bairong on 2020/3/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {RedisService.class})
@Slf4j
public class RedisTest {
    @Resource
    RedisService redisService;


    @Test
    public void test1(){
        String loan_pro_info = redisService.get("LOAN_PRO_INFO");
        System.out.println(loan_pro_info);
    }
}
