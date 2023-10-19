package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingDataValidConfigDefault;
import com.br.marketing.entity.ValidityPeriodResendRecord;
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
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
                variableDicListVO.setValidDaysDefault("T+" + validDaysDefault);
            }
            return PageResultReturn.setPageResult(list, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ApiResult<Boolean> saveOrUpdateVariableDic(VariableDicListVO vo, MarketingUserDetail user, Integer days) {
        String apiCode, userType = null;
        apiCode = vo.getApiCode();
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
        validConfigDefault.setValidDaysDefault(days);
        validConfigDefault.setIsDel(vo.getIsDel());
        if(StringUtils.isEmpty(vo.getId())){
            //新增
            variableDic.setCid(vo.getCid());
            variableDic.setApiCode(apiCode);
            variableDic.setCreateTime(new Date());
            variableDicMapper.insert(variableDic);
            Integer i = validityChangeMapper.selectNum(apiCode, userType);
            if (i >= 1){
                log.warn("该apiCode + userType维度下已存在有效期配置");
                return new ApiResult<Boolean>().fail(ServiceResultEnum.SUCCESS_4);
            }
            validConfigDefault.setApiCode(apiCode);
            validConfigDefault.setCreateTime(new Date());
            validityChangeMapper.insertValidConfigDefault(validConfigDefault);
        }else {
            //编辑
            variableDic.setId(vo.getId());
            Integer i = variableDicMapper.updateByPrimaryKeySelective(variableDic);
            Long id = validityChangeMapper.selectId(apiCode,userType);
            validConfigDefault.setId(id);
            validConfigDefault.setApiCode(apiCode);
            validConfigDefault.setUpdateTime(new Date());
            Integer j = validityChangeMapper.updateMarketingDataValidConfigDefault(validConfigDefault);
            if (i == 1 && j == 1){

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

    @Override
    public String getValidPeriod(String startDate, String endDate) {
        LocalDate start = formatStringToDate(startDate);
        LocalDate end = formatStringToDate(endDate);
        long daysBetween = ChronoUnit.DAYS.between(start, end) + 1;
        String validPeriod = "T+" + daysBetween;
        return  validPeriod;
    }


    public static LocalDate formatStringToDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateString, formatter);
        return date;
    }


}
