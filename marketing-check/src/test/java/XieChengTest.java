import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.check.CkeckApplication;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.xiecheng.FinanceAESUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.tc.TcDataPushDto;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapper;
import com.br.marketing.retry.DatabaseOperationService;
import com.br.marketing.service.Impl.transfertofile.*;
import com.br.marketing.service.Impl.xc.XieChengReportService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * xiechengTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {CkeckApplication.class})
@WebAppConfiguration
public class XieChengTest {
    protected final static Logger log = LoggerFactory.getLogger(XieChengTest.class);

    @Autowired
    SyncConfigService syncConfigService;

    @Autowired
    HttpProxyClient httpProxyClient;

    @Resource
    TransferToFileByXieChengServiceImpl TransferToFileByXieChengServiceImpl;

    @Resource
    XieChengReportService xieChengReportService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String brPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCGnJwI+EI96Lb7+33AiUug3g7aZTr9gpkLjM3w9Gu3PaSigsF8DNaugV8cMAPJfi9QGZ3t5qGGwLW/N5AFknedZvyGzOEmwk1ezimPtYH0ToEz1OKID0uriFGqrF7lzE7l/rsvpRv6TU07ztg1eDSckGZwyHSDgQD7E5HkqHt1wdpW+aqR5y3xtg9viYfI+0BBgduthJ9mPrX1l/26MKvZIeXAxGm84Fvs/LA7nJqJi64YhYx9jbhVPgHwsE057H33Vi5UZUyseM1cZc2QfqtWVJHfJW06b5ZW73MVSK3MxdNZX6dgT9bkHfxzeFOM0BNJm4n6Ykhcgg8sRMUAvDjnAgMBAAECggEAHxKXkhp8b/3//zqWVJNcuc2IcDFd5Jb47QmboDtLggjgsAKu1wu";

    private static final String CODETYPE = "MOBILE";
    private static final String MARKETTYPE = "SMS";
    private static final Boolean MARKETFINANCEUSER = false;


