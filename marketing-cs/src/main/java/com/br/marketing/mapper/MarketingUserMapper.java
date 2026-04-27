package com.br.marketing.mapper;

import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.vo.CustGroupTempVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by Bairong on 2019/8/19.
 */
@Repository
public interface MarketingUserMapper {

        /**
         * insert user
         * @param param
         */
        void insertLoanUser(Map<String,Object> param);

        /**
         * query count
         * @param blt
         * @return
         */
        Integer queryCount(MarketingTask blt);

        /**
         * query total num
         * @param incrList
         * @return
         */
        Integer getTotalNum(List<MarketingTask> incrList);

        /**
         * 查询全量任务的总数量
         * @param list
         * @return
         */
        Integer getTotalNumForAll(List<MarketingTask> list);

        /**
         * 获取user
         * @param blt
         * @return
         */
        List<MarketingUser> queryUser(MarketingTask blt);

        /**
         * 查询变动数据量
         * @param blt
         * @return
         */
        Integer queryCountForFreq(MarketingTask blt);

        /**
         * 通过客户编号查询user
         * @param param
         * @return
         */
        MarketingUser queryUserByCusNum(Map<String,Object> param);

        /**
         *  insert dirty useer
         * @param list
         */
        void insertDirtyuser(List<MarketingUser> list);

        /**
         * 查询最小id
         * @param blt
         * @return
         */
        Long queryMinId(MarketingTask blt);
        /**
         * 查询最大id
         * @param blt
         * @return
         */
        Long queryMaxId(MarketingTask blt);

        /**
         * 查询user 列表
         * @param blt
         * @return
         */
        List<MarketingUser> queryUserByid(MarketingTask blt);

        /**
         * 用户信息入库
         * @param lu 用户
         */
        void insertUser(MarketingUser lu);

        /**
         * 新建用户数据表
         * @param tableName 表名
         */
        void createUserTable(@Param("tableName") String tableName);
        /**
         * 查询最小id
         * @param blt
         * @return
         */
        int queryHnnxMinId(MarketingTask blt);
        /**
         * 查询最大id
         * @param blt
         * @return
         */
        int queryHnnxMaxId(MarketingTask blt);
        /**
         * 查询user 列表
         * @param blt
         * @return
         */
        List<MarketingUser> queryHnnxUserByid(MarketingTask blt);

        /**
         * 根据三要素查ppd临时表中的数据
         * @param param
         * @return
         */
        List<MarketingUser> queryPpd(Map<String, String> param);

        int insertBatchMarketingPreUser(@Param("apiCode") String apiCode,@Param("tasdId") String taskId
                ,@Param("dateTime")String dateTime,@Param("dataItems")List<MarketingPreUserDetailDTO> dataItems);

        int insertBatchMarketingPreUserByDatas(@Param("apiCode") String apiCode,@Param("datas") String datas);

        int insertMarketingPreUserByText(MarketingSyncInfo syncInfo);

        void insertByRequestId(@Param("apiCode") String apiCode,@Param("valuesStr")String valuesStr);

        List<CustGroupTempVO> selectGroupByCodeAndTime(@Param("apiCode") String apiCode, @Param("beginTime")String beginTime);

        List<String> selectGroupByCodeAndCusAndTime(@Param("apiCode") String apiCode,@Param("cusBatch")String cusBatch, @Param("beginTime")String beginTime);

        List<String> selectCusBatchByCodeAndTime(@Param("apiCode") String apiCode,@Param("beginTime")String beginTime,@Param("endTime")String endTime);

        void createMarketingPreUserTable(@Param("tableName") String tableName);

        void createMarketingTransferUserTable(@Param("tableName") String tableName);

        int countByPreUser(@Param("apiCode") String apiCode, @Param("cusBatch") String cusBatch, @Param("groupType") String groupType, @Param("appletDate") String appletDate);

        int countBySureUser(@Param("apiCode") String apiCode, @Param("batchNumber") String batchNumber);

        List<MarketingSyncUser> selectSyncUser(@Param("apiCode") String apiCode, @Param("requestId") String requestId);

        MarketingSyncUser selectSyncUserByCustNum(@Param("apiCode") String apiCode, @Param("custNum") String custNum, @Param("cell") String cell);

        List<MarketingSyncUser> findCustNumCellUserTypeScoreDatePage(@Param("apiCode") String apiCode
                , @Param("beginTime") String beginTime
                , @Param("endTime") String endTime
                , @Param("page") int page
        );

        void createUserLabelTable(@Param("tableName") String tableName);

        MarketingSyncUser selectLatestSyncUser(@Param("apiCode") String apiCode,
                                               @Param("custNum") String custNum,
                                               @Param("userType") String userType);
}

