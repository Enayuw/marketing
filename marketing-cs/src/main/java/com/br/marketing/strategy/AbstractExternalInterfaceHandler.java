package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.rule.InterfaceParams;

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



}
