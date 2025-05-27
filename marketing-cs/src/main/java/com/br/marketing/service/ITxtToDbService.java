package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.LocalFile;

public interface ITxtToDbService {
    Result TwoSevenToDb(TxtToDbDTO content);
    Result toDbByCommon(TxtToDbDTO content);

    Result phoneTodb(TxtToDbDTO content);

    Result phoneTodbByTransfer(TxtToDbDTO content);

    Result phoneTodbByIbu(TxtToDbDTO content);

    Result phoneTodbByXW(TxtToDbDTO content);

    /**
     * D20211215桔子分期转电销api-3710037
     * <p>
     * 样例一：
     * <p>
     * 桔子分期-存量复购场景.txt
     * <p>
     * 测试编号,md5手机号,下单时间,客群类型
     * 0,572527c05beec0898790e239bacd9a5b,2021,存量复购
     * <p>
     * 样例二：
     * <p>
     * 桔子分期-注册未认证场景.txt
     * <p>
     * 测试编号,md5手机号,注册时间,客群类型
     * 100000,19d0aaa0103400db77dab60be8546ff4,2021,注册未认证
     *
     * @author Guo Zeqiang
     * @dateTime 2021/12/20 9:30
     */
    Result phoneTodbByJuZi(TxtToDbDTO content);

    Result phoneTodbByYiXin(TxtToDbDTO content);

    Result<Integer> phoneTodbByYiXinAfterAction(LocalFile file);

    Result csosPhoneTodb(TxtToDbDTO txtToDbDTO);

    Result updateFileTodb(TxtToDbDTO txtToDbDTO);
}