    static {
        // 配置ObjectMapper
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // 不序列化null值
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false); // 日期不转为时间戳
        objectMapper.registerModule(new JavaTimeModule()); // 支持Java 8日期类型
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); // 允许序列化空对象
    }

    @Test
    public void testActionTransferToFile(){
        TransferFileTask transferFileTask = new TransferFileTask();
        transferFileTask.setApiCode("3710058");
        transferFileTask.setFileType(2);
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern(DateHelper.SHORT_DATE_FORMAT));
        transferFileTask.setStartDate(yyyyMMdd);
        log.warn("携程转化数据提取-开始写入文件,apiCode ={}", transferFileTask.getApiCode());
        // TransferToFileByXieChengServiceImpl.actionTransferToFile(transferFileTask,"");
    }

    @Resource
    private DatabaseOperationService dbService;

    @Resource
    private XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;

    @Test
    public void test06() {
//        xieChengReportService.pushXieChengData(148057l);
    }

    @Test
    public void test04() {

        try {
            DatabaseOperationService.RetryConfig config = DatabaseOperationService.RetryConfig.builder().build();
            dbService.executeWithRetry(new DatabaseOperationService.SqlOperation() {
                @Override
                public void execute() {
                    XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
                    taskExample.createCriteria().andIdEqualTo(1L);
                    List<XiechengCollidingDataProcessTask> xiechengCollidingDataProcessTasks = xiechengCollidingDataProcessTaskMapper.selectByExample(taskExample);
                }

                @Override
                public Object getParams() {
                    XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
                    taskExample.createCriteria().andIdEqualTo(1L);
                    return taskExample;
                }

                @Override
                public String getMapperClass() {
                    return "com.br.marketing.mapper.XiechengCollidingDataProcessTaskMapperBase";
                }

                @Override
                public String getMapperMethod() {
                    return "selectByExample";
                }
            },"",config);
        } catch (Exception e) {

        }
    }

    @Test
    public void test05() {
        TcDataPushDto tcDataPushDto = new TcDataPushDto();
        tcDataPushDto.setBatchNo("123");
        tcDataPushDto.setFileUrl("https://oss.17usoft.com/public-nova/xz8KKPBV-AgencyCP20250411000000000041044.gz");
        tcDataPushDto.setFileExpirationTime("2025-03-14 13:58:47");
        tcDataPushDto.setStartDate("2025-03-14");
        tcDataPushDto.setEndDate("2025-04-14");
        tcDataPushDto.setTotal(100l);
        String data;
        try {
            data = objectMapper.writeValueAsString(tcDataPushDto);
        } catch (Exception e) {
            log.error("对象转JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("对象转JSON失败", e);
        }
        TcRequestDTO tcRequestDTO = new TcRequestDTO();
        tcRequestDTO.setRequestNo("1234");
        tcRequestDTO.setTimestamp(String.valueOf(System.currentTimeMillis()));
        tcRequestDTO.setData(data);
        Map<String, Object> convert = objectMapper.convertValue(tcRequestDTO, Map.class);
        String signature = RSAUtil.generateContent(convert);
        String sign = RSAUtil.signByPrivateKey(brPrivateKey, signature);
        tcRequestDTO.setSign(sign);


    }

    /**
     * 触达上报
     */
    @Test
    public void pushXieChengData02() {

        /**
         * data 组装
         */
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("sha256Tel", "0a16fd689248cf58e939d6eb56d0df495910a9ec84d767debc70f6c42c5297bc");
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        ThirdAdOuterReq thirdAdOuterReq = null;
        String aid = "test002";
        String aesKey = "6b219b9e71fdb2d2";
        String ivKey = "5e7b6c07d3edbc01";
        String sKey = "b8845b4cb1f16471";
        String extendSource = "CPS_TEST";
//            try {
//                JSONObject extend = JSONObject.parseObject(xieChengData.getExtend());
//                String sourceStr = extend.getString("source");
//                if (StringUtils.isEmpty(sourceStr)) {
//                    log.warn("携程广告上报接口，source为空:{}，置为默认值:{}", sourceStr, "BaiRong_C01");
//                } else {
//                    extendSource = sourceStr;
//                }
//            } catch (Exception e) {
//                log.error("携程广告上报接口，source字段解析异常:{}", xieChengData.getExtend(), e);
//            }

        thirdAdOuterReq = new ThirdAdOuterReq(
                timestemp,
                extendSource,
                System.currentTimeMillis() + getCode(5) + "0a16fd689248cf58e939d6eb56d0df495910a9ec84d767debc70f6c42c5297bc",
                "IVR",
                deviceInfo.toString()
        );

        thirdAdOuterReq.setMktMode("CPS");



        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", aid);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", "commonOutAdMonitor");
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq), aesKey , ivKey));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, sKey));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, "https://ad-test.fat.ctripqa.com/ad/common/outAdMonitor.do", false, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(thirdAdOuterReq), true, false);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程广告上报接口发送参数:ThirdAdOuterReq={} para={}", JSON.toJSONString(thirdAdOuterReq), JSON.toJSONString(retMap));
//            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        String content = resMap.get("content");

        /*范围40到70的随机数*/
