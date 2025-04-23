package com.br.marketing.mapper;

import com.br.marketing.entity.QueryUserRealMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface QueryUserRealMessageMapper extends QueryUserRealMessageMapperBase{

    /**
     * 更新状态
     * @param status status
     * @param idList 待更新id集合
     */
    void updateStatusByIdList(@Param("status") Integer status, @Param("idList") List<Long> idList);


    List<QueryUserRealMessage> selectUserMessageByStatus(@Param("apiCode")String apiCode, @Param("createDate")String createDate,@Param("uploadUpdateStatus") Integer uploadUpdateStatus,
                                                         @Param("esUpdateStatus")Integer esUpdateStatus, @Param("appletDate")String appletDate,  @Param("userType")String userType,
                                                         @Param("indexId")Long indexId, @Param("pageSize")Integer pageSize);


    List<Map<String,String>> selectAppletDataAndType(@Param("apiCode")String apiCode, @Param("createDate")String createDate);

    /**
     * 更新状态
     * @param status status
     * @param idList 待更新id集合
     */
    void updateUploadStatusByIdList(@Param("status") Integer status, @Param("idList") List<Long> idList);

    /**
     * 更新状态
     * @param status status
     * @param idList 待更新id集合
     */
    void updateEsStatusByIdList(@Param("status") Integer status, @Param("idList") List<Long> idList);

    List<Map<String,String>> selectAppletDataByUpload(@Param("apiCode")String apiCode, @Param("createDate")String createDate);

    /**
     * 批量插入用户消息
     * @param list 用户消息列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<QueryUserRealMessage> list);


}
