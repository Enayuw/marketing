package com.br.marketing.service.Impl.xc;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.vo.xiecheng.PackageRuleListParam;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;

public interface XieChengPackageRuleService {
    /**
     * 获取携程撞库规则列表
     *
     * @param listParam 列表参数
     * @return {@link PageResultReturn }<{@link XiechengCollidingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/23
     */
    PageResultReturn<XiechengCollidingRuleVO> getPackageRuleList(PackageRuleListParam listParam);
}
