package com.br.marketing.mapper;


import com.br.marketing.dos.PeriodOfValidityDO;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransfer;
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import com.br.marketing.vo.TransferUserVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface MarketingSyncInfoMapper extends MarketingSyncInfoMapperBase {
    List<String> getCusBatchByApiAndTime(@Param("apiCode") String apiCode,@Param("beginTime") String beginTime,@Param("endTime") String endTime);

    Long minSyncId(@Param("apiCode") String apiCode,@Param("cusBatch")String cusBatch,@Param("beginTime") String beginTime,@Param("endTime") String endTime);

    List<MarketingSyncInfo> getDatalimit(@Param("apiCode") String apiCode,@Param("cusBatch")String cusBatch
                                         ,@Param("id") Long id,@Param("beginTime") String beginTime
                                         ,@Param("endTime") String endTime);

    void createMarketingTransferTable(@Param("tableName") String tableName);

    Integer insertBatchTransfer(@Param("execSql") String execSql);

    int insertTransfer(MarketingTransfer transfer);

    Integer selectTransfersByRequestId(@Param("apiCode") String apiCode,@Param("requestId") String requestId);

    List<MarketingSyncUser> getPreUserByTaskAndCust(@Param("apiCode") String apiCode,@Param("conditionTextByTaskIdAndCust")String conditionTextByTaskIdAndCust);

    Long countRepeat(@Param("execSql") String execSql);

    TodayIdTimeBySoleVo getSoleValidUser(@Param("execSql") String execSql);

    Integer updateRepeatUserStatus(@Param("execSql") String execSql);

    Long getMinIdByRuleScore(@Param("apiCode")String apiCode
            ,@Param("sTimeStr")String sTimeStr,@Param("eTimeStr")String eTimeStr
            ,@Param("whereStr")String whereStr);

    Long getMaxIdByRuleScore(@Param("apiCode")String apiCode
            , @Param("sTimeStr") String sTimeStr, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    Integer countByPreUserWithRule(@Param("apiCode") String apiCode
            , @Param("sTimeStr") String sTimeStr, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    List<MarketingSyncUser> getSyncUserByRuleScore(@Param("apiCode") String apiCode
            , @Param("sTimeStr") String sTimeStr
            , @Param("eTimeStr") String eTimeStr, @Param("minId") Long minId, @Param("maxId") Long maxId, @Param("whereStr") String whereStr);

    List<MarketingSyncUser> getPreUserByInCust(@Param("apiCode") String apiCode, @Param("custs") Set<String> custs);

    Integer countTransferFile(@Param("apiCode") String apiCode, @Param("beginTime") String beginTime
            , @Param("endTime") String endTime, @Param("groupType") String groupType, @Param("fileTypes") List<String> fileTypes);

    List<TransferUserVO> getTransferFileUser(@Param("apiCode") String apiCode, @Param("beginTime") String beginTime
            , @Param("endTime") String endTime, @Param("minId") Long minId, @Param("groupType") String groupType, @Param("fileTypes") List<String> fileTypes);

    List<MarketingSyncUser> getSyncUserByTaskAndCust(@Param("apiCode") String apiCode, @Param("taskIds") List<String> taskIds, @Param("custNums") List<String> custNums);

    /**
     * 获取有效期时间内的数量
     *
     * @param apiCode            apiCode
     * @param custNum            案件编号
     * @param periodOfValidityDO 有效期pojo
     * @return 数量
     * @author Guo Zeqiang
     * @dateTime 2022/2/14 9:55
     */
    long getPeriodOfValiditySum(@Param("apiCode") String apiCode, @Param("custNum") String custNum, @Param("periodOfValidityDO") PeriodOfValidityDO periodOfValidityDO);
}