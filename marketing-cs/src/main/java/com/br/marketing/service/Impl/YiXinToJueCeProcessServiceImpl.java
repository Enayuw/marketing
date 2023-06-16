package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.Impl.YiXin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.YiXin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 宜信推决策流程
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:23
 */
@Service
@Slf4j
public class YiXinToJueCeProcessServiceImpl implements YiXinToJueCeProcessService {
    @Resource
    private YiXinProcessGetBaseDataService yiXinProcessGetBaseDataService;

    @Resource
    private YiXinProcessExcludeRuleData yiXinProcessExcludeRuleData;
    @Override
    public void doProcess_A() {
    //Todo 1. 查询 a 情况基础数据
    //     2. 数据剔除
    //     3. 数据组装推送
        List<MarketingTransferSyncUser> marketingTransferSyncUserListA = yiXinProcessGetBaseDataService.getMarketingTransferSyncUserList_A();
        marketingTransferSyncUserListA.removeIf((e -> yiXinProcessExcludeRuleData.action_A(e)));
    }

    @Override
    public void doProcess_B() {
        //Todo 1. 查询 b 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }

    @Override
    public void doProcess_C() {
        //Todo 1. 查询 c 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }

    @Override
    public void doProcess_D() {
        //Todo 1. 查询 d 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }

    @Override
    public void doProcess_E() {
        //Todo 1. 查询 e 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }

    @Override
    public void doProcess_F() {
        //Todo 1. 查询 f 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }

    @Override
    public void doProcess_G() {
        //Todo 1. 查询 g 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }

    @Override
    public void doProcess_H() {
        //Todo 1. 查询 h 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }

    @Override
    public void doProcess_I() {
        //Todo 1. 查询 i 情况基础数据
        //     2. 数据剔除
        //     3. 数据组装推送
    }
}