//        int random = (int) (Math.random() * (70 - 40 + 1) + 40);
//        ThreadUtil.sleep(70);
//        String content = "{\"code\":0,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"760a06d2bc9b150d1d5b162e95bed32ed306cd1c2f7417c5e10397715ea165c1\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";

        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");

    }

    /**
     * 拨打上报 生产环境
     */
    @Test
    public void pushXieChengData03() {

        /**
         * data 组装
         */
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("sha256Tel", "81329e4f587a7aed5655f3ea497f055b05bf79d8680c136ded0ccf219a0f73e9");
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        ThirdAdOuterReq thirdAdOuterReq = null;
        String aid = "test002";
        String aesKey = "f3df6f62f0527bf0";
        String ivKey = "3b2dac323465b024";
        String sKey = "95cc01ec07387a44";
        String extendSource = "CPS_TEST";
//            try {
//                JSONObject extend = JSONObject.parseObject(xieChengData.getExtend());
//                String sourceStr = extend.getString("source");
//                if (StringUtils.isEmpty(sourceStr)) {
//                    log.warn("携程广告上报接口，source为空:{}，置为默认值:{}", sourceStr, "BaiRong_C01");
//                } else {
//                    extendSource = sourceStr;
//                }
//            } catch (Exception e) {
//                log.error("携程广告上报接口，source字段解析异常:{}", xieChengData.getExtend(), e);
//            }

        thirdAdOuterReq = new ThirdAdOuterReq(
                timestemp,
                "BaiRong_SME01",
                System.currentTimeMillis() + getCode(5) + "81329e4f587a7aed5655f3ea497f055b05bf79d8680c136ded0ccf219a0f73e9",
                "IVR",
                deviceInfo.toString(),
                "CPA",
                "CTRIP",
                "SME",
                "bairong001"
        );




        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", "bairong001");
        retMap.put("timestamp", timestemp);
        retMap.put("channel", "commonOutAdMonitor");
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq), aesKey , ivKey));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, sKey));
        HashMap<String, String> resMap = httpProxyClient.sendByCodeWithLog(retMap, "https://jr-ad.ctrip.com/ad/common/outAdMonitor.do", false, MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(thirdAdOuterReq), true, false);
        if (!"200".equals(resMap.get("httpcode")) || StringUtils.isBlank(resMap.get("content"))) {
            log.error("携程广告上报接口发送参数:ThirdAdOuterReq={} para={}", JSON.toJSONString(thirdAdOuterReq), JSON.toJSONString(retMap));
//            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        String content = resMap.get("content");

        /*范围40到70的随机数*/
//        int random = (int) (Math.random() * (70 - 40 + 1) + 40);
//        ThreadUtil.sleep(70);
//        String content = "{\"code\":0,\"msg\":\"测试效率\",\"data\":[{\"md5Code\":null,\"sha256Code\":\"760a06d2bc9b150d1d5b162e95bed32ed306cd1c2f7417c5e10397715ea165c1\",\"result\":false,\"orgChannel\":\"测试orgChannel\",\"mktLevel\":\"测试orgmktLevel\",\"info\":\"测试info\"}]}";

        JSONObject resultJson = JSONObject.parseObject(content);
        Integer code = resultJson.getInteger("code");

    }

