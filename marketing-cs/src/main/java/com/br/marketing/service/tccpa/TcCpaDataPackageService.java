package com.br.marketing.service.tccpa;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.tccpa.TcCpDataCleanTaskDTO;
import com.br.marketing.dto.tccpa.TcCpDataPackageGenDTO;
import com.br.marketing.dto.tccpa.TcyrCpaCollidingDataPackageVO;
import com.br.marketing.entity.TcyrCpaCollidingDataPackage;
import com.br.marketing.entity.auth.MarketingUserDetail;

public interface TcCpaDataPackageService {

    /**
     * 规则中心 同程CPA跑分待清洗数据包生成
     * @param dto
     * @return
     */
    Result tcDataPackageGen(TcCpDataPackageGenDTO dto);


    /**
     * 同程CPA跑分待清洗数据包列表
     * @param page 当前页
     * @param pageSize 每页数量
     * @param packageName 包名称
     * @param status 状态
     * @return
     */
    PageResultReturn page(int page, int pageSize, String packageName, Integer status);

    /**
     * 数据包修改
     * @param dataPackage
     */
    Result update(TcyrCpaCollidingDataPackageVO dataPackage);

    /**
     * 同程数据包删除
     * @param id
     * @return
     */
    Result delete(Long id);

    /**
     * 同程数据包清洗任务新增
     * @return
     */
    Result genCleanTask();
}
