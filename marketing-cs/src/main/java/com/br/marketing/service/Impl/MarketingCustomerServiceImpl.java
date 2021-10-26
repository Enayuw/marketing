package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户业务逻辑实现
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/1 15:34
 */
@Service
@Slf4j
public class MarketingCustomerServiceImpl implements MarketingCustomerService {

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;

    @Override
    public List<CustomerSelectVO> getCidOrApiCodeList(String cid) {
        List<CustomerSelectVO> cidOrApiCodeList = marketingCustomerMapper.getCidOrApiCodeList(cid);
        if (StringUtils.isEmpty(cid)) {
            return cidOrApiCodeList.stream().distinct().collect(Collectors.toList());
        }
        return cidOrApiCodeList;
    }

    @Override
    public PageResultReturn getCustomerList(int page, int pageSize, String cid, String apiCode) {
        PageHelper.startPage(page, pageSize);
        try {
            List<MarketingCustomerListVO> list = marketingCustomerMapper.getCustomerList(cid,apiCode);
            return PageResultReturn.setPageResult(list, page);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ApiResult<Boolean> saveOrUpdateCustomer(MarketingCustomerListVO vo, UserDetail user) {
        //新增、变更，还需要记录变更日志，加个日志表
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        marketingCustomer.setMessage(vo.getMessage()!=null?vo.getMessage():"");
        marketingCustomer.setThreadNum(vo.getThreadNum());
        marketingCustomer.setSort(vo.getSort());
        marketingCustomer.setStatus((byte) 1);
        marketingCustomer.setExtendConfigInfo(vo.getExtendConfigInfo());
        //push_type如果为1,push_url、push_thread_num必须不为空
        marketingCustomer.setPushType(vo.getPushType()!=null?vo.getPushType():0);
        marketingCustomer.setPushThreadNum(vo.getPushThreadNum()!=null?vo.getPushThreadNum():0);
        marketingCustomer.setPushUrl(vo.getPushUrl()!=null?vo.getPushUrl():"");
        marketingCustomer.setName(vo.getName());
        marketingCustomer.setShortName(vo.getShortName());
        if(StringUtils.isEmpty(vo.getId())){
            //新增
            marketingCustomer.setCid(vo.getCid());
            marketingCustomer.setApiCode(vo.getApiCode());
            marketingCustomer.setCreateTime(new Date());
            marketingCustomerMapper.insert(marketingCustomer);
        }else {
            //编辑
            marketingCustomer.setUpdateTime(new Date());
            marketingCustomerMapper.updateByPrimaryKeySelective(marketingCustomer);
        }


        return new ApiResult<Boolean>().success(true);
    }
}
