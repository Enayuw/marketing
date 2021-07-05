import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.api.MarketingApiApplication;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailVariablesDTO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.RandomUtils;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.ThreeDes;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.mapper.MarketingStrategyProductMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;


@RunWith(SpringJUnit4ClassRunner.class)
public class MyTest {

    @Test
    public void testThread(){
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(30, 30,60L,TimeUnit.SECONDS
                ,new ArrayBlockingQueue(200),new ThreadFactoryBuilder().setNameFormat("br-test-pool-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            final int a = i;
            threadPoolExecutor.submit(()->{
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String name = Thread.currentThread().getName();
                System.out.println(name.concat(":").concat(String.valueOf(a)));
            });
        }
        threadPoolExecutor.shutdown();
        Boolean b = true;
        while (b){
            if(threadPoolExecutor.isTerminated()){
                System.out.println("结束");
                b=false;
            }else{
                System.out.println("休息");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("结束:".concat(String.valueOf(end-start)));

    }

    @Test
    public void testInt(){
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(30, 30,60L,TimeUnit.SECONDS
                ,new ArrayBlockingQueue(200),new ThreadFactoryBuilder().setNameFormat("br-test-pool-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
        AtomicLong l = new AtomicLong();
        Integer b = 0;
        for (int i = 0; i < 10000; i++) {
            final int a = i;
            threadPoolExecutor.submit(()->{
                l.getAndAdd(a);
            });
            b+=i;
        }
        threadPoolExecutor.shutdown();
        Boolean c = true;
        while (c){
            if(threadPoolExecutor.isTerminated()){
                System.out.println("结束");
                c=false;
            }else{
                System.out.println("休息");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("耗时l:"+l.get());
        System.out.println("耗时b:"+b);
    }
}
