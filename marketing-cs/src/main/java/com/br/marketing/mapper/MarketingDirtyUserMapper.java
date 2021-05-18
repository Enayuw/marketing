package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingUser;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingDirtyUserMapper extends MarketingDirtyUserMapperBase {
    /**
     * 根据客户上传的剔除信息，查询监控中的数据
     * @param user
     * @return
     */
    List<MarketingUser> queryUser(MarketingUser user);

    /**
     * 查询黑名单表
     * @param user
     * @return
     */
    List<MarketingUser> queryDirtyUser(MarketingUser user);
    /**
     * 修改监控中的样本的状态为剔除
     * @param updateList
     */
    void updateUser(List<MarketingUser> updateList);

    /**
     * 插入一条记录到剔除列表中
     * @param insertList
     */
    void insertDirtyUser(List<MarketingUser> insertList);
}
