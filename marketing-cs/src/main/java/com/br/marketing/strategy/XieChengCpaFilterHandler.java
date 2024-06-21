package com.br.marketing.strategy;

import java.util.List;

import javax.annotation.Resource;

import cn.hutool.core.collection.CollectionUtil;
import com.google.api.client.util.Lists;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XiechengCollidingDataEliminationDTO;
import com.br.marketing.entity.XiechengCollidingDataElimination;
import com.br.marketing.mapper.XiechengCollidingDataEliminationMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 携程CPA撞库过滤数据落库处理策略(定制)
 *
 * @author senyang.zheng
 * @date 2024/06/18
 */
@Slf4j
@Service
public class XieChengCpaFilterHandler extends AbstractExternalInterfaceHandler<XiechengCollidingDataEliminationDTO> {

    @Resource
    private XiechengCollidingDataEliminationMapper eliminationMapper;

    @Override
    JSONObject call(List<XiechengCollidingDataEliminationDTO> transferData, ProcessHandlerContext context) {
        List<XiechengCollidingDataElimination> eliminations = Lists.newArrayList();
        for (XiechengCollidingDataEliminationDTO dto : transferData) {
            eliminations.add(dto.getXiechengCollidingDataElimination());
        }
        if(CollectionUtil.isNotEmpty(eliminations)){
            eliminationMapper.batchSave(eliminations);
        }
        return null;
    }

    /**
     * 按照三方接口逻辑调用接口
     */
    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.XIE_CHENG_CPA_FILTER_INSERT_DB;
    }
}
