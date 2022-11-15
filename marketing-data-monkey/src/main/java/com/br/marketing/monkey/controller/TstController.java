package com.br.marketing.monkey.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.client.zhongan.input.ZkReqDTO;
import com.br.marketing.client.zhongan.utils.Md5OfZanUtils;
import com.br.marketing.common.commondto.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tst")
public class TstController {

    @Autowired
    ZhongAnClient zhongAnClient;

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
        Result<Boolean> booleanResult = zhongAnClient.zkXd(xd);
        Result<Boolean> booleanResult1 = zhongAnClient.zkBx(bx);
        System.out.println("0 ==== "+JSON.toJSONString(booleanResult));
        System.out.println("1 ==== "+JSON.toJSONString(booleanResult1));
        return "123";
    }
}
