package com.br.marketing.client.dassservice;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.utils.net.ApiCaller;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class DassServiceClient {

    @Value("${api.dass.aesKey:00}")
    private String ascKey;

    @Value("${api.dass.SecretKey:00}")
    private String secretKey;

    @Value("${api.dass.postHermesUserData:00}")
    private String postHermesUserDataUrl;
    @Value("${api.dass.isProxy:0}")
    private String isProxy;

    @Autowired
    RestTemplate restTemplate;


//    @Autowired
//    @Qualifier("restTemplateByProxy")
//    RestTemplate restTemplateByProxy;


    @Autowired
    HttpProxyClient httpProxyClient;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;
    private DassImportDataDTO t;

    public Result postHermesUserData(DassImportAdapDTO dto){
        Result result = new Result();
        List<DassImportDataDTO> dtos = dto.getList();
        long l = LocalDateTime.now().plusMinutes(10L).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        List sortList = new ArrayList();
        sortList.add(String.valueOf(l));
        dtos.forEach(t->{
            BeanMap beanMap = BeanMap.create(t);
            for (Object k : beanMap.keySet()) {
                if(String.valueOf(k).equals("id")){
                    continue;
                }
                Object o = beanMap.get(k);
                if(String.valueOf(k).equals("phone")){
                    sortList.add(AESUtil.decrypt(String.valueOf(o),ascKey));
                    continue;
                }
                if(o == null){
                    continue;
                }else if(o instanceof String){
                    if(StringUtils.isBlank(String.valueOf(o))){
                        continue;
                    }
                }else if(o instanceof List){
                    List o1 = (List) o;
                    if(o1==null||o1.size()==0){
                        continue;
                    }
                }
                sortList.add(String.valueOf(o));
            }
        });
        Collections.sort(sortList);
        String param = Joiner.on("").join(sortList);
        String sign = DigestUtils.md5DigestAsHex(String.format(secretKey + "%s", param).getBytes());
        HashMap requestParam = new HashMap();
        requestParam.put("ts",l);
        requestParam.put("sign",sign);
        requestParam.put("data", dtos);
        InterfaceLog interfaceLog = new InterfaceLog();
        interfaceLog.setRequestId(UUID.randomUUID().toString());
        interfaceLog.setRequestParam(JSON.toJSONString(requestParam));
        interfaceLog.setUrl(postHermesUserDataUrl);
        interfaceLog.setCreateTime(new Date());
        long start = System.currentTimeMillis();
        try {
            HashMap<String,String> hashMap = httpProxyClient.sendByCode(JSON.toJSONString(requestParam), postHermesUserDataUrl, isProxy.equals("0") ? false : true);
//            ThirdApiResultTransfer transfer = new ApiCaller(isProxy.equals("0") ? restTemplate : restTemplateByProxy)
//                    .setRequestParam(requestParam)
//                    .setUrl(postHermesUserDataUrl)
//                    .setContentType(MediaType.APPLICATION_JSON_UTF8)
//                    .postTransferStr();
            long end = System.currentTimeMillis();
            Integer code = null;
            if(StringUtils.isNotBlank(hashMap.get("httpcode"))){
                code = Integer.valueOf(hashMap.get("httpcode"));
                interfaceLog.setHttpCode(Integer.valueOf(hashMap.get("httpcode")));
            }

            interfaceLog.setResult(hashMap.get("content"));
            interfaceLog.setExpire(String.valueOf(end-start));
            if (Integer.valueOf(200).equals(code)) {
                result.setCode(ResultCode.SUCCESS.getValue());
            } else {
                result.setCode(ResultCode.FAIL.getValue());
            }
        }catch (Exception ex){
            long end = System.currentTimeMillis();
            interfaceLog.setExpire(String.valueOf(end-start));
            interfaceLog.setResult("程序异常："+ex.getMessage());
            log.error(ex.getMessage(),ex);
        }
        interfaceLogMapper.insertSelective(interfaceLog);
        return result;
    }
}
