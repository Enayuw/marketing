package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CustomerBatchNumDTO;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.TaskStatus;
import com.br.marketing.vo.ScoreDetailVo;

import java.util.List;

public interface MarketingTaskOptService {
    Result pauseTask(Long fileId, Integer isOrPause);

    Result pauseTaskByStraHisFile(Integer pauseType, StraHisFile straHisFile, TaskStatus taskStatus);


    /**
     * 规则中心筛选批次列表（评分产品析出字段）
     *
     * @param dto vo
     * @return List<ScoreDetailVo>
     */
    PageResultReturn<List<ScoreDetailVo>> getBatchInfoFieldList(CustomerBatchNumDTO dto);
}
