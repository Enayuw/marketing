package com.br.marketing.service.Impl;

import com.br.marketing.client.didi.DiDiClient;
import com.br.marketing.client.didi.input.DiDiReqVO;
import com.br.marketing.client.didi.output.DiDiFailUserVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.entity.DidiData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.DidiDataMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;

@Service
@Slf4j
public class DiDiServiceImpl {

    @Resource
    DidiDataMapper didiDataMapper;

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Autowired
    DiDiClient diDiClient;
    
    @Transactional(rollbackFor = Exception.class)
    public void invalidData(DidiData didiData){

        DidiData updateEntity = new DidiData();
        updateEntity.setId(didiData.getId());
        updateEntity.setStatus(5);
        int diSize = didiDataMapper.updateByPrimaryKeySelective(updateEntity);
        if(diSize<=0){
            log.warn(String.format("未更新到流量准入数据 didiDataId：%d",didiData.getId()));
        }
        Integer syncSize = marketingSyncUserMapper.updateStatus(didiData.getApiCode(), didiData.getCell(), didiData.getPushDate());
        if(syncSize<=0){
            log.warn(String.format("未更新到上传数据 apiCode:%s,custNum:%s,appletDate:%s",didiData.getApiCode(),didiData.getCell(),didiData.getPushDate()));
        }
//        //todo 测试异常数据
//        if("432f3595dfb187ef811c9d4761c344aa".equals(didiData.getCell())){
//            throw new KnowException("测试异常回滚");
//        }
        DiDiReqVO diDiReqVO = new DiDiReqVO();
        diDiReqVO.setCustMobileMd5(didiData.getCell());
        Result<DiDiFailUserVO> diDiFailUserVOResult = diDiClient.failUser(diDiReqVO);
        if(!ResultCode.SUCCESS.getValue().equals(diDiFailUserVOResult.getCode())){
            throw new KnowException("调用滴滴营销失败接口失败");
        }
    }
}
