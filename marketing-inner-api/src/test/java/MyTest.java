
import java.math.BigDecimal;

import com.br.marketing.dto.DataDetailTestDTO;
import com.google.common.collect.Lists;
import com.alibaba.fastjson.JSON;
import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
public class MyTest {

    final static SimpleDateFormat yyyyMMddHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Test
    public void test001() {
        String batch_no = com.br.marketing.common.utils.StringUtils.humpToLine2("batch_no");
    }

    @Test
    public void yu(){

        int offset = 0;
        System.out.println("offset="+offset+" h:"+getIndex(0,offset));
        System.out.println("offset="+offset+" i:"+getIndex(1,offset));
        System.out.println("offset="+offset+" j:"+getIndex(2,offset));

        offset = 1;
        System.out.println("offset="+offset+" h:"+getIndex(0,offset));
        System.out.println("offset="+offset+" i:"+getIndex(1,offset));
        System.out.println("offset="+offset+" j:"+getIndex(2,offset));

        offset = -1;
        System.out.println("offset="+offset+" h:"+getIndex(0,offset));
        System.out.println("offset="+offset+" i:"+getIndex(1,offset));
        System.out.println("offset="+offset+" j:"+getIndex(2,offset));

        Long dxCode = Long.valueOf("YiXinTransferToDxJob".hashCode());
        Long aiCode = Long.valueOf("YiXinTransferToRobotAIJob".hashCode());
        Long jueCode = Long.valueOf("YiXinTransferToJueCeJob".hashCode());

        System.out.println("dxCode="+dxCode);
        System.out.println("aiCode="+aiCode);
        System.out.println("jueCode="+jueCode);

    }

    int getIndex(int start,int offset){
        return (start+offset) % 3;
    }

    @Test
    public void testhashcode(){
        ArrayList<DataDetailTestDTO> dataDetailTestDTOS = new ArrayList<>();

        DataDetailTestDTO log = new DataDetailTestDTO();
        log.setName("7410437");

        DataDetailTestDTO log1 = new DataDetailTestDTO();
        log1.setName("7410437");

        DataDetailTestDTO log2 = new DataDetailTestDTO();
        log2.setName("7410437");

        DataDetailTestDTO log3 = new DataDetailTestDTO();
        log3.setName("7410437");

        String s = DigestUtils.md5DigestAsHex((log.toString() + log.hashCode()).getBytes());
        System.out.println("s:"+s);
        System.out.println("loghascode:"+log.hashCode());
        System.out.println("log1hascode:"+log1.hashCode());
        String s1 = DigestUtils.md5DigestAsHex((log.toString() + log.hashCode()).getBytes());
        System.out.println("s1:"+s1);
        dataDetailTestDTOS.add(log);
        dataDetailTestDTOS.add(log1);
        dataDetailTestDTOS.add(log2);
        dataDetailTestDTOS.add(log3);
        HashMap<String, Object> objectObjectHashMap = new HashMap<>();
        for (DataDetailTestDTO dataDetailTestDTO : dataDetailTestDTOS) {
            objectObjectHashMap.put(DigestUtils.md5DigestAsHex((log.toString()).getBytes())+log.hashCode(),dataDetailTestDTO);
        }
        List abc = dataDetailTestDTOS;
        abc.remove(log);
        System.out.println(dataDetailTestDTOS);
        Iterator<DataDetailTestDTO> iterator = dataDetailTestDTOS.iterator();
        if (iterator.hasNext()) {
            iterator.remove();
        }
    }

