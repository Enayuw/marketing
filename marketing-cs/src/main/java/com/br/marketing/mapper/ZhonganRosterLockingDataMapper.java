package com.br.marketing.mapper;


import com.br.marketing.dto.SftpFilePushSuccessDTO;
import com.br.marketing.entity.ZhonganRosterLockingData;
import com.br.marketing.monkeydata.query.ZhongAnMobileMd5BizDateQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;


@Mapper
public interface ZhonganRosterLockingDataMapper extends ZhonganRosterLockingDataMapperBase {

    /**
     * 2022/11/14 14:56
     * 对手机号分组，根据条件分页获取满足条件的数据
     *
     * @param data     过滤条件
     * @param pageNo   页号，从0页开始
     * @param pageSize 页面大小
     * @return List
     */
    List<ZhonganRosterLockingData> findGroupMobileMd5ListPage(@Param("data") ZhonganRosterLockingData data
            , @Param("pageNo") int pageNo
            , @Param("pageSize") int pageSize);

    /**
     * 2022/11/14 15:39
     * 获取已推送的手机号
     *
     * @param queries 条件集合
     * @param apiCode apicode
     * @param tag     枚举,CG/MG
     * @return Set
     */
    List<ZhonganRosterLockingData> getMobileMd5ByBeforePush(@Param("queries") List<ZhongAnMobileMd5BizDateQuery> queries
            , @Param("apiCode") String apiCode
            , @Param("tag") String tag);

    /**
     * 2022/11/16 16:54
     * 更新推送状态
     */
    void updatePushStatusOrStatus(@Param("apiCode") String apiCode
            , @Param("updatePushStatus") Integer updatePushStatus
            , @Param("updateStatus") Integer updateStatus
            , @Param("pushStatus") Integer pushStatus
            , @Param("tag") String tag
            , @Param("dataList") List<ZhonganRosterLockingData> dataList
            , @Param("dateStr") String dateStr
            , @Param("date") Date date
    );

    /**
     * 2022/11/16 16:54
     * 统计文件推送成功数据量
     */
    List<SftpFilePushSuccessDTO> getSftpFilePushSuccessSum(@Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr);

    /**
     * 2022/11/16 16:54
     * 获取推送文件id
     */
    List<Long> getSftpFileIdList(@Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr);

    List<Long> getSftpFileIdListByTags(@Param("apiCode") String apiCode, @Param("tags") List<String> tags);

    List<Long> getSftpFileIdListByNoTags(@Param("apiCode") String apiCode, @Param("dateStr") String dateStr
            , @Param("tags") List<String> tags);

    List<String> getTagByApiCodeBizDateList(@Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr);

    List<String> getTagsByApiCodeTagList(@Param("apiCode") String apiCode
            , @Param("tags") List<String> tags);

    List<String> getTagsByApiCodeBizDateNoTagList(@Param("apiCode") String apiCode, @Param("dateStr") String dateStr
            , @Param("tags") List<String> tags);

    /**
     * 2023/05/18 14:56
     * 获取需要推送的日期
     *
     * @param data 过滤条件
     * @return Set
     */
    Set<String> getBizDateListtikv_(@Param("data") ZhonganRosterLockingData data);

    /**
     * 2022/11/14 14:56
     * 分页获取部分字段数据集合
     *
     * @param data     过滤条件
     * @param pageNo   页号，从0页开始
     * @param pageSize 页面大小
     * @return List
     */
    List<ZhonganRosterLockingData> findPartColumnListPage(@Param("data") ZhonganRosterLockingData data
            , @Param("pageNo") int pageNo
            , @Param("pageSize") int pageSize);

    /**
     * 2023/05/29 14:56
     * 获取主键
     *
     * @return List
     */
    List<ZhonganRosterLockingData> getDuplicateMobileMd5List(@Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr, @Param("tag") String tag, @Param("mobileMd5") String mobileMd5);
}