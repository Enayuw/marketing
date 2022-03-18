package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.DataCompare;
import com.br.marketing.mapper.DataCompareMapper;
import com.br.marketing.origin.ProcessHandlerContext;
import com.br.marketing.rule.InterfaceParams;

import javax.annotation.Resource;
import java.util.List;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 三方对外接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 16:28
 */
public abstract class AbstractExternalInterfaceHandler<T extends InterfaceParams> {

    @Resource
    DataCompareMapper dataCompareMapper;

    /**
     * 按照三方接口逻辑调用接口
     */
    /**
     *
     * @param transferData 通过不同转化规则处理后的数据集合
     * @return
     */
    abstract JSONObject call(List<T> transferData, ProcessHandlerContext context);

    /**
     * 按照三方接口逻辑调用接口
     */
    abstract InterfaceHandlerEnum handlerEnum();

    void saveBizLog(String data, int handlerEnum,long infoId){
        DataCompare dataCompare = new DataCompare(data,handlerEnum,infoId);
        dataCompareMapper.insertSelective(dataCompare);
    }

}
