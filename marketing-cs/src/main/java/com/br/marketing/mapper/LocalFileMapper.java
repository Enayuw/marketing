package com.br.marketing.mapper;



import com.br.marketing.entity.LocalFile;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.LocalFileVo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface LocalFileMapper extends LocalFileMapperBase {

    Integer insertFileData(@Param("insertSql") String insertSql);

    @AddDataAuth
    List<LocalFileVo> selectList(@Param("search")String search,
                                 @Param("apiCode") String apiCode,
                                 @Param("uploadStartTime") String uploadStartTime,
                                 @Param("uploadEndTime") String uploadEndTime,
                                 @Param("fileType") String fileType);

    @AddDataAuth
    Integer allCount(@Param("search") String search,
                     @Param("apiCode") String apiCode,
                     @Param("uploadStartTime") String uploadStartTime,
                     @Param("uploadEndTime") String uploadEndTime,
                     @Param("fileType") String fileType);

    void updateUploadStartTimeById(@Param("ids") List<Long> ids, @Param("date") Date date);

    LocalFile getByPrimaryKey(@Param("id") Long id);

    List<LocalFile> getLocalFileByPushNoOrError(@Param("apiCode") String apiCode,@Param("fileType") String fileType);

    List<LocalFile> getNotPushLocalFileByFileTypeAndFileName(@Param("fileName") String fileName,@Param("fileType") String fileType);
}