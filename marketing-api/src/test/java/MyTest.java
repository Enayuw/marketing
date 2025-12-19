
import com.alibaba.fastjson.JSON;
import com.br.marketing.common.bean.ScoreLable;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.entity.XiechengCollidingDataProcessTask;
import com.br.marketing.entity.XiechengCollidingDataProcessTaskExample;
import com.br.marketing.es.bean.MarketingCondition;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.retry.DatabaseOperationService;
import com.br.marketing.util.GeneScriptUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    public void testaesde256(){
        try {
            // 客户提供的AES-256密钥（十六进制）
            String hexKey = "40999bbc7cdc1a14a1c61a3fb9a74485f196f4a8d205e76966ef44178f0827b5";
            System.out.println("原始十六进制密钥: " + hexKey);
            System.out.println("密钥长度: " + hexKey.length() + " 字符");
            
            // 将十六进制字符串转换为字节数组
            byte[] keyBytes = hexStringToByteArray(hexKey);
            System.out.println("转换后密钥字节长度: " + keyBytes.length + " 字节");
            
            // 打印密钥字节数组（用于调试）
            System.out.print("密钥字节数组: ");
            for (byte b : keyBytes) {
                System.out.printf("%02x ", b);
            }
            System.out.println();
            
            // 先尝试ECB模式
            try {
                String s = aesDecryptWithBytes("D9GoyhNjdo+tb4tK9xsuR+ZGekVy8a58k6XehTOmiMs=", keyBytes);
                System.out.println("ECB模式解密结果: " + s);
            } catch (Exception e) {
                System.out.println("ECB模式失败，尝试其他模式:");
                // 尝试不同的解密模式
                tryDifferentModes("D9GoyhNjdo+tb4tK9xsuR+ZGekVy8a58k6XehTOmiMs=", keyBytes);
            }
        }catch (Exception ex){
            System.out.println("解密失败，异常信息:");
            ex.printStackTrace();
        }
    }
    
    /**
     * 使用字节数组密钥进行AES解密
     */
    private String aesDecryptWithBytes(String encryptedData, byte[] keyBytes) throws Exception {
        javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(org.apache.commons.codec.binary.Base64.decodeBase64(encryptedData));
        return new String(decryptedBytes, "UTF-8");
    }
    
    /**
     * 尝试不同的AES解密模式
     */
    private void tryDifferentModes(String encryptedData, byte[] keyBytes) {
        String[] modes = {
            "AES/ECB/PKCS5Padding",
            "AES/ECB/NoPadding", 
            "AES/CBC/PKCS5Padding",
            "AES/CBC/NoPadding"
        };
        
        for (String mode : modes) {
            try {
                System.out.println("尝试模式: " + mode);
                javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
                javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(mode);
                
                if (mode.contains("CBC")) {
                    // CBC模式需要IV，使用密钥的前16字节作为IV
                    byte[] iv = new byte[16];
                    System.arraycopy(keyBytes, 0, iv, 0, 16);
                    javax.crypto.spec.IvParameterSpec ivSpec = new javax.crypto.spec.IvParameterSpec(iv);
                    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, ivSpec);
                } else {
                    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
                }
                
                byte[] decryptedBytes = cipher.doFinal(org.apache.commons.codec.binary.Base64.decodeBase64(encryptedData));
                String result = new String(decryptedBytes, "UTF-8");
                System.out.println("成功! 解密结果: " + result);
                return;
            } catch (Exception e) {
                System.out.println("模式 " + mode + " 失败: " + e.getMessage());
            }
        }
        System.out.println("所有模式都失败了");
    }
    
    /**
     * 十六进制字符串转字节数组
     */
    private byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
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

    @Test
    public void test01() {
        String str = "[{\"labels\":[{\"labelKey\":\"listValue\",\"labelValue\":\"高价值\"},{\"labelKey\":\"valueType\",\"labelValue\":\"type1\"}],\"order\":1,\"condition\":{\"type\":\"logic\",\"logic\":\"or\",\"data\":[{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]}]}},{\"labels\":[{\"labelKey\":\"listValue\",\"labelValue\":\"下探1\"},{\"labelKey\":\"valueType\",\"labelValue\":\"type2\"}],\"order\":2,\"condition\":{\"type\":\"logic\",\"logic\":\"or\",\"data\":[{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]}]}}]";

        String str1 = "[{\"labels\":[{\"labelKey\":\"listValue\",\"labelValue\":\"高价值\"},{\"labelKey\":\"valueType\",\"labelValue\":\"type1\"}],\"order\":1,\"condition\":{\"type\":\"logic\",\"logic\":\"or\",\"data\":[{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]}]}},{\"labels\":[{\"labelKey\":\"listValue\",\"labelValue\":\"下探1\"},{\"labelKey\":\"valueType\",\"labelValue\":\"type2\"}],\"order\":2,\"condition\":{\"type\":\"logic\",\"logic\":\"or\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashon58xkcsxcd\",\"operation\":\"between_left\",\"value\":\"75,80\"},{\"type\":\"operation\",\"key\":\"scorencashon58xkcsxcd\",\"operation\":\"between_left\",\"value\":\"80,85\"}]}}]";
        String str2 = "[{\"labels\":[{\"labelKey\":\"listValue\",\"labelValue\":\"高价值\"},{\"labelKey\":\"valueType\",\"labelValue\":\"type1\"}],\"order\":1,\"condition\":{\"type\":\"logic\",\"logic\":\"or\",\"data\":[{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"65,70\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"60,65\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"55,60\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"50,55\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"45,50\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"5,10\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"10,15\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"15,20\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"20,25\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"25,30\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"30,35\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"35,40\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"40,45\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"45,50\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"=\",\"value\":\"\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"0,5\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"5,10\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"10,15\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"15,20\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"20,25\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"25,30\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"30,35\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"35,40\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]},{\"type\":\"logic\",\"logic\":\"and\",\"data\":[{\"type\":\"operation\",\"key\":\"scorencashonxchx\",\"operation\":\"between_right\",\"value\":\"40,45\"},{\"type\":\"operation\",\"key\":\"scorencashonxctx\",\"operation\":\"between_right\",\"value\":\"50,55\"}]}]}}]";
        List<ScoreLable> scoreLables = GeneScriptUtil.getScoreLables(str2, false);
    }

    @Test
    public void test09() {
        String strategyCodeOriginal = "12345CASTR0321371";
        String substring = strategyCodeOriginal.length() < 12
                ? strategyCodeOriginal
                : strategyCodeOriginal.substring(strategyCodeOriginal.length() - 12);
        String substring1 = strategyCodeOriginal.substring(0, strategyCodeOriginal.length() - 12);
        System.out.println(substring);
        System.out.println(substring1);
    }

    @Test
    public void test10() {
        StandardEvaluationContext context = new StandardEvaluationContext();
        Map map = new HashMap<String, String>();
        map.put("als_m1_id_nbank_orgnum", 0.0);
//        map.put("als_m1_cell_nbank_orgnum", "");
//        map.put("als_m3_id_nbank_orgnum", 3.33);
//        map.put("als_m3_cell_nbank_orgnum", "1");
        context.setVariables(map);
//        String condition = "((#pd_id_apply_age != '')&&((#pd_id_apply_age < '22')||(#pd_id_apply_age > '55')))||((#pd_cell_apply_age != '')&&((#pd_cell_apply_age < '22')||(#pd_cell_apply_age > '55')))";
//        String condition = "((#pd_id_apply_age != '')&&((#pd_id_apply_age < '22')||(#pd_id_apply_age > '55')))";
//        String condition = "(#sl_id_nbank_bad_allnum >= '1')";
//        String condition = "(#als_m1_id_nbank_orgnum >= '13')||(#als_m3_id_nbank_orgnum >= '28')||(#als_m3_id_nbank_orgnum <= '0')||(#als_m1_cell_nbank_orgnum >= '13')||(#als_m3_cell_nbank_orgnum >= '28')||(#als_m3_cell_nbank_orgnum <= '0')";
        String condition = "(#als_m1_id_nbank_orgnum == 0)";
        ExpressionParser parser = new SpelExpressionParser();
        Boolean value = parser.parseExpression(condition).getValue(context, Boolean.class);
        System.out.println(value);
    }

    @Test
    public void test11() {
        String format = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(format);
        Date from = Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        System.out.println(from);
    }

    @Test
    public void test02() {
        MarketingHistory marketingHistory1 = new MarketingHistory();
        marketingHistory1.setCell("AgsDU1lSVΒ7g8BVQc");
        MarketingCondition marketingCondition11 = new MarketingCondition();
        marketingCondition11.setFieldKey("pd_id_apply_age");
        marketingCondition11.setStrValue("18");
        MarketingCondition marketingCondition12 = new MarketingCondition();
        marketingCondition12.setFieldKey("ka_id_province");
        marketingCondition12.setStrValue("青海");
        marketingHistory1.setCondition(Arrays.asList(marketingCondition11, marketingCondition12));

        MarketingHistory marketingHistory2 = new MarketingHistory();
        marketingHistory2.setCell("UwsNΒ6AQ8JAFNcVFw");
        MarketingCondition marketingCondition21 = new MarketingCondition();
        marketingCondition21.setFieldKey("pd_id_apply_age");
        marketingCondition21.setStrValue("23");
        MarketingCondition marketingCondition22 = new MarketingCondition();
        marketingCondition22.setFieldKey("ka_id_province");
        marketingCondition22.setStrValue("北京");
        marketingHistory2.setCondition(Arrays.asList(marketingCondition21, marketingCondition22));

        List<MarketingHistory> marketingHistories = Arrays.asList(marketingHistory1, marketingHistory2);
        System.out.println(JSON.toJSONString(marketingHistories));
    }

    @Test
    public void test03() {
        String str = "";
        System.out.println(isNumeric(str));

        String s = LocalDate.now().toString();
        System.out.println(s);
    }

    private boolean isNumeric(String str) {
        return str != null && str.matches("[+-]?(?:\\d+\\.\\d*|\\.\\d+|\\d+)(?:[eE][+-]?\\d+)?");
    }


}
