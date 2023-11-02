package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataValidConfigDefault;
import com.br.marketing.entity.VariableDic;
import com.br.marketing.entity.VariableDicExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MarketingValidityChangeMapper;
import com.br.marketing.mapper.ValidityPeriodResendRecordMapperBase;
import com.br.marketing.mapper.VariableDicMapper;
import com.br.marketing.service.VariableDicService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.VariableDicListVO;
import com.br.marketing.vo.VariableDicSelectVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
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

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Resource
    private VariableDicMapper variableDicMapper;

    @Resource
    private MarketingValidityChangeMapper validityChangeMapper;

    @Resource
    private ValidityPeriodResendRecordMapperBase validityPeriodResendRecordMapperBase;

    @Override
    public List<VariableDicSelectVO> findListByCidAndApiCode(String cid, String apiCode) {
        VariableDicExample example = new VariableDicExample();
        example.createCriteria().andCidEqualTo(cid).andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
        example.setOrderByClause("create_time desc, update_time desc");
        List<VariableDic> variableDics = variableDicMapper.selectByExample(example);
        if (ObjectUtils.isEmpty(variableDics)) {
            return Collections.emptyList();
        }
        return variableDics.stream().map(v -> new VariableDicSelectVO(
                v.getFieldName(), v.getFieldValue(), v.getFieldDesc())).collect(Collectors.toList());
    }

    @Override
    public PageResultReturn getVariableDicList(int page, int pageSize, String cid, String apiCode) {
        PageHelper.startPage(page, pageSize);
        try {
            List<VariableDicListVO> list = variableDicMapper.getVariableDicList(cid,apiCode);
            for (VariableDicListVO variableDicListVO : list) {
                apiCode = variableDicListVO.getApiCode();
                String userType = null;
                if ("userType".equals(variableDicListVO.getFieldName())){
                    userType = variableDicListVO.getFieldValue();
                }
                Integer validDaysDefault = validityChangeMapper.selectValidDaysDefault(apiCode, userType);
                if (ObjectUtil.isNotEmpty(validDaysDefault)){
                    variableDicListVO.setValidDaysDefault("T+" + validDaysDefault);
                } else {
                    log.warn("不存在有效期天数配置,apiCode={},userType={}", apiCode, userType);
                }

            }
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ApiResult<Boolean> saveOrUpdateVariableDic(VariableDicListVO vo, MarketingUserDetail user) {
        String apiCode, userType = null;
        apiCode = vo.getApiCode();
        if (vo.getValidDaysDefault() == null){
            vo.setValidDaysDefault("0");
        }
        VariableDic variableDic = new VariableDic();
        variableDic.setFieldName(vo.getFieldName());
        variableDic.setFieldValue(vo.getFieldValue());
        variableDic.setFieldDesc(vo.getFieldDesc());
        variableDic.setIsDel(vo.getIsDel());
        variableDic.setUpdateTime(new Date());
        MarketingDataValidConfigDefault validConfigDefault = new MarketingDataValidConfigDefault();
        if ("userType".equals(vo.getFieldName())){
            userType = vo.getFieldValue();
            validConfigDefault.setUserType(userType);
        }
        validConfigDefault.setValidDaysDefault(Integer.valueOf(vo.getValidDaysDefault()));
        validConfigDefault.setIsDel(vo.getIsDel());
        if(StringUtils.isEmpty(vo.getId())){
            //新增
            variableDic.setCid(vo.getCid());
            variableDic.setApiCode(apiCode);
            variableDic.setCreateTime(new Date());
            variableDicMapper.insertSelective(variableDic);
            entityOptService.writeOptLog(variableDic.getId(), variableDic, null);
            Integer i = validityChangeMapper.selectNum(apiCode, userType);
            MarketingDataValidConfigDefault date = validityChangeMapper.selectId(apiCode,userType);
            if (i >= 1){
                log.warn("该apiCode={} , userType={}维度下已存在有效期配置", apiCode, userType);
                validConfigDefault.setId(date.getId());
                validConfigDefault.setApiCode(apiCode);
                validConfigDefault.setUpdateTime(new Date());
                validityChangeMapper.updateMarketingDataValidConfigDefault(validConfigDefault);
                entityOptService.writeOptLog(date.getId(), validConfigDefault, date);
                return new ApiResult<Boolean>().success(true);
            }
            validConfigDefault.setApiCode(apiCode);
            validConfigDefault.setCreateTime(new Date());
            validityChangeMapper.insertSelective(validConfigDefault);
            entityOptService.writeOptLog(validConfigDefault.getId(), validConfigDefault, null);
        }else {
            VariableDic data = variableDicMapper.selectByPrimaryKey(vo.getId());
            //编辑
            variableDic.setId(vo.getId());
            variableDicMapper.updateByPrimaryKeySelective(variableDic);
            entityOptService.writeOptLog(vo.getId(), variableDic, data);
            MarketingDataValidConfigDefault dataValidConfigDefault = validityChangeMapper.selectId(apiCode,userType);
            if (ObjectUtil.isNotEmpty(dataValidConfigDefault)){
                validConfigDefault.setId(dataValidConfigDefault.getId());
                validConfigDefault.setApiCode(apiCode);
                validConfigDefault.setUpdateTime(new Date());
                validityChangeMapper.updateMarketingDataValidConfigDefault(validConfigDefault);
                entityOptService.writeOptLog(dataValidConfigDefault.getId(), validConfigDefault, dataValidConfigDefault);
            } else {
                log.warn("该apiCode={} , userType={}维度不存在代运营默认有效期配置", apiCode, userType);
            }

        }

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public List<Map> findListByCidsAndApiCodes(List<CustomerSelectVO> vos) {
        List<Map> list = new ArrayList<>();
        if(vos!=null && vos.size()>0){
            for (CustomerSelectVO vo :vos) {
                String cid = vo.getCid();
                String apiCode = vo.getApiCode();
                List<VariableDicSelectVO> userTypeList = findListByCidAndApiCode(cid, apiCode);
                Map map = new HashMap();
                map.put("cid",cid);
                map.put("apiCode",apiCode);
                map.put("userTypeList",userTypeList);
                list.add(map);
            }
        }

        return list;
    }


}
