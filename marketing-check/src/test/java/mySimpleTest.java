//import com.br.marketing.check.CkeckApplication;
//import com.br.marketing.check.job.CallingToSendJob;
//import com.br.marketing.check.job.juzi.OrangeTransferCyclicalPushDassJob;
//import com.br.marketing.check.service.OrangePushDassService;
//import com.br.marketing.common.utils.DateHelper;
//import com.br.marketing.entity.*;
//import com.br.marketing.mapper.MarketingSyncUserMapper;
//import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
//import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
//import com.br.marketing.mapper.TransferActionFrontMapper;
//import com.br.marketing.service.Impl.transfertofile.TransferToFileByYiXinRealTimeServiceImpl;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import javax.annotation.Resource;
//import java.text.NumberFormat;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = {CkeckApplication.class})
//public class mySimpleTest {
//
//    @Autowired
//    private CallingToSendJob callingToSendJob;
//    @Resource
//    private MarketingSyncUserMapper marketingSyncUserMapper;
//
//    @Test
//    public void replace() {
//        String str = "orgName不能为空,name不能为空";
//        String name = str.replace("name", "");
//        String s = str.replaceFirst("^(name不能为空)$", "");
//        String s1 = str.replaceFirst("^(orgName不能为空)$", "");
//        System.out.println(name);
//        System.out.println(s);
//        System.out.println(s1);
//    }
//
//
//    @Test
//    public void fortest() {
//        out:
//        for (int i = 0; i < 100; i++) {
//            System.out.println("输出:" + i);
//            if (i == 10) {
//                break out;
//            }
//        }
//        System.out.println("结束");
//
//        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
//        outtwo:
//        for (Integer integer : integers) {
//            System.out.println("输出:" + integer);
//            if (integer == 10) {
//                break outtwo;
//            }
//        }
//        System.out.println("结束two");
//    }
//
//    @Resource
//    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
//
//    @Resource
//    private TransferToFileByYiXinRealTimeServiceImpl transferToFileByYiXinRealTimeService;
//
//    @Test
//    public void test() throws InterruptedException {
//        Map<String, Set<String>> map = new HashMap<>();
//        Set<String> set1 = new HashSet<>();
//        set1.add("2022-01-11");
//        Set<String> set2 = new HashSet<>();
//        map.put("8", set1);
//        set2.add("2022-01-27");
//        map.put("13", set2);
//        // TODO: 2022/9/22 测试数据需要删除
//        TransferFileTask task = new TransferFileTask();
//        task.setApiCode("7410787");
//        task.setFileChildDir("data_yixin_deny");
//        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
//        task.setStartDate(yyyyMMdd);
////        transferToFileByYiXinRealTimeService.actionTransferToFileDenyDataHandle(task);
//        System.out.println(NumberFormat.getNumberInstance().format(100000.123458));
//        TimeUnit.MINUTES.sleep(1);
//    }
//
//    @Resource
//    private OrangePushDassService orangePushDassService;
//    @Resource
//    private TransferActionFrontMapper transferActionFrontMapper;
//    @Resource
//    private OrangeTransferCyclicalPushDassJob orangeTransferCyclicalPushDassJob;
//    @Resource
//    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
//
//    @Test
//    public void dbTist() {
//                orangePushDassService.transferCyclicalPushDass("7410821");
//    }
//}
