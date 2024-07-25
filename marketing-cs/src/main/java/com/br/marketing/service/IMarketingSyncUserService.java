package com.br.marketing.service;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.vo.TodayIdTimeBySoleVo;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IMarketingSyncUserService {

    /**
     * 获取有效去重的数据数量
     *
     * @return
     */
    Long countRepeat(String execSql);

    /**
     * 获取当天有效的去重数据
     *
     * @return
     */
    TodayIdTimeBySoleVo getSoleValidUser(String execSql);

    Integer updateRepeatUserStatus(String execSql);

    /**
     * 是否在有效期内
     *
     * @param apiCode  apiCode
     * @param custNum  案件编号
     * @param userType 场景
     * @param date     判断是否在有效期内的日期
     * @param day      天的范围，+day 为T+day；-day 为T-day；null为T月底
     * @return true or false ,在有效期间为true，否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    Boolean isPeriodOfValidity(String apiCode, String custNum, String userType, Date date, Integer day);

    /**
     * 是否在有效期内
     *
     * @param apiCode      apiCode
     * @param custNum      案件编号
     * @param userType     场景
     * @param date         判断是否在有效期内的日期
     * @param day          天的范围，+day 为{@code validityDate+day}；-day 为{@code validityDate-day}；
     *                     0为{@code validityDate}月底
     * @param validityDate 计算有效期范围的日期，eg：(validityDate +|- day)
     * @return true or false ,在有效期间为true，否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    @Deprecated
    Boolean isPeriodOfValidity(String apiCode, String custNum, String userType, Date date, int day, Date validityDate);

    /**
     * 是否在有效期内
     *
     * @param date         判断是否在有效期内的日期
     * @param day          天的范围，+day 为{@code validityDate+day}；-day 为{@code validityDate-day}；
     *                     null为{@code validityDate}月底
     *                     0为{@code validityDate}当天
     * @param validityDate 计算有效期范围的日期，eg：(validityDate +|- day)
     * @return true or false ,在有效期间为true，否则为false
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    Boolean isPeriodOfValidity(Date date, Integer day, Date validityDate);

    /**
     * 获取有效期构造器
     *
     * @param day          天的范围，+day 为{@code validityDate+day}；-day 为{@code validityDate-day}；
     *                     null为{@code validityDate}月底
     *                     0为{@code validityDate}当天
     * @param validityDate 计算有效期范围的日期，eg：(validityDate +|- day)
     * @return 有效期范围
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:58
     */
    PeriodOfValidityBO.Builder getPeriodOfValidityRange(Integer day, Date validityDate);

    /**
     * 根据案件编号获取客户最新的场景
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @return userType
     * @author Guo Zeqiang
     * @dateTime 2022/2/11 18:17
     */
    String getUserTypeLatestByCustNum(String apiCode, String custNum);

    /**
     * 根据案件编号获取最新的taskId
     *
     * @param apiCode  apiCode
     * @param custNum  案件编号
     * @param userType 场景
     * @return taskId
     * @author Guo Zeqiang
     * @dateTime 2022/2/15 10:52
     */
    String getTaskIdLatestByCustNum(String apiCode, String custNum, String userType);


    /**
     * 获取案件编号的上传时间
     *
     * @param apiCode  apiCode
     * @param custNum  案件编号
     * @param userType 场景
     * @return AppletTime
     * @author Guo Zeqiang
     * @dateTime 2022/2/15 10:52
     */
    String getAppletTimeByCustNumAndUserType(String apiCode, String custNum, String userType);

    /**
     * 获取案件编号的落库的创建时间
     *
     * @param apiCode  apiCode
     * @param custNum  案件编号
     * @param userType 场景
     * @return CreatTime
     * @author Guo Zeqiang
     * @dateTime 2022/2/18 10:52
     */
    Date getCreatTimeByCustNumAndUserType(String apiCode, String custNum, String userType);

    /**
     * 根据案件编号批量获取落库的创建时间
     *
     * @param apiCode     apiCode集合
     * @param custNums    案件编号集合
     * @param userType    场景
     * @param dateTimeEnd 截止时间
     * @return key custNum; value:creatTime
     * @author Guo Zeqiang
     * @dateTime 2022/7/14 10:52
     */
    Map<String, Date> getSyncUserTimeMaxByCustNumsMap(String apiCode, Set<String> custNums, String userType
            , String dateTimeEnd);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return list
     */
    List<MarketingSyncUser> getFreeUserTypeAndDateAllFieldList(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return Map key custNum; value MarketingSyncUser
     */
    Map<String, List<MarketingSyncUser>> getFreeUserTypeAndDateAllFieldMap(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return Map key custNum; value MarketingSyncUser
     */
    Map<String, MarketingSyncUser> getFreeUserTypeAndDateAllFieldMapValueOne(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return list
     */
    List<MarketingSyncUser> getFreeUserTypeAndDateList(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return list
     */
    Map<String, List<MarketingSyncUser>> getFreeUserTypeAndDateMap(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return list
     */
    Map<String, MarketingSyncUser> getFreeUserTypeAndDateMapValueOne(String apiCode
            , Set<String> custNumSet, Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @return list
     */
    Map<String, MarketingSyncUser> getFreeUserTypeAndDateMapValueOne(String apiCode
            , Set<String> custNumSet);

    /**
     * 2022/11/14 19:37
     * 根据手机号 批量获取最新时间上传数据信息
     *
     * @param apiCode apiCode
     * @param cellSet 手机号集合
     * @return Map key cell; value MarketingTransferSyncUser
     */
    Map<String, MarketingSyncUser> getCellByCellAndMaxAppletTimeMap(String apiCode, Set<String> cellSet);

    Boolean existUploadTable(String apiCode);

    List<MarketingSyncUser> noDesUploadByMinId(String apiCode,Long minId);

    Long noDesUploadOfMinId(String apiCode);

    Integer updateSqlByNoDes(String updateSql);
}
