package com.br.marketing.service.Impl;

import com.br.marketing.entity.VariableDic;
import com.br.marketing.entity.VariableDicExample;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.VariableDicService;
import com.br.marketing.vo.VariableDicSelectVO;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户配置变量值字典
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:29
 */
@Service
public class VariableDicServiceImpl implements VariableDicService {

    @Resource
    private VariableDicMapper variableDicMapper;

    @Override
    public List<VariableDicSelectVO> findListByCidAndApiCode(String cid, String apiCode) {
        VariableDicExample example = new VariableDicExample();
        example.createCriteria().andCidEqualTo(cid).andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        List<VariableDic> variableDics = variableDicMapper.selectByExample(example);
        if (ObjectUtils.isEmpty(variableDics)) {
            return null;
        }
        return variableDics.stream().map(v -> new VariableDicSelectVO(
                v.getFieldName(), v.getFieldValue(), v.getFieldDesc())).collect(Collectors.toList());
    }
}
