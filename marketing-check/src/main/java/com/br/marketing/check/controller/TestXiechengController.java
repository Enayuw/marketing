package com.br.marketing.check.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.check.service.OriginPeriodPredicateService;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.xiecheng.FinanceAESUtils;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.MarketingSmyPushService;
import com.br.marketing.service.PushDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.check.controller
 * @Description:
 * @CreateTime: 2022-07-18 17 :09
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@RestController
@RequestMapping("/test/")
@Slf4j
public class TestXiechengController {


    @Autowired
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;
    @Resource
    MarketingSmyPushService marketingSmyPushService;

    private final static String XIECHENGSMSCOLLIDING = "xiechengsmscolliding";

    @Resource
    private LocalFileMapper localFileMapper;
    @Autowired
    private HttpProxyClient httpProxyClient;
    @Autowired
    PushDataService pushDataService;

    @Autowired
    XieChengSmsCollidingDataMapper xieChengSmsCollidingDataMapper;
    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Autowired
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Autowired
    private List<OriginPeriodPredicateService> juZiPeriodPredicateServiceList;





    @Autowired
    private DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    @GetMapping("/test")
    public void transfersmyTest(String id) {


        //pushDataService.pushXieChengSmsCollidingToDbData(id);
//        LocalFileExample localFileExample = new LocalFileExample();
//        localFileExample.createCriteria()
//                .andFileTypeEqualTo(XIECHENGSMSCOLLIDING)
//                .andStatusEqualTo("1");
//        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
//
//        for(int i=0;i<localFileList.size();i++){
//            LocalFile localFile = localFileList.get(i);
//            if(localFile.getId()==942437){
//                com.alibaba.fastjson.JSONObject msg = new JSONObject();
//                msg.put("localId", localFile.getId());
//                msg.put("type", 2);
////            producter.send("Marketing.Universal.SftpToDb.XieChengSmsCollidingReceive", msg.toJSONString());
//                pushDataService.pushXieChengSmsCollidingToDbData(msg.toJSONString());
//            }
//        }
//        localFileList.stream().forEach((localFile) -> {
//
//
//        });

//        LocalFileExample localFileExample = new LocalFileExample();
//        localFileExample.createCriteria()
//                .andFileTypeEqualTo(XIECHENGSMSCOLLIDING)
//                .andStatusEqualTo("1")
//                .andPushStatusNotEqualTo("1");
//        List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);
//
//
//        localFileList.stream().forEach((localFile) -> {
//            Date createTime = localFile.getCreateTime();
//            if (differentDaysByMillisecond(createTime, new Date(), 15 * 24)) {
////                producter.send("Marketing.Universal.SftpToDb.XieChengSmsCollidingReceive", String.valueOf(localFile.getId()));
//                System.out.println(localFile);
//            }
//        });
    }
//    @GetMapping("/test1")
//    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
//        // 创建线程池
////        ThreadPoolExecutor xieChengSmsCollidingThread = BrExecutors.getThreadPool(marketingCommonConfig.getXieChengSmsCollidingThread(), marketingCommonConfig.getXieChengSmsCollidingThread());
//        ThreadPoolExecutor xieChengSmsCollidingThread = BrExecutors.getThreadPool(5,5);
//
//        Boolean actionMark = true;
//        Date endTime = getTimeDay(15);
//        long startTime = endTime.getTime() - (60 * 60 * 1000);
//        // 根据id 进行数据查询 每批次查询 1.5w
//        Long minId = null;
//        AtomicInteger failNum = new AtomicInteger(0);
//        while (actionMark) {
//            List<XieChengSmsCollidingData> xieChengSmsCollidingDataList = xieChengSmsCollidingDataMapper.selectByLocalId(minId,new Date(startTime),);
//            if (xieChengSmsCollidingDataList.size() == 0) {
//                actionMark = false;
//                continue;
//            }
//            // 更新minId 为当前集合最大的id
//            minId = xieChengSmsCollidingDataList.get(xieChengSmsCollidingDataList.size() - 1).getId();
//            // 将查询出来的明细数据进行分组，每组50个数据
//            List<List<XieChengSmsCollidingData>> xieChengSmsCollidingDataPartitions = Lists.partition(xieChengSmsCollidingDataList, 50);
//            for (int i = 0; i < xieChengSmsCollidingDataPartitions.size(); i++) {
//                List<XieChengSmsCollidingData> xieChengSmsCollidingDataListPartition = xieChengSmsCollidingDataPartitions.get(i);
//                xieChengSmsCollidingThread.submit(() -> pushDataService.pushXieChengSmsCollidingData(xieChengSmsCollidingDataListPartition, failNum));
//            }
//        }
//        xieChengSmsCollidingThread.shutdown();
//        try {
//            while (!xieChengSmsCollidingThread.awaitTermination(10L, TimeUnit.SECONDS)) {
//            }
//        } catch (Exception ex) {
//            log.error(ex.getMessage(), ex);
//        }
//    }

