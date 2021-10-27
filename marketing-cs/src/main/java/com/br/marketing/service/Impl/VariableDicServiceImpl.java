package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.VariableDic;
import com.br.marketing.entity.VariableDicExample;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.VariableDicService;
import com.br.marketing.vo.VariableDicListVO;
import com.br.marketing.vo.VariableDicSelectVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户配置变量值字典
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 17:29
 */
@Service
@Slf4j
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

    @Override
    public PageResultReturn getVariableDicList(int page, int pageSize, String cid, String apiCode) {
        PageHelper.startPage(page, pageSize);
        try {
            List<VariableDicListVO> list = variableDicMapper.getVariableDicList(cid,apiCode);
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ApiResult<Boolean> saveOrUpdateVariableDic(VariableDicListVO vo, UserDetail user) {
        VariableDic variableDic = new VariableDic();
        variableDic.setFieldName(vo.getFieldName());
        variableDic.setFieldValue(vo.getFieldValue());
        variableDic.setFieldDesc(vo.getFieldDesc());
        variableDic.setIsDel(vo.getIsDel());
        variableDic.setUpdateTime(new Date());
        if(StringUtils.isEmpty(vo.getId())){
            //新增
            variableDic.setCid(vo.getCid());
            variableDic.setApiCode(vo.getApiCode());
            variableDic.setCreateTime(new Date());
            variableDicMapper.insert(variableDic);
        }else {
            //编辑
            variableDic.setId(vo.getId());
            variableDicMapper.updateByPrimaryKeySelective(variableDic);
        }

        return new ApiResult<Boolean>().success(true);
    }

    /*@Override
    public ApiResult<Boolean> delete(Integer id) {

        return new ApiResult<Boolean>().success(true);
    }*/

}