    @Test
    public void testIbu(){
        IbuReqDTO ibuReqDTO = new IbuReqDTO();
        ibuReqDTO.setData("[{\"id\":614504,\"phone\":\"14455624387\",\"planId\":5655,\"reserveField1\":\"{\\\"operateType\\\":\\\"0\\\",\\\"tid\\\":\\\"89658\\\"}\",\"source\":\"100\",\"uid\":\"zwistio_2023030708174\",\"userCode\":\"zwistio_2023030708174\",\"userName\":\"1\",\"userType\":\"D\"},{\"id\":614505,\"phone\":\"14455624388\",\"planId\":5655,\"reserveField1\":\"{\\\"operateType\\\":\\\"0\\\",\\\"tid\\\":\\\"89658\\\"}\",\"source\":\"100\",\"uid\":\"zwistio_2023030708175\",\"userCode\":\"zwistio_2023030708175\",\"userName\":\"1\",\"userType\":\"D\"},{\"id\":614506,\"phone\":\"14455624389\",\"planId\":5655,\"reserveField1\":\"{\\\"operateType\\\":\\\"0\\\",\\\"tid\\\":\\\"89658\\\"}\",\"source\":\"100\",\"uid\":\"zwistio_2023030708176\",\"userCode\":\"zwistio_2023030708176\",\"userName\":\"1\",\"userType\":\"D\"}]");
        ibuReqDTO.setAccessKey("d87a6e0ab4dc2903");
        ibuReqDTO.setTs(System.currentTimeMillis());
        StringBuilder mText = new StringBuilder();
        mText.append("fbd1478a51d88954");
        mText.append(ibuReqDTO.getData());
        mText.append(ibuReqDTO.getTs());
        String s = DigestUtils.md5DigestAsHex(mText.toString().getBytes()).toUpperCase();
        ibuReqDTO.setSign(s);
        System.out.println(ibuReqDTO.toString());
    }

