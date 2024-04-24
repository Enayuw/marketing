package com.br.marketing.innerapi.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.vo.xiecheng.XiechengCollidingDataVO;

import java.util.List;

/**
 * 规则中心-撞库筛选相关接口
 */
public interface RuleCenterCollidingService {


    Result<List<XiechengCollidingDataVO>> getCollidingResultData(String apiCode);
}
