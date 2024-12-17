package com.br.marketing.service;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 有效期计算
 *
 * @author Guo Zeqiang
 * @dateTime 2023-02-09 9:30
 */
public interface IPeriodOfValidityService {

    /**
     * 已过期，有效期计算依据参数中{@code validityDate}的值
     * <p>
     * 批量数据判断有效期时建议使用该方法，因为提前计算出{@code day}可提升效率，避免重复计算{@code day}的值；
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
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
     * <p>
     * 批量数据判断有效期时建议使用该方法，因为提前计算出{@code day}可提升效率，避免重复计算{@code day}的值
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
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
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @param validityDate   计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 如果是{@code true}表示{@code date}超过效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    boolean isExpire(Date date, String validityDayStr, Date validityDate) throws IllegalArgumentException;

    /**
     * 未过期，有效期计算依据参数中{@code validityDate}的值
     *
     * @param date           查是否在有效期内的日期,为null时默认为当前日期
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @param validityDate   计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 如果是{@code true}表示{@code date}未超过效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     * @deprecated 方法已过时，需使用新版有效期
     */
    @Deprecated
    boolean isNotExpire(Date date, String validityDayStr, Date validityDate) throws IllegalArgumentException;

    /**
     * 已过期，有效期计算依据参数中{@code validityDateSupplier}的值
     *
     * @param date                   查是否在有效期内的日期,为null时默认为当前日期
     * @param validityDayStrSupplier 返回格式： [T+N]、[T+0]、[M]的字符串或整形数字，只接受“String”或“Integer”数据类型
     * @param validityDateSupplier   返回计算有效期范围的日期，也就是有效期表达式中[T+N]中的T，通过自定义函数提供
     * @return 如果是{@code true}表示{@code date}超过效期范围
     * @throws IllegalArgumentException 非法参数类型或无法解析有效期格式
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    boolean isExpire(Date date, Supplier<Object> validityDayStrSupplier
            , Supplier<Date> validityDateSupplier) throws IllegalArgumentException;

    /**
     * 未过期，有效期计算依据参数中{@code validityDateSupplier}的值
     *
     * @param date                   查是否在有效期内的日期,为null时默认为当前日期
     * @param validityDayStrSupplier 返回格式： [T+N]、[T+0]、[M]的字符串或整形数字，只接受“String”或“Integer”数据类型
     * @param validityDateSupplier   返回计算有效期范围的日期，也就是有效期表达式中[T+N]中的T，通过自定义函数提供
     * @return 如果是{@code true}表示{@code date}未超过效期范围
     * @throws IllegalArgumentException 非法参数类型或无法解析有效期格式
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    boolean isNotExpire(Date date, Supplier<Object> validityDayStrSupplier
            , Supplier<Date> validityDateSupplier) throws IllegalArgumentException;


    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param apiCode        apiCode
     * @param custNum        案件编号
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isExpire(String apiCode, String custNum, Date date, String validityDayStr) throws IllegalArgumentException;

    /**
     * 未过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param apiCode        apiCode
     * @param custNum        案件编号
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isNotExpire(String apiCode, String custNum, Date date, String validityDayStr) throws IllegalArgumentException;


    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     * <p>
     * 批量数据判断有效期时建议使用该方法，因为提前计算出{@code day}可提升效率，避免重复计算{@code day}的值
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @param date    查是否在有效期内的日期
     * @param day     天的范围，也就是有效期表达式中[T+N]中的N
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isExpire(String apiCode, String custNum, Date date, Integer day) throws IllegalArgumentException;

    /**
     * 未过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     * <p>
     * 批量数据判断有效期时建议使用该方法，因为提前计算出{@code day}可提升效率，避免重复计算{@code day}的值
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @param date    查是否在有效期内的日期
     * @param day     天的范围，也就是有效期表达式中[T+N]中的N
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isNotExpire(String apiCode, String custNum, Date date, Integer day) throws IllegalArgumentException;

    /**
     * 过期 返回true；过期返回false
     * @param dataDateStr 数据上传日期
     * @param validityDayStr 有效期配置
     * @param dtf  对dataDate参数日期格式 （不传默认是yyyy-MM-dd格式）
     * @return
     * @throws IllegalArgumentException
     */
    boolean isExpire(String dataDateStr, String validityDayStr,DateTimeFormatter dtf) throws IllegalArgumentException, ParseException;

