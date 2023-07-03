package com.br.marketing.mapper;


import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.vo.TransferOfCnIdVO;
import com.br.marketing.vo.TransferOfRdRFVO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface MarketingTransferSyncUserMapper extends MarketingTransferSyncUserMapperBase {
    /**
     * 根据cust_num获取最新数据
     *
     * @param cid
     * @param caseNum
     * @return
     */
    MarketingTransferSyncUser getNewestByCusnum(@Param("cid") String cid, @Param("caseNum") String caseNum);

    /**
     * 根据cust_num、Apicode获取1h内最新数据
     */
    MarketingTransferSyncUser getNewestByCusnumAndApicode(@Param("cid") String cid, @Param("caseNum") String caseNum, @Param("apicode") String apicode
            , @Param("userType") String userType, @Param("timeAddHour") String timeAddHour);

    /**
     * 根据cust_num获取1小时内最新数据
     *
     * @param cid
     * @param caseNum
     * @param timeAddHour
     * @return
     */
    MarketingTransferSyncUser getNewestByCusnumInHour(@Param("cid") String cid, @Param("caseNum") String caseNum, @Param("timeAddHour") String timeAddHour);

    List<MarketingTransferSyncUser> getTransferOrderInsertTime(@Param("cid") String cid, @Param("data") String data, @Param("limitStart") Integer limitStart);

    List<MarketingTransferSyncUser> getTransferDataByRequestDataAndApiCode(@Param("cid") String cid, @Param("apiCode") String apiCode, @Param("data") String data, @Param("limitStart") Integer limitStart);

    List<MarketingTransferSyncUser> getTransferByRequestData(@Param("cid") String cid, @Param("endDate") String endDate, @Param("limitStart") Integer limitStart);

    List<MarketingTransferSyncUser> getTransferData(@Param("cid") String cid, @Param("endDate") String endDate, @Param("limitStart") Integer limitStart);

    /**
     * 根据ApplyDt数据统计
     *
     * @param cid
     * @param apiCode
     * @param limitStart
     * @return
     */
    List<MarketingTransferSyncUser> getTransferByApplyDt(@Param("cid") String cid, @Param("apiCode") String apiCode, @Param("limitStart") Integer limitStart,
                                                         @Param("startDay") String startDay, @Param("endDay") String endDay);

    /**
     * 获取指定日期，指定custNum的非延时数据
     *
     * @param cid
     * @param custNums
     * @param date
     * @return
     */
    List<MarketingTransferSyncUser> getTransferOrderInsertTimeByCustNum(@Param("cid") String cid, @Param("custNums") List<String> custNums, @Param("date") String date);

    /**
     * 获取指定custNum的最新数据
     *
     * @param cid
     * @param custNums
     * @param date
     * @return
     */
    List<MarketingTransferSyncUser> getTransferOrderRequestTimeByCustNum(@Param("cid") String cid, @Param("custNums") List<String> custNums, @Param("date") String date);

    /**
     * 数禾转化数据提取，按场景
     *
     * @param tCid    cid
     * @param sqlPart sql片段
     * @return {@link MarketingTransferSyncUser}
     * @author Guo Zeqiang
     * @dateTime 2022/4/15 11:43
     */
    List<MarketingTransferSyncUser> findShuHeTransferList(@Param("tCid") String tCid, @Param("sqlPart") String sqlPart);

    /**
     * 根据apiCode,create_time获取数据
     *
     * @param startDate
     * @param endDate
     * @param apiCode
     * @return
     */
    List<MarketingTransferSyncUser> getTransferByApiCodeAndCreateTime(@Param("apiCode") String apiCode, @Param("tCid") String tcId, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("minId") Long minId);

    /**
     * 根据获取分页数据
     *
     * @param transferSyncUser pojo
     * @param startTime        开始时间
     * @param endTime          结束
     * @param rowCount         行数
     * @param offset           步长
     * @return List
     * @author Guo Zeqiang
     * @dateTime 2022/5/27 11:43
     */
    List<MarketingTransferSyncUser> findTransferByApiCodeAndCreateTimePage(
            @Param("transferSyncUser") MarketingTransferSyncUser transferSyncUser
            , @Param("startTime") String startTime
            , @Param("endTime") String endTime
            , @Param("orderByClause") String orderByClause
            , @Param("rowCount") int rowCount
            , @Param("offset") int offset);

    /**
     * 过滤转化表实时数据&&上传表案件状态为有效的数据
     *
     * @param tcId
     * @param apiCode
     * @param limitStart
     * @return
     */
    List<MarketingTransferSyncUser> getTransferByTransformTypeAndStatus(@Param("tcId") String tcId, @Param("apiCode") String apiCode, @Param("endDate") String endDate, @Param("limitStart") Integer limitStart);

    /**
     * 过滤caseEffecctive=0的案件编号
     *
     * @param tcId
     * @param apiCode
     * @param set
     * @return
     */
    List<MarketingTransferSyncUser> getByInCustAndCaseEffective(@Param("tcId") String tcId, @Param("apiCode") String apiCode, @Param("custs") Set<String> set);

    /**
     * 获取request_data = eliminateDate下 applyLoan = 0的数据
     *
     * @param tcId
     * @param eliminateDate
     * @return
     */
    List<MarketingTransferSyncUser> getCustNumByApplyLoan(@Param("tcId") String tcId, @Param("apiCode") String apiCode, @Param("eliminateDate") LocalDate eliminateDate);

    List<MarketingTransferSyncUser> getTransferUserByCreateTimeOrder(@Param("tcId") String tcId, @Param("apiCode") String apiCode
            , @Param("startTime") String startTime, @Param("endTime") String endTime
            , @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize);

    /**
     * 根据
     * apiCode
     * applyResult
     * requestData
     * applyDt
     * 获取分页数据
     *
     * @param tcId        cid
     * @param apiCode     code
     * @param applyResult 审批结果
     * @param requestDate 客户请求日期
     * @param applyDt     进件时间yyyy-mm-dd hh:mm:ss:SSS
     * @param rowCount    行数
     * @param offset      步长
     * @return List
     * @author Guo Zeqiang
     * @dateTime 2022/9/21 13:43
     */
    List<MarketingTransferSyncUser> findByApplyResultAndRequestDataAndApplyDtPage(
            @Param("tcId") String tcId
            , @Param("apiCode") String apiCode
            , @Param("applyResult") String applyResult
            , @Param("requestDate") String requestDate
            , @Param("applyDt") String applyDt
            , @Param("rowCount") int rowCount
            , @Param("offset") int offset);

    /**
     * 根据
     * apiCode
     * requestData
     * applyDt
     * caseEffective
     * custNumSet
     * 获取案件集合数据
     *
     * @param tcId          cid
     * @param apiCode       code
     * @param requestDate   客户请求日期
     * @param applyDt       进件时间yyyy-mm-dd hh:mm:ss:SSS
     * @param caseEffective 进件时间yyyy-mm-dd hh:mm:ss:SSS
     * @param custNumSet    案件集合
     * @return List
     * @author Guo Zeqiang
     * @dateTime 2022/9/21 13:43
     */
    Set<String> getCustNumSet(
            @Param("tcId") String tcId
            , @Param("apiCode") String apiCode
            , @Param("requestDate") String requestDate
            , @Param("applyDt") String applyDt
            , @Param("caseEffective") String caseEffective
            , @Param("custNumSet") Set<String> custNumSet
    );


    /**
     * 获取桔子D规则的转化数据
     *
     * @param tcId
     * @param requestData
     * @param minId
     * @return
     */
    List<MarketingTransferSyncUser> getJuZiDRuleTransferData(@Param("tCid") String tcId, @Param("requestData") String requestData, @Param("minId") Long minId);


    /**
     * 获取桔子C规则的转化数据
     *
     * @param tcId
     * @param requestData
     * @param minId
     * @return
     */
    List<MarketingTransferSyncUser> getJuZiCRuleTransferData(@Param("tCid") String tcId, @Param("requestData") String requestData, @Param("minId") Long minId);

    /**
     * 获取桔子B规则的转化数据
     *
     * @param tcId
     * @param registerTime
     * @param minId
     * @return
     */
    List<MarketingTransferSyncUser> getJuZiBRuleTransferData(@Param("tCid") String tcId, @Param("requestData") String requestData, @Param("registerTime") String registerTime, @Param("minId") Long minId);

    /**
     * 获取桔子A规则的转化数据
     *
     * @param tcId
     * @param loginTime
     * @param minId
     * @return
     */
    List<MarketingTransferSyncUser> getJuZiARuleTransferData(@Param("tCid") String tcId, @Param("requestData") String requestData, @Param("loginTime") String loginTime, @Param("minId") Long minId);

    /**
     * 获取桔子D规则的锁定期数据
     *
     * @param tcId
     * @param lentTime
     * @param custNums
     * @return
     */
    List<String> getJuZiDRuleLockData(@Param("tCid") String tcId, @Param("lentTime") String lentTime, @Param("custNums") Set<String> custNums);

    /**
     * 获取桔子C规则的锁定期数据
     *
     * @param tcId
     * @param applyLoanTime
     * @param custNums
     * @return
     */
    List<String> getJuZiCRuleLockData(@Param("tCid") String tcId, @Param("applyLoanTime") String applyLoanTime, @Param("custNums") Set<String> custNums);

    /**
     * 获取桔子B规则或A规则的锁定期数据
     *
     * @param tcId
     * @param applyDt
     * @param custNums
     * @return
     */
    List<String> getJuZiBOrARuleLockData(@Param("tCid") String tcId, @Param("applyDt") String applyDt, @Param("custNums") Set<String> custNums);

    /**
     * 获取转化数据的数据量
     *
     * @param tcId
     * @param requestDate
     * @return
     */
    int getTransferDataCount(@Param("tcId") String tcId, @Param("apiCode") String apiCode, @Param("requestData") String requestDate);


    List<MarketingTransferSyncUser> getConvtypeData(@Param("tcId") String tcId, @Param("limitStart") Integer limitStart, @Param("requestData") String requestDate, @Param("convType") String convType);

    List<String> getCustNumAndConvtypeData(@Param("tcId") String tcId, @Param("custNums") List<String> custNums, @Param("requestData") String requestDate, @Param("convTypeStr") String convTypeStr);

    List<MarketingTransferSyncUser> getRsToPolicyData(@Param("requestDate") String requestDate, @Param("tcId") String tcId
            , @Param("userTypes") List<String> userTypes
            , @Param("ifActivity") String ifActivity, @Param("activityTime") String activityTime
            , @Param("ifApply") String ifApply, @Param("applyDt") String applyDt
            , @Param("minId") Long minId, @Param("pageSize") Integer pageSize);


    /**
     * 获取桔子A规则的转化数据
     *
     * @param tcId
     * @return
     */
    List<MarketingTransferSyncUser> getJuZiARuleData(@Param("tCid") String tcId, @Param("apiCode") String apiCode, @Param("minId") Long minId);

    List<MarketingTransferSyncUser> getJuZiCRuleData(@Param("tCid") String tcId, @Param("apiCode") String apiCode, @Param("minId") Long minId);

    List<MarketingTransferSyncUser> getJuZiDRuleData(@Param("tCid") String tcId, @Param("apiCode") String apiCode, @Param("minId") Long minId);

    List<MarketingTransferSyncUser> getJuZiBRuleData(@Param("tCid") String tcId, @Param("apiCode") String apiCode, @Param("minId") Long minId);

    List<MarketingTransferSyncUser> getValidityPeriodData(@Param("tCid") String tcId, @Param("apiCode") String apiCode, @Param("custNum") String custNum);

    List<TransferOfRdRFVO> getTransferOfRdRFs(@Param("requestDate") String requestDate, @Param("custNums") List<String> custNums
            , @Param("tcId") String tcId, @Param("apiCode") String apiCode);

    List<String> getTransferCustNumsRangReqDateByPage(@Param("tCid") String tcId, @Param("apiCode") String apiCode
            , @Param("begingDate") String begingDate, @Param("endDate") String endDate
            , @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSiz);

    List<TransferOfCnIdVO> getTransferReqDateAndIdByPage(@Param("tCid") String tcId, @Param("apiCode") String apiCode
            , @Param("requestDate") String requestDate, @Param("minId") Long minId);

    List<MarketingTransferSyncUser> getTransferByRequestDate(@Param("tCid") String tcId, @Param("apiCode") String apiCode
            , @Param("requestDate") String requestDate, @Param("minId") Long minId);

    List<MarketingTransferSyncUser> getTransferByCustNumOrderDatatikv_(@Param("tCid") String tcId,@Param("custNums") List<String> custNums);

    List<MarketingTransferSyncUser> getYxTransferByApiCodeBtoCtoItikv_(@Param("tCid") String tcId,
                                                                       @Param("apiCode") String apiCode,
                                                                       @Param("requestData") String requestData,
                                                                       @Param("type") String type,
                                                                       @Param("minId") Long minId);

    List<MarketingTransferSyncUser> getYxTransferByApiCodeAtikv_(@Param("cid") String cid,
                                                                  @Param("apiCode") String apiCode,
                                                                  @Param("yesterday") String yesterday,
                                                                  @Param("minId") Long minId);

    List<String> getExcludeRuleFirstYxTransferByApiCodetikv_(@Param("cid") String cid,
                                                             @Param("apiCode") String apiCode,
                                                             @Param("today") String today,
                                                             @Param("custNums") List<String> custNums);

    List<String> getExcludeRuleSecondYxTransferByApiCodetikv_(@Param("cid") String cid, @Param("apiCode") String apiCode,
                                                              @Param("custNums") List<String> custNums);

    List<String> getRuleFifthYxTransferByApiCodetikv_(@Param("cid") String cid,
                                                      @Param("apiCode") String apiCode,
                                                      @Param("queryDate") String queryDate,
                                                      @Param("custNums") List<String> custNums);

    Long getYiXinMinAtoBtoCtoI(@Param("cid") String cid,
                               @Param("apiCode") String apiCode,
                               @Param("requestData") String requestData,
                               @Param("type") String type);
}