package com.br.marketing.monkey.controller;

import com.br.marketing.client.zhongan.ZhongAnClient;
import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.client.zhongan.input.ZaMarketDetail;
import com.br.marketing.client.zhongan.input.ZhongAnRequestDTO;
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
        dataDTO.setReqNo(UUID.randomUUID().toString().replaceAll("-",""));
        dataDTO.setData(details);
        ZaMarketDetail zaMarketDetail = new ZaMarketDetail();
        zaMarketDetail.setChannelCode(ZhongAnClient.XdChannelCode);
        zaMarketDetail.setMobileMd5("0");
        zaMarketDetail.setTaskId("0");
        zaMarketDetail.setBizDate("0");
        zaMarketDetail.setTag("MG");

        ZaMarketDetail zaMarketDetail1 = new ZaMarketDetail();
        zaMarketDetail1.setChannelCode(ZhongAnClient.XdChannelCode);
        zaMarketDetail1.setMobileMd5("1");
        zaMarketDetail1.setTaskId("1");
        zaMarketDetail1.setBizDate("1");
        zaMarketDetail1.setTag("MG");

        ZaMarketDetail zaMarketDetail2 = new ZaMarketDetail();
        zaMarketDetail2.setChannelCode(ZhongAnClient.XdChannelCode);
        zaMarketDetail2.setMobileMd5("2");
        zaMarketDetail2.setTaskId("2");
        zaMarketDetail2.setBizDate("2");
        zaMarketDetail2.setTag("CG");

        details.add(zaMarketDetail);
        details.add(zaMarketDetail1);
        details.add(zaMarketDetail2);

        zhongAnClient.pushDetail(dataDTO,null);
        return "123";
    }
}