    /**
     * 已过期案件编号,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime
     *
     * @param apiCode        apiCode
     * @param custNumSet     案件编号集合
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 超过有效期范围案件编号
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    List<String> isExpire(String apiCode, Set<String> custNumSet, Date date, String validityDayStr)
            throws IllegalArgumentException;

    /**
     * 未过期案件编号,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime
     *
     * @param apiCode        apiCode
     * @param custNumSet     案件编号集合
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 未超过有效期范围案件编号
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    List<String> isNotExpire(String apiCode, Set<String> custNumSet, Date date, String validityDayStr)
            throws IllegalArgumentException;

    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param syncUser       上传表过滤条件，支持 apiCode、custNum、userType
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isExpire(MarketingSyncUser syncUser, Date date, String validityDayStr) throws IllegalArgumentException;

    /**
     * 未过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param syncUser       上传表过滤条件，支持 apiCode、custNum、userType
     * @param date           查是否在有效期内的日期
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 如果是{@code true}表示{@code date}未超过有效期范围
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:08
     */
    boolean isNotExpire(MarketingSyncUser syncUser, Date date, String validityDayStr) throws IllegalArgumentException;

    /**
     * 已过期,有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     * <p>
     * 批量数据判断有效期时建议使用该方法，因为提前计算出{@code day}可提升效率，避免重复计算{@code day}的值
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
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
     * <p>
     * 批量数据判断有效期时建议使用该方法，因为提前计算出{@code day}可提升效率，避免重复计算{@code day}的值
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
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
     * 有效期构造器
     *
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @param validityDate   计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 有效期范围, {@code validityDate} 为null时，返回null
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(String validityDayStr, Date validityDate)
            throws IllegalArgumentException;

    /**
     * 有效期构造器
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
     *
     * @param day          天的范围，也就是有效期表达式中[T+N]中的N
     * @param validityDate 计算有效期范围的日期，也就是有效期表达式中[T+N]中的T
     * @return 有效期范围, {@code validityDate} 为null时，返回null
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(Integer day, Date validityDate);

    /**
     * 有效期构造器
     * <p>
     * 有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param apiCode        apiCode
     * @param custNum        案件编号
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 有效期范围, 上传数据为获取到时，返回null
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(String apiCode, String custNum, String validityDayStr)
            throws IllegalArgumentException;

    /**
     * 有效期构造器
     * <p>
     * 有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @param day     天的范围，也就是有效期表达式中[T+N]中的N
     * @return 有效期范围, 上传数据为获取到时，返回null
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(String apiCode, String custNum, Integer day);

    /**
     * 有效期构造器
     * <p>
     * 有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     *
     * @param syncUser       上传表过滤条件，支持 apiCode、custNum、userType
     * @param validityDayStr 格式 [T+N]、[T+0]、[M]
     * @return 有效期范围, 上传数据为获取到时，返回null
     * @throws IllegalArgumentException 有效期格式无法解析
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(MarketingSyncUser syncUser, String validityDayStr)
            throws IllegalArgumentException;

    /**
     * 有效期构造器
     * <p>
     * 有效期计算依据为上传表中AppletTime或CreateTime，优先使用AppletTime，都为null时默认为当前日期
     * 计算{@code day}的工具{@link com.br.marketing.util.PeriodOfValidityHelper}
     *
     * @param syncUser 上传表过滤条件，支持 apiCode、custNum、userType
     * @param day      天的范围，也就是有效期表达式中[T+N]中的N
     * @return 有效期范围, 上传数据为获取到时，返回null
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(MarketingSyncUser syncUser, Integer day);

    /**
     * 有效期构造器
     * <p>
     * 有效期计算通过自定义函数{@code validityDayStrSupplier}提供
     *
     * @param validityDayStrSupplier 返回格式： [T+N]、[T+0]、[M]的字符串或整形数字，只接受“String”或“Integer”数据类型
     * @param validityDateSupplier   返回计算有效期范围的日期，也就是有效期表达式中[T+N]中的T，通过自定义函数提供
     * @return 有效期范围, {@code validityDateSupplier}结果为null时，返回null
     * @throws IllegalArgumentException 非法参数类型或无法解析有效期格式
     * @author Guo Zeqiang
     * @dateTime 2023/2/9 9:18
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(Supplier<Object> validityDayStrSupplier
            , Supplier<Date> validityDateSupplier) throws IllegalArgumentException;


    /**
     * 2023-07-07 15:18
     * 生成默认有效期范围
     *
     * @param syncUser 上传数据
     * @return Boolean
     */
    Result<Boolean> configValidDateDefault(MarketingSyncUser syncUser);

    /**
     * 生成定制化默认有效期范围
     *
     * @param syncUser 上传数据
     * @return Boolean
     */
    Result<Boolean> customizeConfigValidDateDefault(MarketingSyncUser syncUser);

    /**
     * 根据明确的有效期开始和结束时间生成有效期范围
     * 使用范围：三方流程数据上传
     * @param syncUser 上传数据
     * @return Boolean
     */
    Result<Boolean> generateConfigValidByStartAndEndDate(MarketingSyncUser syncUser);
}
