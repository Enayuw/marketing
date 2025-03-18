import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.utils.BrExecutors;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Bairong on 2020/7/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {RedisChgService.class})
@Slf4j
public class ThreadTest {
    @Resource
    RedisChgService redisChgService;
    // 通过静态方法创建ScheduledExecutorService的实例
    private ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(4);

    @Test
    public void testStopThread(){
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(20, 20);
        threadPool.submit(()->{
            System.out.println("123");
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                System.out.println("手动停止");
            }
            System.out.println("456");
        });

        List<Runnable> runnables = threadPool.shutdownNow();
        runnables.forEach(t->System.out.println("错误"+t.toString()));
        Boolean exec = Boolean.TRUE;
        while (exec){
            if(threadPool.isTerminated()){
                exec = Boolean.FALSE;
            }
        }
    }

    @Test
    public void test() {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 5, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue(1), new ThreadPoolExecutor.CallerRunsPolicy());

        for (int i = 0; i < 35; i++) {
            log.info("i:{}", i);
            int activeCount = threadPoolExecutor.getActiveCount();
            int quqesize=threadPoolExecutor.getQueue().size();
            log.info("activeCount:{},size:{}", activeCount, quqesize);
            int size=Integer.parseInt(redisChgService.get("THREADTEST_000001"));
            int size1=Integer.parseInt(redisChgService.get("THREADTEST_000001"));

            while (activeCount>=size){
                activeCount = threadPoolExecutor.getActiveCount();
                size = Integer.parseInt(redisChgService.get("THREADTEST_000001"));
                if(size1!=size){
                    log.info("size------------------:{}",size);
                }
            }

            threadPoolExecutor.submit(new test());
        }


        threadPoolExecutor.shutdown();
        while (true){
            if(threadPoolExecutor.isTerminated()){
                log.warn("所有线程都执行结束");
                break;
            }
            try {
                Thread.sleep(3000);
                log.warn("waiting-----------");
            }catch (Exception e){
            }
        }
    }

    class test implements Callable<String> {

        @Override
        public String call() throws Exception {
            try {
                 log.info("sleep......");
                Thread.sleep(2*60*60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    @Test
    public void test_schedule4Callable() throws Exception {
        // 延时任务
    /*    mScheduledExecutorService.schedule(threadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                log.info("lzp", "first task");
            }
        }), 1, TimeUnit.SECONDS);*/

        // 循环任务，按照上一次任务的发起时间计算下一次任务的开始时间
        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("lzp", "first:" + System.currentTimeMillis() / 1000);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        // 循环任务，以上一次任务的结束时间计算下一次任务的开始时间
       /* mScheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                log.info("lzp", "scheduleWithFixedDelay:" + System.currentTimeMillis() / 1000);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);*/

    }

    public static void main(String[] args) {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("run "+ System.currentTimeMillis());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executorService.shutdown();
            }
        }, 1000,  TimeUnit.MILLISECONDS);
    }
}
