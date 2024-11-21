package com.br.marketing.service;

import com.br.marketing.bo.CellValidityPeriodBO;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/14 14:44
 */
public interface TransferDataValidityPeriodService {


    /**
     * (T+N),(T,N)
     * 判断转化数据是否在有效期内,在的话返回最新一条上传数据，返回带电话的转化数据不在返回可空
     */
    MarketingTransferSyncUserCell getNewValidityPeriodTransferData(MarketingTransferSyncUser marketingTransferSyncUser, String requestDate);


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
     * 有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     * 存在上传数据，有效
     * 不存在上传数据，无效
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
            List<MarketingTransferSyncUser> transferSyncUserList, String apiCode, Object requestDateObj);


    /**
     * 根据cell获取有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     * 存在上传数据，有效
     * 不存在上传数据，无效
     *
     * @param cellSet        手机号数据集合
     * @param apiCode        客户编号
     * @param requestDateObj 接收日期，为null时使用转化数据请求日期，
     *                       支持数据格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar,
     *                       非以上格式时默认当前日期
     * @return Map  key：cell value：SyncUserValidityPeriodBO
     * <p>
     * {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     * @author Guo Zeqiang
     * @dateTime 2023-07-13 10:07
     */
    Map<String, SyncUserValidityPeriodBO> getValidityPeriodCellBatchFirstVersion(
            Set<String> cellSet, String apiCode, Object requestDateObj);

    /**
     * 根据custNum获取有效期内的原始数据（上传数据）{@link MarketingSyncUser}及有效期范围{@link PeriodOfValidityBO.Builder}
     * 存在上传数据，有效
     * 不存在上传数据，无效
     *
     * @param custNumSet     案件编号数据集合
     * @param apiCode        客户编号
     * @param requestDateObj 接收日期，为null时使用转化数据请求日期，
     *                       支持数据格式 String(yyyy-MM-dd)、Date、LocalDate、LocalDateTime、Long、Calendar,
     *                       非以上格式时默认当前日期
     * @return Map  key：custNum value：SyncUserValidityPeriodBO
     * <p>
     * {@linkplain SyncUserValidityPeriodBO MarketingSyncUser PeriodOfValidityBO.Builder}
     * @author Guo Zeqiang
     * @dateTime 2023-07-13 10:07
     */
    Map<String, SyncUserValidityPeriodBO> getValidityPeriodCustNumBatchFirstVersion(
            Set<String> custNumSet, String apiCode, Object requestDateObj);


    /**
     * 根据custNum获取多组有效期期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param custNumSet     custNum集合
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/10/07
     */
    Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCustNum(Set<String> custNumSet, String apiCode, Object requestDateObj);


    /**
     * 根据上传数据custNum+userType获取多组有效期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param custNumSet     custNum集合
     * @param userType       场景
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/12/08
     */
    Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCustNumAndUserType(Set<String> custNumSet,
                                                                                  String userType,
                                                                                  String apiCode,
                                                                                  Object requestDateObj);


    /**
     * 根据上传数据cell+userType获取多组有效期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param cellSet        cell集合
     * @param userType       场景
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/12/08
     */
    Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCellAndUserType(Set<String> cellSet,
                                                                               String userType,
                                                                               String apiCode,
                                                                               Object requestDateObj);


    /**
     * 根据上传数据cell获取多组有效期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param cellSet        cell集合
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2023/12/08
     */
    Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCells(Set<String> cellSet,
                                                                     String apiCode,
                                                                     Object requestDateObj);


    /**
     * 根据上传数据custNum获取多组有效期范围,根据taskId获取有效上传数据 Tips：定制化有效期配置使用，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param custNumSet     custNum集合
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return {@link Map }<{@link String }, {@link SyncUserValidityPeriodsBO }>
     * @author senyang.zheng
     * @date 2024/01/15
     */
    Map<String, SyncUserValidityPeriodsBO> getValidityPeriodsByCustNumAndTaskId(Set<String> custNumSet,
                                                                                String apiCode,
                                                                                Object requestDateObj);

    /**
     * 根据上传数据custNum集合+userType集合获取多组有效期范围 Tips：仅支持新版有效期规则，有效期配置valid_start_date和valid_end_date字段都非空
     *
     * @param custNumSet     custNum集合
     * @param userTypeSet    场景集合
     * @param apiCode        apiCode
     * @param requestDateObj 日期
     * @return Map key:custNum value:{@link Map<String, SyncUserValidityPeriodsBO>} key:userType value:{@link SyncUserValidityPeriodsBO}
     * @author Hua Qiang
     * @date 2024-08-27 18:50
     */
    Map<String, Map<String, SyncUserValidityPeriodsBO>> getValidityPeriodsByCustNumAndUserTypeSet(Set<String> custNumSet,
                                                                                                  Set<String> userTypeSet,
                                                                                                  String apiCode,
                                                                                                  Object requestDateObj);

    /**
     * 获取有效期的有效期配置,分页
     *
     * @param apiCode        客户编号
     * @param requestDateObj 请求时间
     * @param page           页号
     * @param pageSize       页大小
     * @return 有效期的有效期配置
     * @author Guo Zeqiang
     * @version 1.0
     * @dateTime 2024-01-09 15:30
     */
    List<MarketingDataValidConfig> getDataValidityPeriodPageList(
            String apiCode, Object requestDateObj, Integer page, Integer pageSize);

    /**
     * 指定场景获取有效期的有效期配置
     *
     * @param apiCode        客户编号
     * @param userType       场景
     * @param requestDateObj 请求时间
     * @param pageNo         页号
     * @param pageSize       页大小
     * @return 有效期的有效期配置
     * @author Guo Zeqiang
     * @version 1.0
     * @dateTime 2024-01-09 15:30
     */
    List<MarketingDataValidConfig> getDataValidityPeriodPageList(
            String apiCode, String userType, Object requestDateObj, int pageNo, int pageSize);

    /**
     * 指定场景集合获取有效期的有效期配置
     *
     * @param apiCode        客户编号
     * @param userTypeSet    场景集合
     * @param requestDateObj 请求时间
     * @param pageNo         页号
     * @param pageSize       页大小
     * @return 有效期的有效期配置
     * @author Guo Zeqiang
     * @version 1.0
     * @dateTime 2024-01-09 15:30
     */
    List<MarketingDataValidConfig> getDataValidityPeriodPageList(
            String apiCode, Set<String> userTypeSet, Object requestDateObj, int pageNo, int pageSize);

    /**
     * 指定场景获取合并重叠时间后的有效期
     *
     * @param apiCode        客户编号
     * @param userType       场景
     * @param requestDateObj 请求时间
     * @return 合并重叠时间后的有效期的有效期集合
     * @author Guo Zeqiang
     * @version 1.0
     * @dateTime 2024-01-09 15:30
     */
    List<MarketingDataValidConfig> getDataMergeValidityPeriodList(String apiCode, String userType, Object requestDateObj);
}
