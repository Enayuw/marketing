package com.br.marketing.api.entities.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.br.marketing.api.entity.MerchantParam;
import com.br.marketing.api.entity.RiskRank;
import com.br.marketing.api.entity.Strategy;
import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.constants.web.RequestType;
import lombok.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.util.ArrayList;
import java.util.List;

/** 策略贷中API请求参数
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@Data
public class StrategyApiContext implements BeanFactoryAware {

    private StrategyResult  strategyResult;
    private boolean done=false;
    private String apiCode;
    /**
     * 策略编号
     * */
    private String strategyId;
    /**
     * 数据删除时间
     * */
    private Object deleteTime;


    /**
     * 拥有着id
     */
    private Integer createUser;
    /**
     * json数据参数
     * */
    private Object jsonData;

    private Object reqData;

    /**
     * 校验成功的用户参数集合
     * */
    private List<ApiUserParam> paramList = new ArrayList<>();

    /**
     * 贷中策略定义
     * */
    private Strategy strategy;

    /**数据策略*/
    private JSONObject dtbStrategy;

    /**
     * 贷中风险分级定义
     * */
    private RiskRank riskRank;


    /**
     * 策略请求类型，默认为API请求
     * */
    private String requestType = RequestType.API_BATCH.getCode();

    /**
     * 批量流水号
     * */
    private String swiftNumber;

    /**
     * spring 上下文对象
     * */
    private transient BeanFactory springContext;

    /**
     * 是否展示数据产品详情
     * */
    private transient AuthShowProductor isShowData;

    private Integer accountType;

    private Boolean haveRule=Boolean.FALSE;

    private Boolean haveReview=Boolean.FALSE;

    private Boolean haveBehavior=Boolean.FALSE;

    private Boolean haveSanxiangzhili=Boolean.FALSE;

    private Boolean haveHx=Boolean.FALSE;


    /**
     * 請求的url
     */
    private String requestUrl;

    /**
     * Translatejson array object.
     *
     * @return the object
     */
    public Object translatejsonArray() {
        if (jsonData instanceof JSONArray) {
            return jsonData;
        }else {
            jsonData = JSONArray.parseArray(jsonData.toString());
            return jsonData;
        }
    }

    @JSONField(serialize = false)
    private MerchantParam merchantParam;

    /**
     * Add user param.
     *
     * @param putParam the put param
     */
    public void addUserParam(ApiUserParam putParam) {
        paramList.add(putParam);
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        springContext = beanFactory;
    }

    /**
     * Translatejson object object.
     *
     * @return the object
     */
    public Object translatejsonObject() {
        jsonData = JSONObject.parse(jsonData.toString());
        return jsonData;
    }

    @Override
    public String toString() {
        return "StrategyApiContext{" +
                "apiCode='" + apiCode + '\'' +
                ", strategyId='" + strategyId + '\'' +
                ", deleteTime=" + deleteTime +
                ", createUser=" + createUser +
                ", jsonData=" + jsonData +
                ", swiftNumber='" + swiftNumber + '\'' +
                ", accountType=" + accountType +
                '}';
    }
}
