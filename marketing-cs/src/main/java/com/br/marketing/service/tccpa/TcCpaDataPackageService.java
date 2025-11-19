package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.entity.TcyrCpaCollidingDataCleanTask;

public interface TcCpaDataPackageService {

    /**
     * 规则中心 同程CPA跑分待清洗数据包生成
     * @param dto
     * @return
     */
    Result tcDataPackageGen(TcCpDataPackageGenDTO dto);

    /**
     * 同程数据包删除
     * @param dto
     * @return
     */
    Result delete(TcCpDataPackageGenDTO dto);

    /**
     * 同程数据包 启用禁用
     * @param packageName 数据包名称
     * @param status 状态：1启用 0禁用
     * @return
     */
    Result enable(String packageName, Integer status);

    /**
     * 同程数据包清洗任务新增
     * @param task
     * @return
     */
    Result genCleanTask(TcCpDataCleanTaskDTO task);
}
