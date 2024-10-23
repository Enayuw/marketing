package com.br.marketing.monkey.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.qifu.*;
import com.br.marketing.client.tongcheng.TongChengAgentMktClient;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.client.zhongan.input.ZkReqDTO;
import com.br.marketing.client.zhongan.output.ZkReponseVO;
import com.br.marketing.client.zhongan.utils.Md5OfZanUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.monkeydata.entity.commonobj.MonkeyContext;
import com.br.marketing.monkeydata.entity.commonobj.PageCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/tst")
@Slf4j
public class TstController {

    @Autowired
    ZhongAnClient zhongAnClient;
    @Autowired
    QiFuClients qiFuClients;

    @Autowired
    IMonkeyDataHandle zhongAnHandleImpl;

    @GetMapping("/queryList")
    public String queryList(){
        ArrayList<RealDataesReq> list = new ArrayList<>();
        RealDataesReq realDataesReq = new RealDataesReq();
        realDataesReq.setUniqueReqNo("AGOP6567212229496934401");
        realDataesReq.setMobileMd5("c4a2ec03981c32175c52c1c5ecf3802b");
        list.add(realDataesReq);

        String uuid = UUID.randomUUID().toString();
        QrySleepUserRealMessageReq req = new QrySleepUserRealMessageReq();
        req.setRealDataes(list);
        req.setRequestNo(uuid);
        req.setBatchNo("68228_6567211717338857473");
        req.setInitiatingType("noArt");
        req.setPartner("bairong");

        Result<ResponseData<QrySleepUserRealMessageResp>> responseDataResult =
                qiFuClients.qryUserRealMessageUrl(req);
        return JSONObject.toJSONString(responseDataResult);
    }

    @GetMapping("/testPushZan")
    public String testPushZan(){

        ZaMarketDataDTO dataDTO = new ZaMarketDataDTO();
        List<ZaMarketDetail> details = new ArrayList<>();
//        dataDTO.setReqNo(UUID.randomUUID().toString().replaceAll("-",""));
//        dataDTO.setReqNo("b88db8f0098643eca073d89268c0c658");
        dataDTO.setData(details);
        ZaMarketDetail zaMarketDetail = new ZaMarketDetail();
        zaMarketDetail.setChannelCode(ZhongAnClient.XdChannelCode);
        zaMarketDetail.setMobileMd5(Md5OfZanUtils.getMD5("14413201320"));
        zaMarketDetail.setTaskId("0");
        zaMarketDetail.setBizDate("2022-11-14");
        zaMarketDetail.setTag("MG");

        ZaMarketDetail zaMarketDetail1 = new ZaMarketDetail();
        zaMarketDetail1.setChannelCode(ZhongAnClient.XdChannelCode);
        zaMarketDetail1.setMobileMd5(Md5OfZanUtils.getMD5("14413211321"));
        zaMarketDetail1.setTaskId("1");
        zaMarketDetail1.setBizDate("2022-11-14");
        zaMarketDetail1.setTag("MG");

        ZaMarketDetail zaMarketDetail2 = new ZaMarketDetail();
        zaMarketDetail2.setChannelCode(ZhongAnClient.XdChannelCode);
        zaMarketDetail2.setMobileMd5(Md5OfZanUtils.getMD5("14413221322"));
        zaMarketDetail2.setTaskId("2");
        zaMarketDetail2.setBizDate("2022-11-14");
        zaMarketDetail2.setTag("CG");

        details.add(zaMarketDetail);
        details.add(zaMarketDetail1);
        details.add(zaMarketDetail2);

        zhongAnClient.pushDetail(dataDTO);
        return "123";
    }


    @GetMapping("/testZk")
    public String testZk(){

        ZkReqDTO xd = new ZkReqDTO();
        ZkReqDTO bx = new ZkReqDTO();
        xd.setCustMobileMd5(Md5OfZanUtils.getMD5("14413201320"));
        xd.setChannelCode(ZhongAnClient.XdChannelCode);
        bx.setCustMobileMd5(Md5OfZanUtils.getMD5("14413211321"));
        bx.setChannelCode(ZhongAnClient.BxChannelCode);
        Result<ZkReponseVO> booleanResult = zhongAnClient.zkXd(xd);
        Result<ZkReponseVO> booleanResult1 = zhongAnClient.zkBx(bx);
        System.out.println("0 ==== "+JSON.toJSONString(booleanResult));
        System.out.println("1 ==== "+JSON.toJSONString(booleanResult1));
        return "123";
    }

    @GetMapping("/testInterface")
    public String testInterface(){
        PageCondition pageCondition = new PageCondition();
        pageCondition.setPageIndex(1);
        MonkeyContext.setProcessContext("123");
        zhongAnHandleImpl.action(pageCondition);
        return "123";
    }

    @GetMapping("/testLogError")
    public String testLogError(){
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(200, 200);
        for (int i = 0; i < 300; i++) {
            threadPool.submit(()->
                    {
                        RuntimeException runtimeException = new RuntimeException("url=https://finance-gateway-pop.diandian.com.cn/fcpGateway param=ZhongAnRequestDTO(apiKey=zadpreloan.nexusmetric.07brdyy01, reqNo=22684673dbda48d9be5b73459ff741ff, reqDate=2023-03-29, gatewayVersion=1.0.0, bizParam=XjY5R26rHuFEbLq3pQi5lvshWdtfn09hz0XKPNG2uwqvliIfgDqYL9UDxnfcB9/1QkioxYm2mC82Xd7B9sIR0xGei1KIZgEKsiwlXlZEaE2/rQCX8fs43hIDPHAGn1hfLuGfeku1V11EZUa0BVV0d/ibXHQLY633r03cc+QTYEs=, sign=9c67bee0e3b7990d7f40c8e5e05c26e7)\n" +
                                "org.apache.http.conn.HttpHostConnectException: Connect to squid-proxy2.brapp.com:3128 [/squid-proxy2.brapp.com] failed: Connection timed out (Connection timed out)");
                        log.error("url={} param={}","https://finance-gateway-pop.diandian.com.cn/fcpGateway","ZhongAnRequestDTO(apiKey=zadpreloan.nexusmetric.07brdyy01, reqNo=22684673dbda48d9be5b73459ff741ff, reqDate=2023-03-29, gatewayVersion=1.0.0, bizParam=XjY5R26rHuFEbLq3pQi5lvshWdtfn09hz0XKPNG2uwqvliIfgDqYL9UDxnfcB9/1QkioxYm2mC82Xd7B9sIR0xGei1KIZgEKsiwlXlZEaE2/rQCX8fs43hIDPHAGn1hfLuGfeku1V11EZUa0BVV0d/ibXHQLY633r03cc+QTYEs=, sign=9c67bee0e3b7990d7f40c8e5e05c26e7)",runtimeException);
                    });
        }
        threadPool.shutdown();
        Boolean b = true;
        while (b){
            if(threadPool.isTerminated()){
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
        return "success";
    }

    @GetMapping("/testReq")
    public String testReq(){
        log.error("test");
        return "testReq";
    }

    @Autowired
    TongChengAgentMktClient tongChengAgentMktClient;

    @GetMapping("/testTongChengAgentMkt")
    public String testTongChengAgentMkt(){
        List<Map<String,String>> dataList = new ArrayList<>();
        Map<String,String> map  = new HashMap<>();
        map.put("mobileMd5","21ea88b0c91d2283964e8fbf13499ef3");
        dataList.add(map);
        Result result = tongChengAgentMktClient.pushToTongChengAgentMkt(dataList, "7492639",null);
        log.warn("Result:{}",result);
        return "testTongChengAgentMkt";
    }
}
