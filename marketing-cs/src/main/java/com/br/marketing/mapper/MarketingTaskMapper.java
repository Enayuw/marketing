package com.br.marketing.mapper;

import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.entity.ApiCodeTask;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.CustomerBatchNumVO;
import com.br.marketing.vo.MarketingTaskVO;
import com.br.marketing.vo.ScoreDetailVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by Bairong on 2019/8/20.
 */
public interface MarketingTaskMapper extends MarketingTaskMapperBase {


    /**
     * 跑分记录列表
     * @param search
     * @param status
     * @param createTimeStart
     * @param createTimeEnd
     * @param updateTimeStart
     * @param updateTimeEnd
     * @param taskStatus
     * @return
     */
    @AddDataAuth
    List<MarketingTaskVO> selectList(@Param("search")String search, @Param("status")Integer status,
                                     @Param("createTimeStart")String createTimeStart, @Param("createTimeEnd")String createTimeEnd,
                                     @Param("updateTimeStart")String updateTimeStart, @Param("updateTimeEnd")String updateTimeEnd,
                                     @Param("taskStatus")Integer taskStatus, @Param("id")String id, @Param("execType")Integer execType);


    List<Map<String, Object>> getModule(String apiCode);

    /**
     * 查询监控种批次
     * @return
     */
    List<MarketingTask> queryBatchNum();

    /**
     * 获取任务
     * @param batchNumber
     * @return
     */
    MarketingTask queryBlt(String batchNumber);

    /**
     *修改监控状态
     * @param blt
     */
    void updateMonitorStatus(MarketingTask blt);

    /**
     * 获取任务
     * @return
     */
    List<MarketingTask> queryTask();

    /**
     * 查询配置
     * @return
     */
    List<MarketingTask> queryConfig();

    /**
     * 修改是否超检查通过
     * @param blt
     */
    void updateIsCheck(MarketingTask blt);

    /**
     * 查询api_code
     * @return
     */
    List<String> queryApiCode();

    /**
     * 通过api_code查询任务
     * @param apiCode
     * @return
     */
    List<MarketingTask> queryBltByapiCode(String apiCode);

    /**
     * 通过客户侧的批次号查询任务
     * @param apiCode
     * @return
     */
    List<String> queryBltForCusBatch(String apiCode);

    /**
     * 通过api_code
     * 查询任务
     * @param apiCode
     * @return
     */
    List<MarketingTask> queryBatchNumByapiCode(String apiCode);

    /**
     * 查询360的任务
     * @param apiCode
     * @return
     */
    List<MarketingTask> query360BatchNumByapiCode(String apiCode);


    /**
     * 查询scoredata字段
     * @param param
     * @return
     */
    String queryScoreData(Map<String,String> param);

    /**
     *查询任务列表
     * @param blt
     * @return
     */
    List<MarketingTask> queryList(MarketingTask blt);

    /**
     *查询任务条数
     * @param blt
     * @return
     */
    int queryCount(MarketingTask blt);

    /**
     *修改任务
     * @param blt
     */
    void updateTask(MarketingTask blt);

    /**删除任务
     *
     * @param param
     */
    void deleteTask(Map<String, Object> param);

    /**
     *修改任务的监控状态
     * @param param
     */
    void updateMonitorStatusForOff(Map<String, Object> param);

    /**
     *获取任务
     * @param blt
     * @return
     */
    MarketingTask getById(MarketingTask blt);

    MarketingTask getByBatchNumber(@Param("batchNumber") String batchNumber);

    /**
     *获取客户侧批次号
     * @param apiCode
     * @return
     */
    List<String> getCusBatch(String apiCode);

    /**
     *修改任务
     * @param blt
     */
    void updateBatchTask(MarketingTask blt);

    /**
     *根据客户侧批次号获取任务列表
     * @param blt
     * @return
     */
    List<MarketingTask> getByCusBatch(MarketingTask blt);

    /**
     *获取昨天上传的任务
     * @return
     */
    List<MarketingTask> queryYesterdayUploadTask();

    /**
     * 修改任务的监控数量
     * @param lt 任务信息
     */
    void updateTaskActualNumber(MarketingTask lt);

    /**
     * 查询监控中的批次信息
     * @param apiCode
     * @return
     */
    List<MarketingTask> queryMonitorBatch(String apiCode);

    /**
     * 插入任务信息
     * @param lt 任务对象
     */
    void insertTask(MarketingTask lt);
    /**
     * 修改任务信息
     * @param lt 任务对象
     */
    void modifyTask(MarketingTask lt);

    void modifyTaskActualNum(MarketingTask lt);

    /**
     * 查询监控截止日期为当前日期的任务
     */
    List<ApiCodeTask> queryCloseBlt(String date);
    /**
     * 查询监控截止日期为当前日期的任务
     */
    List<ApiCodeTask> queryCloseBltSoon(String date);

    @AddDataAuth
    List<ScoreDetailVo> queryBatchs(CustomerBatchNumDTO dto);

    @AddDataAuth
    Long queryBatchsCount(CustomerBatchNumDTO dto);

    MarketingTask selectCycleTopByApiCode(String apiCode);

    List<MarketingTask> getScoreTasks(@Param("date") String date, @Param("taskId") Long taskId, @Param("hm") String hm);

    Integer selectByPriority(Integer priority);

    String selectHisFileById(String hisFileId);

    @AddDataAuth
    List<ScoreDetailVo> queryBatchList(CustomerBatchNumVO batchNumVO);

    List<MarketingTaskVO> queryCompletStatus(@Param("apiCode")String apiCode, @Param("createTimeStart")String createTimeStart,
                                             @Param("createTimeEnd")String createTimeEnd, @Param("taskStatus")Integer taskStatus,
                                             @Param("conditionType")Integer conditionType, @Param("ruleNameShort")String ruleNameShort);

    /**
     * 通过apiCode查询最后一个非验证任务
     *
     * @param apiCode     apiCode
     * @param batchNumber 任务编号
     * @return {@link List }<{@link MarketingTask }>
     * @author senyang.zheng
     * @date 2024/10/21
     */
    MarketingTaskVO queryLastNonValidationTask(@Param("apiCode") String apiCode, @Param("batchNumber") String batchNumber);



    List<MarketingTaskVO> queryNoFinishStatus(@Param("apiCode")String apiCode, @Param("createTimeStart")String createTimeStart,
                                             @Param("createTimeEnd")String createTimeEnd);


    MarketingTaskVO getByFileId(@Param("fileId") Long fileId);

    /**
     * 跨天恢复跑分时，将任务生效日期窗口对齐到当天（与运维手工改 start_date/close_date 一致）
     */
    int updateDateWindowByBatchNumber(@Param("batchNumber") String batchNumber,
                                      @Param("startDate") String startDate,
                                      @Param("closeDate") String closeDate);
}
