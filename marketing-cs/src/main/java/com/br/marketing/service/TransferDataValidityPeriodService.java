package com.br.marketing.service;

import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/14 14:44
 */
public interface TransferDataValidityPeriodService {

    /**
     * (T+N),(T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，不在返回可空
     */
    MarketingSyncUser getNewValidityPeriodData(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate);

    /**
     * (T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，不在返回可空
     */
    MarketingSyncUser getNewValidityPeriodDataFirstVersion(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate);

    /**
     * (T+N),(T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，不在返回可空(滴滴专用)
     */
    MarketingSyncUser getMarketingSyncUserDidi(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate);

    /**
     * (T+N),(T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，返回带电话的转化数据不在返回可空
     */
    MarketingTransferSyncUserCell getNewValidityPeriodTransferData(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate);

    /**
     * 判断转化数据是否在有效期内，在的话返回true，不在返回false
     */
    boolean isValidityPeriod(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate);
    boolean isValidityPeriodFirstVersion(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate);

    /**
     * 有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     *
     * @param transferSyncUserList 转化数据集合
     * @param apiCode              客户编号
     * @return Map key：custNum value：SyncUserValidityPeriodBO {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     * @author Guo Zeqiang
     * @dateTime 2023-03-22 16:07
     */
    Map<String, SyncUserValidityPeriodBO> getSyncUserValidityPeriodMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode);


    /**
     * 场景中有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     *
     * @param transferSyncUserList 转化数据集合
     * @param apiCode              客户编号
     * @return Map key：custNum value：Map key：userType value：SyncUserValidityPeriodBO {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     * @author Guo Zeqiang
     * @dateTime 2023-03-22 16:07
     */
    Map<String, Map<String, SyncUserValidityPeriodBO>> getSyncUserValidityPeriodUserTypeMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode);


    /**
     * 获取范围内的有效期的原始数据
     * @param transferSyncUserList
     * @param apiCode
     * @param limitDate
     * @return
     */
    Map<String, Map<String, SyncUserValidityPeriodBO>> getSyncUserValidityPeriodUserTypeMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode,String limitDate);

    /**
     * 有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     *
     * @param transferSyncUserList 转化数据集合
     * @param apiCode              客户编号
     * @param requestDateObj       接收日期，为null时使用转化数据请求日期，
     *                             支持数据格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar,
     *                             非以上格式时默认当前日期
     * @return Map key：custNum value：SyncUserValidityPeriodBO {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     * @author Guo Zeqiang
     * @dateTime 2023-03-22 16:07
     */
    Map<String, SyncUserValidityPeriodBO> getSyncUserValidityPeriodMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode, Object requestDateObj)
            throws ParseException, IllegalArgumentException;

    /**
     * 场景中有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     *
     * @param transferSyncUserList 转化数据集合
     * @param apiCode              客户编号
     * @param requestDateObj       接收日期，为null时使用转化数据请求日期，
     *                             支持数据格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar,
     *                             非以上格式时默认当前日期
     * @return Map key：custNum value：Map key：userType value：SyncUserValidityPeriodBO {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     * @author Guo Zeqiang
     * @dateTime 2023-03-22 16:07
     */
    Map<String, Map<String, SyncUserValidityPeriodBO>> getSyncUserValidityPeriodUserTypeMap(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode, Object requestDateObj,String UploadLimitDate)
            throws ParseException, IllegalArgumentException;

    /**
     * 根据apiCode和日期
     * 获取T+N规则的有效开始时间
     *
     * @param apiCode
     * @return
     */
    Result<Date> getValidityBeginOfTn(String apiCode, Date endDate);


    /**
     * 有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     *
     * @param transferSyncUserList 转化数据集合
     * @param apiCode              客户编号
     * @param requestDateObj       接收日期，为null时使用转化数据请求日期，
     *                             支持数据格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar,
     *                             非以上格式时默认当前日期
     * @return Map key：custNum+userType value：SyncUserValidityPeriodBO
     * {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     * @author Guo Zeqiang
     * @dateTime 2023-07-11 10:07
     */
    Map<String, SyncUserValidityPeriodBO> getValidityPeriodUserTypeBatchFirstVersion(
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode, Object requestDateObj) throws ParseException;


}
