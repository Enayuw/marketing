package com.br.marketing.context;

import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.origin.MqFact;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import lombok.Data;

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
 * @Description : 从接收mq消息到处理过程，所需信息放在处理上下文中
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/12 16:26
 */
@Data
public class ProcessHandlerContext {

    /**
     * 客户apiCode
     */
    private String apiCode;

    /**
     * 转化表id
     */
    private Long transferInfoId;


    /**
     * 上下文消息对象
     */
    private MqFact mqFact;

    /**
     *
     */
    private RuleNecessaryData ruleNecessaryData;

    /**
     * 0:非最后一次，1:最后一次
     */
    private String last;

    private CustomerTagsVO customerTagsVO;

}