//    @Test
//    public void pushXieChengReport() {
//
//        long l = System.currentTimeMillis();
//
//        ThirdAdOuterReq thirdAdOuterReq = new ThirdAdOuterReq(
//                String.valueOf(l / 1000),
//                "CPS_TEST",
//                l + getCode(5) + "0a16fd689248cf58e939d6eb56d0df495910a9ec84d767debc70f6c42c5297bc",
//                xieChengData.getActionType(),
//                deviceInfo.toString(),
//                config.getString("mktMode"),
//                xieChengData.getMktChannel(),
//                config.getString("mktProductNo"),
//                config.getString("appId")
//        );
//
//        // 5. 准备请求参数
//        Map<String, Object> retMap = new HashMap<>();
//        retMap.put("appId", config.getString("appId"));
//        retMap.put("timestamp", timestamp);
//        retMap.put("channel", config.getString("channel"));
//        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(thirdAdOuterReq),
//                config.getString("aesKey"), config.getString("iv")));
//        retMap.put("sign", FinanceAESUtils.signLocal(retMap, config.getString("singKey")));
//
//        // 6. 发送请求
//        Map<String, String> resMap = httpProxyClient.sendByCodeWithLog(
//                retMap,
//                config.getString("url"),
//                isProxy,
//                MediaType.APPLICATION_JSON_UTF8_VALUE,
//                JSON.toJSONString(thirdAdOuterReq),
//                true,
//                false
//        );
//
//    }

    /**
     * 撞库
     */
    @Test
    public void testColliding() {
        List<String> sha256CodeList = Lists.newArrayList();
        sha256CodeList.add("fa3e0a97327269b7c33a1b8a45b41e4d9d6e3a1149a5233db3945cf5cc34bc6f");
//        pushXieChengSmsCollidingDataNew(sha256CodeList);
    }


    @Test
    public void pushXieChengSmsCollidingDataNew() {
        List<String> sha256CodeList = Lists.newArrayList();
        sha256CodeList.add("0a16fd689248cf58e939d6eb56d0df495910a9ec84d767debc70f6c42c5297bc");
//        String smsCollidingOpenUrl = collidingConfig.getString("smsCollidingOpenUrl");
        String smsCollidingOpenUrl = "https://adtest.fat.qatetrip.com/ad/common/unionCheckUser.do";
//        String smsCollidingAppId = collidingConfig.getString("smsCollidingAppId");
        String smsCollidingAppId = "test002";
//        String smsCollidingKey = collidingConfig.getString("smsCollidingKey");
        String smsCollidingKey = "6b219b9e71fdb2d2";
//        String smsCollidingIv = collidingConfig.getString("smsCollidingIv");
        String smsCollidingIv = "5e7b6c07d3edbc01";
//        String smsCollidingSingKey = collidingConfig.getString("smsCollidingSingKey");
        String smsCollidingSingKey = "b8845b4cb1f16471";
        String smsCollidingChannel = "commonUnionCheckUser";
        Boolean smsCollidingIsProxy = false;
        /**
         * data 组装
         */
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                smsCollidingAppId, sha256CodeList, CODETYPE, MARKETTYPE, MARKETFINANCEUSER
        );
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsCollidingAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsCollidingChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), smsCollidingKey, smsCollidingIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsCollidingSingKey));
        HashMap<String, String> resMap;
        resMap = httpProxyClient.sendByCodeWithLog(retMap, smsCollidingOpenUrl, smsCollidingIsProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(xieChengSmsCollidingReq), true, false);

    }

    /**
     * 撞库生产
     */
    @Test
    public void pushXieChengSmsCollidingDataNewSC() {
        List<String> sha256CodeList = Lists.newArrayList();
        sha256CodeList.add("0a16fd689248cf58e939d6eb56d0df495910a9ec84d767debc70f6c42c5297bc");
//        String smsCollidingOpenUrl = collidingConfig.getString("smsCollidingOpenUrl");
        String smsCollidingOpenUrl = "https://jr-ad.ctrip.com/ad/common/unionCheckUser.do";
//        String smsCollidingAppId = collidingConfig.getString("smsCollidingAppId");
        String smsCollidingAppId = "bairong001";
//        String smsCollidingKey = collidingConfig.getString("smsCollidingKey");
        String smsCollidingKey = "f3df6f62f0527bf0";
//        String smsCollidingIv = collidingConfig.getString("smsCollidingIv");
        String smsCollidingIv = "3b2dac323465b024";
//        String smsCollidingSingKey = collidingConfig.getString("smsCollidingSingKey");
        String smsCollidingSingKey = "95cc01ec07387a44";
        String smsCollidingChannel = "commonUnionCheckUser";
        Boolean smsCollidingIsProxy = false;
        /**
         * data 组装
         */
        XieChengSmsCollidingReq xieChengSmsCollidingReq = new XieChengSmsCollidingReq(
                smsCollidingAppId, sha256CodeList, CODETYPE, MARKETTYPE, MARKETFINANCEUSER
        );
        String timestemp = String.valueOf(System.currentTimeMillis() / 1000);
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("appId", smsCollidingAppId);
        retMap.put("timestamp", timestemp);
        retMap.put("channel", smsCollidingChannel);
        retMap.put("data", FinanceAESUtils.encryptStr(JSON.toJSONString(xieChengSmsCollidingReq), smsCollidingKey, smsCollidingIv));
        retMap.put("sign", FinanceAESUtils.signLocal(retMap, smsCollidingSingKey));
        HashMap<String, String> resMap;
        resMap = httpProxyClient.sendByCodeWithLog(retMap, smsCollidingOpenUrl, smsCollidingIsProxy,
                MediaType.APPLICATION_JSON_UTF8_VALUE, JSON.toJSONString(xieChengSmsCollidingReq), true, false);

    }

    public static String getCode(int n) {
        char arr[] = new char[n];
        int i = 0;
        while (i < n) {
            char ch = (char) (int) (Math.random() * 124);
            if (ch >= 'a' && ch <= 'z' || ch >= '0' && ch <= '9') {
                arr[i++] = ch;
            }
        }
        //将数组转为字符串
        return new String(arr);
    }

}
