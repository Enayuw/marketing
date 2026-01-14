package com.br.marketing.mapper;



import com.br.marketing.entity.LocalFile;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.LocalFileVo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface LocalFileMapper extends LocalFileMapperBase {

    /**
     * insert
     * @param insertSql insertSql
     * @return java.lang.Integer 插入的数据量
     */
    Integer insertFileData(@Param("insertSql") String insertSql);

    /**
     * 查询
     * @param search search
     * @param apiCode apiCode
     * @param uploadStartTime uploadStartTime
     * @param uploadEndTime uploadEndTime
     * @param fileType fileType
     * @return java.util.List<com.br.marketing.vo.LocalFileVo> 查询结果
     */
    @AddDataAuth
    List<LocalFileVo> selectList(@Param("search")String search,
                                 @Param("apiCode") String apiCode,
                                 @Param("uploadStartTime") String uploadStartTime,
                                 @Param("uploadEndTime") String uploadEndTime,
                                 @Param("fileType") String fileType);

    /**
     * 更新
     * @param search search
     * @param apiCode apiCode
     * @param uploadStartTime uploadStartTime
     * @param uploadEndTime uploadEndTime
     * @param fileType fileType
     * @return java.lang.Integer 数据量
     */
    @AddDataAuth
    Long allCount(@Param("search") String search,
                     @Param("apiCode") String apiCode,
                     @Param("uploadStartTime") String uploadStartTime,
                     @Param("uploadEndTime") String uploadEndTime,
                     @Param("fileType") String fileType);

    /**
     * 更新
     * @param ids ids
     * @param date date
     */
    void updateUploadStartTimeById(@Param("ids") List<Long> ids, @Param("date") Date date);

    /**
     * 查询
     * @param id id
     * @return com.br.marketing.entity.LocalFile 查询到的对象
     */
    LocalFile getByPrimaryKey(@Param("id") Long id);

    /**
     * 查询
     * @param apiCode apiCode
     * @param fileType fileType
     * @return java.util.List<com.br.marketing.entity.LocalFile> 查询到的结果
     */
    List<LocalFile> getLocalFileByPushNoOrError(@Param("apiCode") String apiCode,@Param("fileType") String fileType);

    /**
     * 查询
     * @param fileName fileName
     * @param fileType fileType
     * @return java.util.List<com.br.marketing.entity.LocalFile> 查询到的结果
     */
    List<LocalFile> getNotPushLocalFileByFileTypeAndFileName(@Param("fileName") String fileName,@Param("fileType") String fileType);

    /**
     * 更新
     * @param localFile localFile
     */
    void updatePushNumber(@Param("localFile") LocalFile localFile);

    void updatePushStatusByLocalId(@Param("highValueIds") String highValueIds);

    /**
     * 获取大于指定时间 apiCode的最新下载完成的记录
     * @param apiCode
     * @param startTime
     * @return
     */
    List<LocalFile> getLastDataByApiCode(@Param("apiCode") String apiCode,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime")LocalDateTime endTime);

    void updatePushEndTimeById(@Param("id")Long id, @Param("pushNumber")int pushNumber, @Param("date")Date date);
}