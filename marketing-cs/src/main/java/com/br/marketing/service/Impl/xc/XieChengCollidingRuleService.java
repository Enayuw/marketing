package com.br.marketing.service.Impl.xc;

import java.util.List;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.rulecenter.XcDeleteTaskQueryDTO;
import com.br.marketing.dto.rulecenter.XcDeleteTaskVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingStagingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengPackageVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleConfirmParam;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingRuleParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingSwitchParam;
import com.br.marketing.vo.xiecheng.param.UpdatePriorityParam;
import com.br.marketing.vo.xiecheng.param.UpdateRoundParam;

public interface XieChengCollidingRuleService {
    /**
     * 获取调度任务列表-False-分页
     *
     * @param listParam 列表参数
     * @return {@link PageResultReturn }<{@link XiechengCollidingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/23
     */
    PageResultReturn<XiechengCollidingRuleVO> getCollidingRuleFalseList(CollidingRuleListParam listParam);

    /**
     * 获取调度任务列表-True-不分页
     *
     * @param listParam 列表参数
     * @return {@link List }<{@link XiechengCollidingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    List<XiechengCollidingRuleVO> getCollidingRuleTrueList(CollidingRuleListParam listParam);

    /**
     * 修改包优先级
     *
     * @param param param
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    Boolean updatePriority(UpdatePriorityParam param);

    /**
     * 获取携程撞库规则详情
     *
     * @param dprId dpr id
     * @return {@link XiechengCollidingRuleVO }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    XiechengCollidingRuleVO getCollidingRuleDetail(Long dprId);

    /**
     * 更新撞库规则
     *
     * @param param 更新参数
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/24
     */
    Boolean updateCollidingRule(UpdateCollidingRuleParam param);

    /**
     * 变更任务状态
     *
     * @param param param
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    Boolean updateCollidingSwitch(UpdateCollidingSwitchParam param);

    /**
     * 删除撞库规则
     *
     * @param dprIds dpr ids
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    Boolean deleteCollidingRules(String dprIds);

    /**
     * 获取撞库数据包下拉列表-不分页
     *
     * @return {@link List }<{@link XiechengPackageVO }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    List<XiechengPackageVO> getPackageList();

    /**
     * 确认/暂存 撞库规则
     *
     * @param confirmParam 确认参数
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/24
     */
    Long  confirmCollidingRule(CollidingRuleConfirmParam confirmParam);

    /**
     * 获取暂存规则列表
     *
     * @return {@link List }<{@link XiechengCollidingStagingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    List<XiechengCollidingStagingRuleVO> getCollidingRuleStagingList();


    /**
     * 保存撞库规则
     *
     * @return {@link ApiResult }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    ApiResult<Boolean> saveCollidingRule();


    /**
     * 删除已确认暂存规则
     *
     * @param prsId prs id
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/24
     */
    Boolean deleteStagingCollidingRule(Long prsId);

    /**
     * 修改包轮次
     *
     * @param param param
     * @return {@link Boolean }
     * @author hong.chen
     * @date 2024/08/07
     */
    Boolean updateRound(UpdateRoundParam param);
    
    /**
     * @description 剔除任务列表查询
     * @param queryDTO
     * @return com.br.marketing.commonentity.PageResultReturn<com.br.marketing.dto.rulecenter.XcDeleteTaskVO>
     * @author hedongshuo
     * @date 2025/11/3 15:32
     **/
    PageResultReturn<XcDeleteTaskVO> getCollidingDataDeleteTaskList(XcDeleteTaskQueryDTO queryDTO);

    Boolean deleteCollidingDataDeleteTask(Long taskId);
}
