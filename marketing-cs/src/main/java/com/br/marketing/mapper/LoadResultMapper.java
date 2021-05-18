package com.br.marketing.mapper;

import com.br.marketing.entity.LoadResult;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface LoadResultMapper extends LoadResultMapperBase {
    /**查询上传文件成功或者失败的记录
     * @param param 入参api_code batch_number
     * @return
     */
    List<LoadResult> queryLoadResult(Map<String, String> param);

    /**
     * 插入文件上传记录
     * @param lr
     */
    void insertLoadResult(LoadResult lr);
}