    public static Date getTimeDay(int index) {
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        calendar.add(Calendar.DAY_OF_MONTH, -index);
        return  calendar.getTime();
    }
    /**
     * 通过时间秒毫秒数判断两个时间的间隔
     *
     * @param date1
     * @param date2
     * @return
     */

    public static boolean differentDaysByMillisecond(Date date1, Date date2, int hours) {
        int days = ((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600)));
        return days / hours > 0 && days % hours == 0;
    }
    @GetMapping("/resultVolumeCheck")
    public void process() {
        PhoneSaleExtendInfoExample updateExample = new PhoneSaleExtendInfoExample();
        List<Long> ids = new ArrayList<>();
        ids.add(690057L);
        updateExample.createCriteria().andIdIn(ids);
        PhoneSaleExtendInfo updateEntity = new PhoneSaleExtendInfo();
        updateEntity.setPStatus(2);
        phoneSaleExtendInfoMapper.updateByExampleSelective(updateEntity, updateExample);
    }

    @GetMapping("testSms")
    public void test() throws JSONException {
        /**
         * data 组装
         * 域名：http://cgcallback-fat.ctripqa.com/nemoweb/ad/common/unionCheckUser.do
         *
         * channel：commonUnionCheckUser
         * appId：bairong001
         * signKey：95cc01ec07387a44
         * aesKey：f3df6f62f0527bf0
         * aesIv：3b2dac323465b024
         */

        String url = "https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/unionCheckUser.do";

        String appId = "bairong001";
        String key = "f3df6f62f0527bf0";
        String iv = "3b2dac323465b024";
        String singKey = "95cc01ec07387a44";

        String codeType = "MOBILE";
        String marketType = "SMS";
        Boolean marketFinanceUser = false;
        List<String> sha256CodeLists = new ArrayList<>();
//        e92920aec3c7eac4d8b74f3a46f5fd06eef6a193ceebd4826d2674fd3a9b271e
//EA0AA5AA5CB418AE71F87AF3D28390002EDD53BCD87EB3EF482EE80D6A75BE79
        sha256CodeLists.add("e92920aec3c7eac4d8b74f3a46f5fd06eef6a193ceebd4826d2674fd3a9b271e");
        sha256CodeLists.add("EA0AA5AA5CB418AE71F87AF3D28390002EDD53BCD87EB3EF482EE80D6A75BE79".toLowerCase());

        String channel = "commonUnionCheckUser";

        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                appId, sha256CodeLists, codeType, marketType, marketFinanceUser
        );
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", appId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", channel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), key, iv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, singKey));
        System.out.println(retMap);
        String send = httpProxyClient.send(JSON.toJSONString(retMap), url, false);
        //https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/outAdMonitor.do
        System.out.println(JSON.toJSONString(send));
    }



    public static void main(String[] args) throws ParseException {
        String timeDay = getTimeDay("yyyy-MM-dd HH:mm:ss", 2);
        System.out.println(timeDay);
    }
    public static String getTimeDay(String simpleDateFormat, int index) throws ParseException {
        Calendar calendar = Calendar.getInstance (); // 创建 Calendar 的实例
        calendar.add (Calendar.DAY_OF_MONTH,-1); // 当前时间减去一天，即一天前的时间
        calendar.getTimeInMillis ();// 返回当前时间的毫秒数
        SimpleDateFormat fmt = new SimpleDateFormat(simpleDateFormat);
        calendar.add(Calendar.DAY_OF_MONTH, index);
        String date = fmt.format(calendar.getTimeInMillis());
        return date;
    }
    //public static void main(String[] args) throws JSONException {
    //    /**
    //     * data 组装
    //     */
    //
    //    //String url = "https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/outAdMonitor.do";
    //
    //    String appId = "bairong001";
    //    String key = "f3df6f62f0527bf0";
    //    String iv = "3b2dac323465b024";
    //    String singKey = "95cc01ec07387a44";
    //
    //    String source = "BaiRong_C01";
    //    String actionType = "SMS";
    //    String channel = "commonOutAdMonitor";
    //
    //    JSONObject deviceInfo = new JSONObject();
    //    deviceInfo.put("sha256Tel","AEAA638C17D05884717153C3C898A41598E87209AC42E4CFCD844F6E724C33D7");
    //
    //    //String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
    //    String timestemp = "1660283657";
    //    ThirdAdOuterReq thirdAdOuterReq = new ThirdAdOuterReq(timestemp,source,"202208110117312",actionType,deviceInfo.toString());
    //
    //
    //    Map<String,Object> retMap = Maps.newHashMap();
    //    retMap.put("appId",appId);
    //    retMap.put("timestamp",timestemp);
    //    retMap.put("channel",channel);
    //    retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq),key,iv));
    //    retMap.put("sign",FinanceAESUtils.signLocal(retMap,singKey));
    //    System.out.println(retMap);
    //    //https://cgcallback-fat.ctripqa.com/nemoweb/ad/common/outAdMonitor.do
    //    System.out.println(JSON.toJSONString(retMap));
    //}
}
