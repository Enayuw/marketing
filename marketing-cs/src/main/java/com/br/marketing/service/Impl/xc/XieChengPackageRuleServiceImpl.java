package com.br.marketing.service.Impl.xc;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.vo.xiecheng.PackageRuleListParam;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.github.pagehelper.PageHelper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XieChengPackageRuleServiceImpl implements XieChengPackageRuleService {

    @Resource
    private XiechengCollidingDataPackageRuleMapper packageRuleMapper;

    /**
     * 获取携程撞库规则列表
     *
     * @param listParam 列表参数
     * @return {@link PageResultReturn }<{@link XiechengCollidingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/23
     */
    @Override
    public PageResultReturn<XiechengCollidingRuleVO> getPackageRuleList(PackageRuleListParam listParam) {
        PageHelper.startPage(listParam.getPage(), listParam.getPageSize());
        List<XiechengCollidingRuleVO> soleOptLogs = packageRuleMapper.getPackageRuleList(listParam);
        return PageResultReturn.setPageResult(soleOptLogs, listParam.getPage(), listParam.getPageSize());
    }

}
