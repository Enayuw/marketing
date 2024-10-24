
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.validators.user.UserValidator;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class MyTest {

    final static SimpleDateFormat yyyyMMddHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String msTimeRegex = "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$|^\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}:\\d{3}$";
    @Test
    public void testTime(){
        LocalDate startDate = LocalDate.parse("2021-12-29",DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate now = LocalDate.now();
        long days = startDate.until(now, ChronoUnit.DAYS);
        System.out.println(days);
    }

    @Test
    public void A(){
        Integer a =1;
        B(a);
        System.out.println("对象值"+a);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        C(list);
        System.out.println(list.toString());

    }

    private void C(List<Integer> l){
        l.add(123);
    }
    private void B(Integer a){
        a=2;
    }

    @Test
    public void testaesde(){
        try {
            String s = AESUtil.aesDecrypt("2teLt5s8LnvmmLLzEYukRV8qcOc1tKK0EUx2J+eyu1A=", "ovksl39fcl13m9dF");
            String s1 = AESUtil.aesDecrypt("18822755986", "ovksl39fcl13m9dF");
            System.out.println(s);
            System.out.println(s1);
        }catch (Exception ex){

        }
    }

    @Test
    public void checkName(){
        UserValidator userValidator = new UserValidator(2);
        boolean b = userValidator.validateName("迪拉帢尔·帕塔尔·土尔根");
        boolean b1 = userValidator.validateId("650102199402022617");
        System.out.println(b);
        System.out.println(b1);

        Pattern NAME = Pattern.compile("^(?=[\\u4dae\\u00b7\\u2022\\u2027\\uff65\\u4e00-\\u9fa5]{2,25}$)([\\u4dae\\u4e00-\\u9fa5]{2,15}|([\\u4e00-\\u9fa5]+[\\u00b7\\u2022\\u2027\\uff65][\\u4e00-\\u9fa5]+))$");
        boolean matches = NAME.matcher("迪拉帢尔·帕塔尔·土尔根").matches();
        System.out.println(matches);
    }

    @Test
    public void haluoBydxTimeFormat(){
        String time = "2021-10-12 18:07:00:000";
        if(StringUtils.isBlank(time)){
            System.out.println("a====="+time);
        }

        if(Pattern.matches(msTimeRegex,time)){
            System.out.println("b====="+time.replace(":000",""));
        }

        System.out.println("c====="+time);
    }

    @Test
    public void testThreadSafe(){
        String abc = "2021-08-11 11:00:00";
        for(int i = 0;i<20;i++){

            new Thread(()->{
                    try {
                        System.out.println(yyyyMMddHMS.format(yyyyMMddHMS.parse(abc)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
            }).start();
        }
    }

    @Test
    public void testStr(){

        Date yyyyMMdd1 = null;
        try {
            yyyyMMdd1 = new SimpleDateFormat("yyyy-MM-dd").parse("2021-07-26");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(yyyyMMdd1);
        System.out.println(yyyyMMdd);
        HashMap<String,String> hs = new HashMap();
        hs.put("checkBlackList",new String("1"));
        if(hs.get("checkBlackList")=="1"){
            System.out.println("====");
        }else{
            System.out.println("////");
        }
    }

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

    @Test
    public void testLog(){
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("a",123);
        objectObjectHashMap.put("b", Arrays.asList(1,2,3,4));
        log.warn("【跑批任务】调度结束，耗时：{},分片：{}",1,objectObjectHashMap);
    }

    @Test
    public void testLong(){
        Long a = 2L;
        Integer b = 2;
        ArrayList<Integer> objects = new ArrayList<>();
        objects.add(0);
        objects.add(1);
        boolean contains = Arrays.asList(0, 1).contains(a % 2);
        System.out.println("输出："+contains+"ceshi:"+a % 2);
        boolean containsb = objects.contains(a % 2);
        System.out.println("输出2："+containsb+"ceshi:"+a % 2);
        boolean containsc = objects.contains(b % 2);
        System.out.println("输出3："+containsc+"ceshi:"+a % 2);
    }

}
