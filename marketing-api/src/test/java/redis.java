
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Date;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.*;
import com.br.marketing.entity.MarketingStrategyProduct;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.es.bean.Product;
import com.br.marketing.es.service.MarketingHistoryEsService;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.MarketingStrategyProductMapper;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.vo.CustGroupTempVO;
import com.br.marketing.vo.TaskExtendInfoVO;
import com.google.common.collect.Lists;

import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.api.MarketingApiApplication;
import com.br.marketing.client.RedisService;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailVariablesDTO;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.ProInSys;
import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.*;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MarketingApiApplication.class)
public class redis {

//    @Resource
//    RedisService redisService;
//
//    @Test
//    public void test(){
//       // String s = redisService.get("redisProduct_loan_5200156_ApplyLoanInterval_V1.0");
//        String s = redisService.get("productionMng-allProductions");
//        JSONArray array=new JSONArray();
//        List<ProInSys> proInSys = JSONArray.parseArray(s, ProInSys.class);
//        Iterator<ProInSys> iterator = proInSys.iterator();
//        while (iterator.hasNext()){
//            ProInSys pro=iterator.next();
//            if(pro.getBusinessTypeCode().indexOf(Constants.LOAN_BUSINESSTYPECODE)==-1){
//                iterator.remove();
//            }
//        }
//        if(proInSys.size()>0){
//            String json= JSONObject.toJSONString(proInSys);
//            array=JSONArray.parseArray(json);
//        }
//        System.out.println(array);
//    }

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    StraHisFileMapper straHisFileMapper;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Test
    public void testPushUser(){
        String s = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode("AgsNΒ7VlVSWwkAVwY").getBytes());
        String s2 = DigestUtils.md5DigestAsHex(BrCipherMaker.getInstance().decode("Uw0JDAoΒ6DBVRdXF0").getBytes());
        System.out.println(s);
        System.out.println(s2);
        String ss = BrCipherMaker.getInstance().decode("UwAKCQAFCVBXV1Β6M");
        System.out.println(ss);
//        String ab = "{\"apiCode\":\"7410438\",\"jsonData\":{\"accessNumber\":\"123123_2\",\"batchNumber\":\"123123\",\"data\":[{\"caseNumber\":\"1_82021072601_csd_1627293995809\",\"phone\":\"AgsNΒ7VlVSWwkAVwY\",\"variables\":{\"groupType\":\"促首登\",\"score\":\"83.0\",\"scoreDate\":\"2021-07-26\",\"scoreName\":\"scorencashonshcdlyxf\",\"taskId\":\"82021072601\",\"update\":\"\"}}],\"extendData\":{\"sampleTotal\":\"1\",\"scoreName\":\"scorencashonshcdlyxf\"},\"method\":\"caseAdd\"},\"platApiCode\":\"7410438\"}";
//        PushMarketingUserDTO o = JSON.parseObject(ab, new TypeReference<PushMarketingUserDTO>() {
//        }.getType());
//        Result<Integer> integerResult = intelligentCustomerServiceClient.pushUser(o, 123L, "123");
//        System.out.println(integerResult.getMessage());
    }

    @Test
    public void testRedis(){
        String s = redisChgService.get("acb:");
        boolean notBlank = StringUtils.isNotBlank(s);
        boolean notBlank2 = StringUtils.isNotBlank(null);
        boolean notBlank1 = StringUtils.isNotBlank("");
        System.out.println("test");
    }

    @Test
    public void serTest(){
        PushMarketingUserDetailVariablesDTO pushMarketingUserDetailVariablesDTO = new PushMarketingUserDetailVariablesDTO();
        pushMarketingUserDetailVariablesDTO.setScore("123");
        pushMarketingUserDetailVariablesDTO.setScoreDate("123");
        pushMarketingUserDetailVariablesDTO.setScoreName("123");
        pushMarketingUserDetailVariablesDTO.setUpdate("123");
        System.out.println(JSON.toJSONString(pushMarketingUserDetailVariablesDTO));
        List<String> strList = new ArrayList<>();
        strList.add("123");
        strList.add("456");
        strList.add("789");
        here: for (String s : strList) {

        }
    }

    @Test
    public void testPool(){
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(10, 10);
        ThreadPoolExecutor threadPool2 = BrExecutors.getThreadPool(10, 10);
        for (int i = 0; i < 10; i++) {
            threadPool.submit(()->{System.out.println("threadPool==="+Thread.currentThread().getName()+Thread.currentThread().getId());});
            threadPool2.submit(()->{System.out.println("threadPool2==="+Thread.currentThread().getName()+Thread.currentThread().getId());});
        }
    }

    @Test
    public void testDes(){
        String s = "{\"taskId\":\"4ec94c9e2a61a05bf912a3b9f9684f1a\",\"dataItems\":[{\"cell\":\"67c3461a3ef7be453775cf9227fa60db\",\"groupType\":\"促首登\",\"caseNum\":\"1\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"1\",\"reserveField2\":\"1\"},{\"cell\":\"e01d4dc231b25fef9672b43408c4a496\",\"groupType\":\"促首登\",\"caseNum\":\"2\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"2\",\"reserveField2\":\"2\"},{\"cell\":\"d258d79465755613dc28fe28b71b13cd\",\"groupType\":\"促首登\",\"caseNum\":\"3\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"3\",\"reserveField2\":\"3\"},{\"cell\":\"feeba7577a2de1521619c4629b4123c1\",\"groupType\":\"促首登\",\"caseNum\":\"4\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"4\",\"reserveField2\":\"4\"},{\"cell\":\"a3627d465c9466abfe78ea8695733c27\",\"groupType\":\"促首登\",\"caseNum\":\"5\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"5\",\"reserveField2\":\"5\"},{\"cell\":\"6f86c136018169210d813093c0218fb8\",\"groupType\":\"促首登\",\"caseNum\":\"6\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"6\",\"reserveField2\":\"6\"},{\"cell\":\"5d11596a5966d1344ec7738ea0956256\",\"groupType\":\"促首登\",\"caseNum\":\"7\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"7\",\"reserveField2\":\"7\"},{\"cell\":\"368f5fe5a8544afe18745f1297a3f02d\",\"groupType\":\"促首登\",\"caseNum\":\"8\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"8\",\"reserveField2\":\"8\"},{\"cell\":\"2c284c58471060b8f61b338619ace49d\",\"groupType\":\"促首登\",\"caseNum\":\"9\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"9\",\"reserveField2\":\"9\"},{\"cell\":\"f4eae314a456eb2e49bff509131f52af\",\"groupType\":\"促首登\",\"caseNum\":\"10\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"10\",\"reserveField2\":\"10\"},{\"cell\":\"28ad5e6d5c0ab384634c17e465b23371\",\"groupType\":\"促首登\",\"caseNum\":\"11\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"11\",\"reserveField2\":\"11\"},{\"cell\":\"2874e8d6c8a24a76b3b90ff041e50a0a\",\"groupType\":\"促首登\",\"caseNum\":\"12\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"12\",\"reserveField2\":\"12\"},{\"cell\":\"9945ac42bad09b576048b03918a5f090\",\"groupType\":\"促首登\",\"caseNum\":\"13\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"13\",\"reserveField2\":\"13\"},{\"cell\":\"1a3d04d493e6554b5148907d36057b11\",\"groupType\":\"促首登\",\"caseNum\":\"14\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"14\",\"reserveField2\":\"14\"},{\"cell\":\"8a034fa1866f256cc589af717a4a705c\",\"groupType\":\"促首登\",\"caseNum\":\"15\",\"registerDate\":\"2021-06-04\",\"reserveField1\":\"15\",\"reserveField2\":\"15\"}]}";
        MarketingPreUserDTO o = JSON.parseObject(s, new TypeReference<MarketingPreUserDTO>() {
        }.getType());
        System.out.println(o.toString());
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
    public void randomTest(){
        String s = RandomUtils.randomStr(2);
        String s1 = RandomUtils.randomStr(2);
        String s2 = RandomUtils.randomStr(2);
        System.out.println(s+"__"+s1+"__"+s2);

    }

    @Test
    public void testConcurrent(){
        Integer k=10;
        List<String> list =new ArrayList();
        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Callable<List>> callableList = new ArrayList<>();
        for (Integer i = 0; i < k; i++) {
            callableList.add(()->{
                List list1 = new ArrayList();
                Integer kk = 20000;
                for (Integer integer = 0; integer < kk; integer++) {
                    list1.add("kk".concat(String.valueOf(kk)));
                }
                return list1;
            });
        }
        try {
            List<Future<List>> futures = executorService.invokeAll(callableList);
            futures.forEach(t->{
                try {
                    list.addAll(t.get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(list.size()+"_________"+(System.currentTimeMillis()-start));

        long start2 = System.currentTimeMillis();
        Integer kkk=200000;
        List<String> list2 =new ArrayList();
        for (Integer i = 0; i < kkk; i++) {
            list2.add("kkk".concat(String.valueOf(kkk)));
        }
        System.out.println(list2.size()+"_________"+(System.currentTimeMillis()-start2));
        System.out.println("1231");



    }

    @Test
    public void encAnddec() throws Exception {
        String s = ThreeDes.encryptByCbc("123", "abcddesds", "abcdefgh");
        String abcddesds = ThreeDes.decryptByCbc(s, "abcddesds","hhhhtttt");
        System.out.println(abcddesds);
    }

    @Test
    public void indexTest(){
        String s = "acd-acd-acd-acd";
        int acd = s.indexOf("acd");
        System.out.println(acd);
        String s1 = "abd-acd-acd-acd";
        int acd1 = s1.indexOf("acd");
        System.out.println(acd1);
        int abd = s.indexOf("abd");
        System.out.println(abd);
    }

    @Autowired
    MarketingHistoryEsService marketingHistoryEsService;

    @Autowired
    MarketingTaskMapper marketingTaskMapper;

    @Autowired
    MarketingStrategyProductMapper productMapper;

    DecimalFormat df = new DecimalFormat("######0.000");
    @Test
    public void testScript(){
//        Date date = new Date();
//        String apiCode = "7410431";
//        for (int k = 0; k < 10; k++) {
//            final Integer m = k;
//            new Thread(()->{
//            //region 跑测试数据
//            double scoreA = 0.001;
//            double scoreB = 0.001;
//
//            String number = "7410431_20210621172100_yp_test6_" + String.valueOf(m);
//            StraHisFile file = new StraHisFile();
//            file.setApiCode(apiCode);
//            file.setBatchNumber(number);
//            Date date1 = new Date();
//            file.setCreateTime(date1);
//            file.setUpdateTime(date1);
//            file.setStatus(2);
//            file.setZipStatus(1);
//            file.setScoreStatus(2);
//            file.setFilePath("");
//            file.setFileSize("0");
//            file.setType(1);
//            straHisFileMapper.insertSelective(file);
//
//            MarketingStrategyProduct product1 = new MarketingStrategyProduct();
//            product1.setFileId(file.getId());
//            product1.setApiCode(apiCode);
//            product1.setCusBatchNumber(number);
//            product1.setBatchNumber(number);
//            product1.setStrategyId("DTM_BR0000005");
//            product1.setProductName("scorencashonszyxxy");
//            product1.setProductVersion("S1_0");
//            product1.setIsDel(1);
//            product1.setCreateTime(date1);
//
//            MarketingStrategyProduct product2 = new MarketingStrategyProduct();
//            product2.setFileId(file.getId());
//            product2.setApiCode(apiCode);
//            product2.setCusBatchNumber(number);
//            product2.setBatchNumber(number);
//            product2.setStrategyId("DTM_BR0000005");
//            product2.setProductName("scoremcashonxhqbdzcd");
//            product2.setProductVersion("S1_0");
//            product2.setIsDel(1);
//            product2.setCreateTime(date1);
//            productMapper.insertSelective(product1);
//            productMapper.insertSelective(product2);
//
//            MarketingTask task = new MarketingTask();
//            task.setApiCode(apiCode);
//            task.setBatchNumber(number);
//            task.setFileName("1");
//            task.setStrategyId("DTM_BR0000005");
//            task.setFrequency("1");
//            task.setCreateTime("2021-06-19 00:00:00");
//            task.setUpdateTime("2021-06-19 01:00:00");
//            task.setMonitorStatus(1);
//            task.setStatus(2);
//            task.setTaskNumber(0);
//            task.setActualNumber(0);
//            task.setIncrement(0);
//            task.setBegin(0);
//            task.setEnd(0);
//            task.setTableName("1");
//            task.setStrategyName("1");
//            task.setStartDate("2021-06-19");
//            task.setCloseDate("2021-06-20");
//            task.setMonitorModel(0);
//            task.setIsCheck(0);
//            task.setHitDate("");
//            task.setErrorMessage("");
//            task.setCusBatch(number);
//            task.setMonitorType(0);
//            task.setQueryBeginDate("2021-06-19");
//            task.setQueryEndDate("2021-06-20");
//            task.setStart(0);
//            task.setLimit(0);
//            task.setStrategyType("1");
//            task.setIsRepair("1");
//            task.setDataVolume(0);
//            marketingTaskMapper.insertTask(task);
//
//            String filePath = "D:\\data\\test\\"+number+".text";
//            File file1 = new File(filePath);
//            Path path = Paths.get(filePath);
//            if(!file1.getParentFile().exists()){
//                file1.getParentFile().mkdirs();
//            }
//            if(!file1.exists()){
//                try {
//                    file1.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            try(BufferedWriter writer =
//                        Files.newBufferedWriter(path, StandardCharsets.UTF_8,
//                                StandardOpenOption.APPEND)) {
//                writer.write("batchNumber,cusNum,scorencashonszyxxy_score,scoremcashonxhqbdzcd_score\r\n");
//                ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(50, 50, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5000));
//                for (int i = 0; i < 1000000; i++) {
//                    threadPoolExecutor.submit(()->{
//                        //region 处理 数据入es和文件
//                        String cusNum = "yp_b_" + String.valueOf(m)+"s_"+ i;
//                        MarketingHistory bean = new MarketingHistory();
//                        bean.setApiCode(apiCode);
//                        bean.setIdCard("120222199" + String.valueOf(m) + i);
//                        bean.setCell("188" + String.valueOf(m) + i);
//                        bean.setName("燕萍_" + String.valueOf(m) + i);
//                        bean.setRequestTime(date);
//                        bean.setBatchNumber(number);
//                        bean.setCusBatchNumber(number);
//                        bean.setCusNum(cusNum);
//                        bean.setStrategyId("DTM_BR0000005");
//                        bean.setVersion("1");
//                        bean.setFileId(file.getId().toString());
//                        List<Product> products = new ArrayList<>();
//                        for (int j = 0; j < 2; j++) {
//                            Product product = new Product();
//                            if (j == 0) {
//                                product.setCode("scorencashonszyxxy");
//                                product.setVersion("S1_0");
//                                product.setCodeVersion("scorencashonszyxxy_S1_0");
//                                scoreA = new BigDecimal(scoreA + 0.001).setScale(2,BigDecimal.ROUND_DOWN).doubleValue();
//                                product.setScore(scoreA);
//                            } else {
//                                product.setCode("scoremcashonxhqbdzcd");
//                                product.setVersion("S1_0");
//                                product.setCodeVersion("scoremcashonxhqbdzcd_S1_0");
//                                scoreB = new BigDecimal(scoreB + 0.001).setScale(2,BigDecimal.ROUND_DOWN).doubleValue();
//                                product.setScore(scoreB);
//                            }
//                            product.setFlag("1");
//                            products.add(product);
//                        }
//                        bean.setProduct(products);
//                        marketingHistoryEsService.insert(bean, UUID.randomUUID().toString());
//                        writer.write(number.concat(",").concat(cusNum).concat(",").concat(String.valueOf(scoreA))
//                                .concat(",").concat(String.valueOf(scoreB)).concat("\r\n"));
//                        //endregion
//                    });
//
//                }
//            }catch(Exception ex){
//                System.out.println(ex.getMessage());
//            }
//            //endregion
//            }).start();
//        }
//
//        while(true){
//            try {
//                Thread.sleep(10000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

    }

    @Autowired
    MarketingUserMapper marketingUserMapper;

    @Test
    public void testGroup(){
        List<CustGroupTempVO> groupTypes = marketingUserMapper.selectGroupByCodeAndTime("7410437", "2021-07-13");
        System.out.println(groupTypes.toString());
    }

/*@Resource
    DecodeClient decodeClient;


    @Test
    public void test() {

        String name = decodeClient.decode("id", "c74adbb43d009d0d6e96bbaeeb1c8bac", "1003", "", "1231231231");
        System.out.println(name);

    }
 @Test
    public  void testLog(){
        long l = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<400;i++){
                    log.error("ceshi注入 StringRedisTemplate, 使用默认配置");
                }
            }
        }).start();

        log.error("{}",System.currentTimeMillis()-l);
    }*/

/*    @Test
    @SuppressWarnings("all")
    public void testExecutePipelined() {
      //  JedisCluster jedisCluster = JedisClusterUtil.getInstance().getJedisCluster();

        Set<HostAndPort> nodes = new HashSet<HostAndPort>();
        nodes.add(new HostAndPort("redis-cluster1-01.brapp.com", 7360));
        nodes.add(new HostAndPort("redis-cluster1-02.brapp.com", 7360));
        nodes.add(new HostAndPort("redis-cluster1-03.brapp.com", 7360));

        JedisCluster jc = new JedisCluster(nodes);

        JedisClusterPipeline pipelined = jcp.pipelined(jc);
        long s = System.currentTimeMillis();
       // jcp.refreshCluster();
        List<Object> batchResult = null;
        try {
            // batch write

 for (int i = 0; i < 10000; i++) {
                jcp.set("k" + i, "v1" + i);
            }
            jcp.sync();
            String[] ss={"cnt_loan_test:4002055:TotalLoan:totalCount", "cnt_loan_test:4002055:KeyAttribution:totalCount", "cnt_loan_test:4002055:Consumption_c:totalCount", "cnt_loan_test:4002055:ApplyLoanMon:totalCount", "cnt_loan_test:4002055:ApplyLoan_d:totalCount", "cnt_loan_test:4002055:Media_c:totalCount", "cnt_loan_test:4002055:Stability_c:totalCount", "cnt_loan_test:4002055:ApplyLoanStr:totalCount", "cnt_loan_test:4002055:SpecialList_c:totalCount", "cnt_loan_test:4002055:InfoRelation:totalCount"};

            // batch read
            for (int i=0;i<ss.length;i++) {
                jcp.get(ss[i]);
            }
            batchResult = jcp.syncAndReturnAll();
        } finally {
            jcp.close();
        }

        // output time
        long t = System.currentTimeMillis() - s;
        System.out.println(t);

        System.out.println(batchResult.size());
        // 实际业务代码中，close要在finally中调，这里之所以没这么写，是因为懒
        try {
            jc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 实际业务代码中，close要在finally中调，这里之所以没这么写，是因为懒
   // 使用 RedisCallback 把命令放在 pipeline 中
        RedisCallback<Object> redisCallback = new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String[] s={"cnt_loan_test:4002055:TotalLoan:totalCount", "cnt_loan_test:4002055:KeyAttribution:totalCount", "cnt_loan_test:4002055:Consumption_c:totalCount", "cnt_loan_test:4002055:ApplyLoanMon:totalCount", "cnt_loan_test:4002055:ApplyLoan_d:totalCount", "cnt_loan_test:4002055:Media_c:totalCount", "cnt_loan_test:4002055:Stability_c:totalCount", "cnt_loan_test:4002055:ApplyLoanStr:totalCount", "cnt_loan_test:4002055:SpecialList_c:totalCount", "cnt_loan_test:4002055:InfoRelation:totalCount"};

                for (int i=0;i<s.length;i++) {
                    connection.get(s[i].getBytes());
                }
                return null;
            }
        };
        System.out.println(redisTemplate.executePipelined(redisCallback));

        // 使用 SessionCallback 把命令放在 pipeline
 SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                operations.opsForValue().set("name", "qinyi");
                operations.opsForValue().set("gender", "male");
                operations.opsForValue().set("age", "19");

                return null;
            }
        };

        //System.out.println(stringRedisTemplate.executePipelined(redisCallback));
        System.out.println(redisTemplate.executePipelined(sessionCallback));
    }*/

}