    @Test
    public void testThreadSafe() {
        String abc = "2021-08-11 11:00:00";
        for (int i = 0; i < 20; i++) {

            new Thread(() -> {
                try {
                    System.out.println(yyyyMMddHMS.format(yyyyMMddHMS.parse(abc)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @Test
    public void testIbuInterface() {
        List<IbuReqDTO.Datum> list = new ArrayList<>();
        IbuReqDTO.Datum datum = new IbuReqDTO.Datum();

//        datum.setUid("ab123");
//        datum.setUserType("D");
//        datum.setUserCode("ab123");
//        datum.setUserName("1");
//        datum.setPhone("15520342033");
//        datum.setSource("101");
//        datum.setPlanId(123);

        datum.setUid("ab124");
        datum.setUserType("D");
        datum.setPurpose("12");
        datum.setUserCode("ab124");
        datum.setUserName("1");
        datum.setGender("男");
        datum.setPhone("15520342034");
        datum.setSignInTimeStr("2023-02-15 11:00:00");
        datum.setClickProductName("123");
        datum.setClickTimeStr("2023-02-15 11:00:00");
        datum.setRecommendList(Lists.newArrayList());
        datum.setRecommendH5List(Lists.newArrayList());
        datum.setBasicInfo("123");
        datum.setRealName("123");
        datum.setSupplement("123");
        datum.setContract("123");
        datum.setOperator("123");
        datum.setLoanProductName("123");
        datum.setLoanTimeStr("2023-02-15 11:00:00");
        datum.setCreateTimeStr("2023-02-15 11:00:00");
        datum.setDiffAmount("12");
        datum.setFaceRecognition("12");
        datum.setFirstApproveResult("12");
        datum.setFirstApproveTimeStr("2023-02-15 11:00:00");
        datum.setHasBindCard("123");
        datum.setHasEverBorrow("123");
        datum.setHasWithdraw("123");
        datum.setInsteadCommitFlag("123");
        datum.setInsteadCommitPname("123");
        datum.setIsTimely("123");
        datum.setLoanFailedTimeStr("123");
        datum.setLoanSuccessTimeStr("123");
        datum.setLoanWillingness("123");
        datum.setACardScore("123");
        datum.setBucketName("123");
        datum.setOverdueDays("123");
        datum.setPrepayAmount("123");
        datum.setPrepayPname("123");
        datum.setPrepayTimeStr("2023-02-15 11:00:00");
        datum.setRepayPname("123");
        datum.setRepayAmount("123");
        datum.setRepayTimeStr("2023-02-15 11:00:00");
        datum.setSecondApproveResult("123");
        datum.setSecondApproveTimeStr("2023-02-15 11:00:00");
        datum.setApplyAmount("123");
        datum.setApproveAmount("123");
        datum.setSource("100");
        datum.setProdType("234");
        datum.setScore("234");
        datum.setCallTimes("2");
        datum.setCallAccessScore(0);
        datum.setRemark("234");
        datum.setGrade("234");
        datum.setTotalAmount("123");
        datum.setSurplusAmount("123");
        datum.setPid(0);
        datum.setPchannel("123");
        datum.setChannelName("123");
        datum.setMarketPurpose("123");
        datum.setRiskControlLabel("123");
        datum.setFirstLoginTimeStr("123");
        datum.setPlanId(123);
        datum.setGoalsApp("123");
        datum.setFlowSideName("123");
        datum.setFlowSidePath("123");
        datum.setCusTag("123");
        datum.setAbgroupPushOffsetStr("123");
        datum.setExtra1("");
        datum.setExtra2("");
        datum.setExtra3("");
        datum.setReserveField1("{\"tid\":\"123\",\"operateType\":\"1\"}");
        datum.setCreditTimeStr("2023-02-15 11:00:00");
        datum.setCreditChannel("123");
        datum.setAmountStatus("1");
        datum.setConnectTimes(0);
        datum.setZyApplyFlag(false);
        datum.setZyApplySuccessFlag(false);
        datum.setZyAmountStatus("1");
        datum.setZyTotalUsableAmount(new BigDecimal("0"));
        datum.setIsIdnumber("1");
        datum.setIsTaobao("1");
        datum.setIsNuclearapproval("1");
//        datum.setCallaccessscore("1");
        datum.setMarketingScore("1");
        datum.setNoWithdrawOrders("1");
        datum.setPlanData("1");
        datum.setPriorityScore("1");
        datum.setCallType("1");




        IbuReqDTO.Datum datum1 = new IbuReqDTO.Datum();
        datum1.setUid("125");
        datum1.setUserType("D");
        datum1.setPurpose("12");
        datum1.setUserCode("125");
        datum1.setUserName("1");
        datum1.setGender("男");
        datum1.setPhone("15520342055");
//        datum1.setSignInTimeStr("2023-02-15 11:00:00");
        datum1.setClickProductName("123");
//        datum1.setClickTimeStr("2023-02-15 11:00:00");
        datum1.setRecommendList(Lists.newArrayList());
        datum1.setRecommendH5List(Lists.newArrayList());
        datum1.setBasicInfo("123");
        datum1.setRealName("123");
        datum1.setSupplement("123");
        datum1.setContract("123");
        datum1.setOperator("123");
        datum1.setLoanProductName("123");
//        datum1.setLoanTimeStr("2023-02-15 11:00:00");
//        datum1.setCreateTimeStr("2023-02-15 11:00:00");
        datum1.setDiffAmount("12");
        datum1.setFaceRecognition("12");
        datum1.setFirstApproveResult("12");
//        datum1.setFirstApproveTimeStr("2023-02-15 11:00:00");
        datum1.setHasBindCard("123");
        datum1.setHasEverBorrow("123");
        datum1.setHasWithdraw("123");
        datum1.setInsteadCommitFlag("123");
        datum1.setInsteadCommitPname("123");
        datum1.setIsTimely("123");
        datum1.setLoanFailedTimeStr("123");
        datum1.setLoanSuccessTimeStr("123");
        datum1.setLoanWillingness("123");
        datum1.setACardScore("123");
        datum1.setBucketName("123");
        datum1.setOverdueDays("123");
        datum1.setPrepayAmount("123");
        datum1.setPrepayPname("123");
//        datum1.setPrepayTimeStr("2023-02-15 11:00:00");
        datum1.setRepayPname("123");
        datum1.setRepayAmount("123");
//        datum1.setRepayTimeStr("2023-02-15 11:00:00");
        datum1.setSecondApproveResult("123");
//        datum1.setSecondApproveTimeStr("2023-02-15 11:00:00");
        datum1.setApplyAmount("123");
        datum1.setApproveAmount("123");
        datum1.setSource("100");
        datum1.setProdType("234");
        datum1.setScore("234");
        datum1.setCallTimes("2");
        datum1.setCallAccessScore(0);
        datum1.setRemark("234");
        datum1.setGrade("234");
        datum1.setTotalAmount("123");
        datum1.setSurplusAmount("123");
        datum1.setPid(0);
        datum1.setPchannel("123");
        datum1.setChannelName("123");
        datum1.setMarketPurpose("123");
        datum1.setRiskControlLabel("123");
        datum1.setFirstLoginTimeStr("123");
        datum1.setPlanId(123);
        datum1.setGoalsApp("123");
        datum1.setFlowSideName("123");
        datum1.setFlowSidePath("123");
        datum1.setCusTag("123");
        datum1.setAbgroupPushOffsetStr("123");
        datum1.setExtra1("");
        datum1.setExtra2("");
        datum1.setExtra3("");
        datum1.setReserveField1("{\"tid\":\"123\",\"operateType\":\"1\"}");
//        datum1.setCreditTimeStr("2023-02-15 11:00:00");
        datum1.setCreditChannel("123");
        datum1.setAmountStatus("1");
        datum1.setConnectTimes(0);
        datum1.setZyApplyFlag(false);
        datum1.setZyApplySuccessFlag(false);
        datum1.setZyAmountStatus("1");
        datum1.setZyTotalUsableAmount(new BigDecimal("0"));
        datum1.setIsIdnumber("1");
        datum1.setIsTaobao("1");
        datum1.setIsNuclearapproval("1");
//        datum1.setCallaccessscore("1");
        datum1.setMarketingScore("1");
        datum1.setNoWithdrawOrders("1");
        datum1.setPlanData("1");
        datum1.setPriorityScore("1");
        datum1.setCallType("1");
        list.add(datum);
//        list.add(datum1);

        pushIbuArtificial(list, true);
    }

    @Test
    public void testDay(){
        long l = LocalDate.parse("2023-04-30").toEpochDay() - LocalDate.parse("2023-02-02").toEpochDay();
        System.out.println(l);
    }

    public Result pushIbuArtificial(List<IbuReqDTO.Datum> datumList, Boolean mock) {
        UUID reqId = UUID.randomUUID();
        try {
            Result<String> res = new Result<>();
            IbuReqDTO ibuReqDTO = new IbuReqDTO();
            ibuReqDTO.setData(JSON.toJSONString(datumList));
            ibuReqDTO.setAccessKey("d87a6e0ab4dc2903");
            ibuReqDTO.setTs(System.currentTimeMillis());
            StringBuilder mText = new StringBuilder();
            mText.append("fbd1478a51d88954");
            mText.append(ibuReqDTO.getData());
            mText.append(ibuReqDTO.getTs());
            System.out.println("待加密:"+mText);
            String s = DigestUtils.md5DigestAsHex(mText.toString().getBytes()).toUpperCase();
            ibuReqDTO.setSign(s);
            StringBuilder paramStr = new StringBuilder();
            BeanMap beanMap = BeanMap.create(ibuReqDTO);

            String urlencode = urlencode(beanMap, "");
            System.out.println("请求：" + urlencode);
            System.out.println("请求decode：" + URLDecoder.decode(urlencode,"UTF-8"));
//            for (Object o : beanMap.keySet()) {
//                if(beanMap.get(o) instanceof List){
//                    List<Object> _params = (List<Object>) beanMap.get(o);
//                    for(Integer i = 0; i < _params.size(); i++) {
//                        String k = key.isEmpty() ? i.toString() : (key +"["+ i.toString() +"]");
//                        String encodeValue = urlencode(_params.get(i), k);
//                        if(!encodeValue.isEmpty()) {
//                            res += '&'+ encodeValue;
//                        }
//                    }
//                }else{
//                    paramStr.append(String.format("%s=%s&",o.toString(), URLEncoder.encode(beanMap.get(o).toString(),"utf-8")));
//                }
//
//            }
            System.out.println(paramStr.toString());
            if (mock) {
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            }
            return res;
        } catch (Exception ex) {
            log.error("ibu定制接口错误(" + reqId.toString() + ")" + ex.getMessage(), ex);
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage(ex.getMessage());
        }

    }

    public static String urlencode(Object params, String key) {
        String res = "";
        if (params == null) {
            return "";
        } else if (params instanceof Map) {
            Map<String, Object> _params = (Map<String, Object>) params;
            for (String i : _params.keySet()) {
                String k = key.isEmpty() ? i : (key + "[" + i + "]");
                String encodeValue = urlencode(_params.get(i), k);
                if (!encodeValue.isEmpty()) {
                    res += '&' + encodeValue;
                }
            }
        } else if (params instanceof List) {
            List<Object> _params = (List<Object>) params;
            for (Integer i = 0; i < _params.size(); i++) {
                String k = key.isEmpty() ? i.toString() : (key + "[" + i.toString() + "]");
                boolean typeBoolean = params.getClass().isArray()
                        || params instanceof String || params instanceof Map || params instanceof Number;
                String encodeValue = urlencode(typeBoolean ? _params.get(i) : BeanMap.create(_params.get(i)), k);
                if (!encodeValue.isEmpty()) {
                    res += '&' + encodeValue;
                }
            }
        } else if (params.getClass().isArray()) {
            Object[] _params;
            if (params instanceof Object[]) {
                _params = (Object[]) params;
            } else if (params instanceof String[]) {
                _params = (String[]) params;
            } else if (params instanceof int[]) {
                _params = ArrayUtils.toObject((int[]) params);
            } else if (params instanceof double[]) {
                _params = ArrayUtils.toObject((double[]) params);
            } else {
                _params = new Object[]{};
            }
            for (Integer i = 0; i < _params.length; i++) {
                String k = key.isEmpty() ? i.toString() : (key + "[" + i.toString() + "]");
                String encodeValue = urlencode(_params[i], k);
                if (!encodeValue.isEmpty()) {
                    res += '&' + encodeValue;
                }
            }
        } else if (params instanceof String) {
            String _params = (String) params;
            try {
                res += '&' + URLEncoder.encode(key, "UTF-8") + '=' + URLEncoder.encode(_params, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (params instanceof Number) {
            Number _params = (Number) params;
            try {
                res += '&' + URLEncoder.encode(key, "UTF-8") + '=' + URLEncoder.encode(_params.toString(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            return "";
        }
        return res.substring(1);
    }


    public static Map<String, Object> urldecode(String param) {
        if (param == null || param.isEmpty()) {
            return null;
        }
        //解码
        String[] params = param.split("&");
        Map<String, String> key2value = new TreeMap<String, String>();
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 0) {
                continue;
            }
            try {
                String keyStr = URLDecoder.decode(p[0], "UTF-8");
                if (StringUtils.isBlank(keyStr)) {
                    continue;
                }
                String valueStr;
                if (p.length == 2) {
                    valueStr = URLDecoder.decode(p[1], "UTF-8");
                } else {
                    valueStr = "";
                }
                key2value.put(keyStr, valueStr);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //遍历每一行传参
        Map<String, Object> map = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : key2value.entrySet()) {
            String keyStr = entry.getKey();
            String value = entry.getValue();
            //根目录的key
            Matcher keyMatcher = Pattern.compile("^[a-zA-Z\\_]{1}[\\w]*").matcher(keyStr);
            if (!keyMatcher.find()) {
                continue;
            }
            String key = keyMatcher.group(0);
            if (!map.containsKey(key)) {
                map.put(key, new HashMap<String, Object>());
            }

            //二级以及二级目录以上的key
            String pattern = "\\[([\\w]+?)\\]";
            Matcher filterMatcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(keyStr);
            //获取所有的patternKey
            List<String> patternKeyList = new ArrayList<String>();
            while (filterMatcher.find()) {
                String patternKey = filterMatcher.group(1);
                patternKeyList.add(patternKey);
            }
            //有子元素
            if (!patternKeyList.isEmpty()) {
                //遍历并写入
                Object childMap = map.get(key);
                int patternKeyListSize = patternKeyList.size();
                for (int j = 0; j < patternKeyListSize; j++) {
                    String patternKey = patternKeyList.get(j);
                    Map<String, Object> _childMap = (HashMap<String, Object>) childMap;
                    if (!_childMap.containsKey(patternKey)) {
                        //是否是最后一个节点，是的话直接赋值
                        if (j == patternKeyListSize - 1) {
                            _childMap.put(patternKey, value);
                            break;
                        }
                        _childMap.put(patternKey, new HashMap<String, Object>());
                    }
                    childMap = _childMap.get(patternKey);
                }
            }
            //只有一级元素
            else {
                map.put(key, value);
            }
        }
        map = (Map<String, Object>) map2list(map);
        return map;
    }

    private static Object map2list(Map<String, Object> map) {
        Set<String> keySet = map.keySet();
        boolean all_is_number = true;
        for (String key : keySet) {
            //不是数字
            if (!Pattern.matches("^[0-9]+$", key)) {
                all_is_number = false;
            }
            Object childNode = map.get(key);
            if (childNode instanceof Map) {
                childNode = map2list((Map<String, Object>) childNode);
                map.put(key, childNode);
            }
        }
        Object res;
        if (all_is_number) {
            res = new ArrayList<Object>();
            for (String key : keySet) {
                Object value = map.get(key);
                ((List<Object>) res).add(value);
            }
        } else {
            res = map;
        }
        return res;
    }


    @Test
    public void testStr() {

        Date yyyyMMdd1 = null;
        try {
            yyyyMMdd1 = new SimpleDateFormat("yyyy-MM-dd").parse("2021-07-26");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String yyyyMMdd = new SimpleDateFormat("yyyyMMdd").format(yyyyMMdd1);
        System.out.println(yyyyMMdd);
        HashMap<String, String> hs = new HashMap();
        hs.put("checkBlackList", new String("1"));
        if (hs.get("checkBlackList") == "1") {
            System.out.println("====");
        } else {
            System.out.println("////");
        }
    }

    @Test
    public void testThread() {
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(30, 30, 60L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(200), new ThreadFactoryBuilder().setNameFormat("br-test-pool-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            final int a = i;
            threadPoolExecutor.submit(() -> {
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
        while (b) {
            if (threadPoolExecutor.isTerminated()) {
                System.out.println("结束");
                b = false;
            } else {
                System.out.println("休息");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("结束:".concat(String.valueOf(end - start)));

    }

    @Test
    public void testInt() {
        ExecutorService threadPoolExecutor = new ThreadPoolExecutor(30, 30, 60L, TimeUnit.SECONDS
                , new ArrayBlockingQueue(200), new ThreadFactoryBuilder().setNameFormat("br-test-pool-%d").build()
                , new ThreadPoolExecutor.CallerRunsPolicy());
        AtomicLong l = new AtomicLong();
        Integer b = 0;
        for (int i = 0; i < 10000; i++) {
            final int a = i;
            threadPoolExecutor.submit(() -> {
                l.getAndAdd(a);
            });
            b += i;
        }
        threadPoolExecutor.shutdown();
        Boolean c = true;
        while (c) {
            if (threadPoolExecutor.isTerminated()) {
                System.out.println("结束");
                c = false;
            } else {
                System.out.println("休息");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("耗时l:" + l.get());
        System.out.println("耗时b:" + b);
    }

    @Test
    public void testLog() {
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("a", 123);
        objectObjectHashMap.put("b", Arrays.asList(1, 2, 3, 4));
        log.warn("【跑批任务】调度结束，耗时：{},分片：{}", 1, objectObjectHashMap);
    }

    @Test
    public void testLong() {
        Long a = 2L;
        Integer b = 2;
        ArrayList<Integer> objects = new ArrayList<>();
        objects.add(0);
        objects.add(1);
        boolean contains = Arrays.asList(0, 1).contains(a % 2);
        System.out.println("输出：" + contains + "ceshi:" + a % 2);
        boolean containsb = objects.contains(a % 2);
        System.out.println("输出2：" + containsb + "ceshi:" + a % 2);
        boolean containsc = objects.contains(b % 2);
        System.out.println("输出3：" + containsc + "ceshi:" + a % 2);
    }

    @Test
    public void testSm3() throws IOException {
//        String nn = Sm3Util.getSM3Value("wzq" + "9a3a4beb9508b71114ac8346122067250d205c5b123b6be277e72245ac39738b");
//        System.out.println(nn.toLowerCase());
//        String mm = nn.toLowerCase() + "dyih";
//        String sm3Value = Sm3Util.getSM3Value(mm).toLowerCase();
//        //String sm3Value =  Sm3Util.getSM3Value("b42b692a53777f13a894a881b24492fe86e06838ae23e91ac9f2ba43050bc448dyih");
//        System.out.println(sm3Value);
        LocalDate date = LocalDate.now();
        System.out.println(date);
    }


    /**
     *  异步，多任务。汇总返回值
     */
    @Test
    public  void allOfGet()  {
        //该线程池仅用于示例，实际建议使用自定义的线程池
        ExecutorService executorService = Executors.newCachedThreadPool();

        //线程安全的list，适合写多读少的场景
        List<String> resultList = Collections.synchronizedList(new ArrayList<>(50));
        CompletableFuture<String> completableFuture1 = CompletableFuture.supplyAsync(
                        () -> runTask("result1", 1000), executorService)
                .whenComplete((result, throwable) -> {
                    //任务完成时执行。用list存放任务的返回值
                    if (result != null) {
                        resultList.add(result);
                    }
                    //触发异常
                    if (throwable != null) {
                        log.error("completableFuture1  error:{}", throwable);
                    }
                });

        CompletableFuture<String> completableFuture2 = CompletableFuture.supplyAsync(
                        () -> runTask("result2", 1500), executorService)
                .whenComplete((result, throwable) ->{
                    if (result != null) {
                        resultList.add(result);
                    }
                    if (throwable != null) {
                        log.error("completableFuture2  error:{}", throwable);
                    }

                });

        List<CompletableFuture<String>> futureList = new ArrayList<>();
        futureList.add(completableFuture1);
        futureList.add(completableFuture2);

        try  {
            //多个任务
            CompletableFuture[] futureArray = futureList.toArray(new CompletableFuture[0]);
            //将多个任务，汇总成一个任务，总共耗时不超时2秒
            CompletableFuture.allOf(futureArray).get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("CompletableFuture.allOf Exception error.", e);
        }
        List<String> list = new ArrayList<>(resultList);

        list.forEach(System.out::println);
    }


    private static String runTask(String result, int millis) {
        try {
            //此处忽略实际的逻辑，用sleep代替
            //任务耗时。可以分别设置1000和3000，看未超时和超时的不同结果。
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("supplyAsyncGet error.");
        }
        return result;
    }


}
