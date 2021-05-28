import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.MarketingApiApplication;
import com.br.marketing.client.RedisService;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.entity.ProInSys;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;


@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = MarketingApiApplication.class)
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
