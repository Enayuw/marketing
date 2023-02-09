package com.br.marketing.service;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.entity.MarketingSyncUser;

import java.util.Date;

/**
 * 有效期计算
 *
 * @author Guo Zeqiang
 * @dateTime 2023-02-09 9:30
 */
public interface IPeriodOfValidityService {

    /**
     * 已过期，有效期计算依据参数中{@code validityDate}的值
     *
     * @param date         查是否在有效期内的日期,为null时默认为当前日期
     * @param day          天的范围，也就是有效期表达式中[T+N]中的N
     * @param validityDate 计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 如果是{@code true}表示{@code date}超过效期范围
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    boolean isExpire(Date date, Integer day, Date validityDate);

    /**
     * 未过期，有效期计算依据参数中{@code validityDate}的值
     *
     * @param date         查是否在有效期内的日期,为null时默认为当前日期
     * @param day          天的范围，也就是有效期表达式中[T+N]中的N
     * @param validityDate 计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 如果是{@code true}表示{@code date}未超过效期范围
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    boolean isNotExpire(Date date, Integer day, Date validityDate);


    /**
     * 已过期，有效期计算依据参数中{@code validityDate}的值
     *
     * @param date           查是否在有效期内的日期,为null时默认为当前日期
     * @param validityDayStr 格式 [T+N]、[T+0]、 [M]
     * @param validityDate   计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 如果是{@code true}表示{@code date}超过效期范围
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    boolean isExpire(Date date, String validityDayStr, Date validityDate) throws IllegalAccessException;

    /**
     * 未过期，有效期计算依据参数中{@code validityDate}的值
     *
     * @param date           查是否在有效期内的日期,为null时默认为当前日期
     * @param validityDayStr 格式 [T+N]、[T+0]、 [M]
     * @param validityDate   计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 如果是{@code true}表示{@code date}未超过效期范围
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    boolean isNotExpire(Date date, String validityDayStr, Date validityDate) throws IllegalAccessException;


    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param apiCode        apiCode
     * @param custNum        案件编号
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、 [M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isExpire(String apiCode, String custNum, Date date, String validityDayStr) throws IllegalAccessException;

    /**
     * 未过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param apiCode        apiCode
     * @param custNum        案件编号
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、 [M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isNotExpire(String apiCode, String custNum, Date date, String validityDayStr) throws IllegalAccessException;


    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @param date    查是否在有效期内的日期
     * @param day     天的范围，也就是有效期表达式中[T+N]中的N
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isExpire(String apiCode, String custNum, Date date, Integer day) throws IllegalAccessException;

    /**
     * 未过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @param date    查是否在有效期内的日期
     * @param day     天的范围，也就是有效期表达式中[T+N]中的N
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isNotExpire(String apiCode, String custNum, Date date, Integer day) throws IllegalAccessException;


    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param syncUser       上传表过滤条件，支持 apiCode、custNum、userType
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、 [M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isExpire(MarketingSyncUser syncUser, Date date, String validityDayStr) throws IllegalAccessException;

    /**
     * 未过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param syncUser       上传表过滤条件，支持 apiCode、custNum、userType
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、 [M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isNotExpire(MarketingSyncUser syncUser, Date date, String validityDayStr) throws IllegalAccessException;

    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param syncUser 上传表过滤条件，支持 apiCode、custNum、userType
     * @param date     判断是否在有效期内的日期
     * @param day      天的范围，也就是有效期表达式中[T+N]中的N
     * @return 如果是{@code true}表示{@code date}超过效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isExpire(MarketingSyncUser syncUser, Date date, Integer day);

    /**
     * 未过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param syncUser 上传表过滤条件，支持 apiCode、custNum、userType
     * @param date     判断是否在有效期内的日期
     * @param day      天的范围，也就是有效期表达式中[T+N]中的N
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isNotExpire(MarketingSyncUser syncUser, Date date, Integer day);


    /**
     * 获取有效期构造器
     *
     * @param validityDayStr 格式 [T+N]、[T+0]、 [M]
     * @param validityDate   计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(String validityDayStr, Date validityDate) throws IllegalAccessException;

    /**
     * 获取有效期构造器
     *
     * @param day          天的范围，也就是有效期表达式中[T+N]中的N
     * @param validityDate 计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 有效期范围
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(Integer day, Date validityDate);
}
