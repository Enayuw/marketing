package com.br.marketing.check.service.Impl;

import com.br.marketing.check.service.OrangePushDassService;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 桔子推送电销 业务实现
 *
 * @author Guo Zeqiang
 * @dateTime 2022/10/19 14:32
 */
@Service
public class OrangePushDassServiceImpl implements OrangePushDassService {

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Override
    public void transferCyclicalPushDaas(String apiCode) {

    }
}
