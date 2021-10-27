package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.TableCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.EntityOptLog;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.mapper.EntityOptLogMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private EntityOptLogMapper entityOptLogMapper;

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
    @Transactional(rollbackFor = Exception.class)
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
        marketingCustomer.setUpdateTime(new Date());
        if(StringUtils.isEmpty(vo.getId())){
            //新增
            marketingCustomer.setCid(vo.getCid());
            marketingCustomer.setApiCode(vo.getApiCode());
            marketingCustomer.setCreateTime(new Date());
            marketingCustomerMapper.insertSelective(marketingCustomer);
        }else {
            //更新记录到日志表
            EntityOptLog entityOptLog = new EntityOptLog();
            entityOptLog.setSourceObj(TableCodeEnum.MARKETING_CUSTOMER.getTableName());
            entityOptLog.setSourceEntity(TableCodeEnum.MARKETING_CUSTOMER.getTableEntity());
            entityOptLog.setSourceId(vo.getId().toString());

            StringBuilder content = new StringBuilder();
            MarketingCustomer customerOld = marketingCustomerMapper.selectByPrimaryKey(vo.getId());

            content.append("【message】=【"+customerOld.getMessage()+"】"+"->【"+marketingCustomer.getMessage()+"】,");
            content.append("【threadNum】=【"+customerOld.getThreadNum()+"】"+"->【"+marketingCustomer.getThreadNum()+"】,");
            content.append("【sort】=【"+customerOld.getSort()+"】"+"->【"+marketingCustomer.getSort()+"】,");
            content.append("【status】=【"+customerOld.getStatus()+"】"+"->【"+marketingCustomer.getStatus()+"】,");
            content.append("【extendConfigInfo】=【"+customerOld.getExtendConfigInfo()+"】"+"->【"+marketingCustomer.getExtendConfigInfo()+"】,");
            content.append("【pushType】=【"+customerOld.getPushType()+"】"+"->【"+marketingCustomer.getPushType()+"】,");
            content.append("【pushThreadNum】=【"+customerOld.getPushThreadNum()+"】"+"->【"+marketingCustomer.getPushThreadNum()+"】,");
            content.append("【pushUrl】=【"+customerOld.getPushUrl()+"】"+"->【"+marketingCustomer.getPushUrl()+"】,");
            content.append("【name】=【"+customerOld.getName()+"】"+"->【"+marketingCustomer.getName()+"】,");
            content.append("【shortName】=【"+customerOld.getShortName()+"】"+"->【"+marketingCustomer.getShortName()+"】,");

            entityOptLog.setContent(content.toString());
            entityOptLog.setOptUserId(user.getUserId());
            entityOptLog.setOptUserName(user.getUsername());
            /*entityOptLog.setOptUserId("xxx");
            entityOptLog.setOptUserName("xxxx");*/
            entityOptLog.setCreateTime(new Date());
            int i = entityOptLogMapper.insertSelective(entityOptLog);
            if (StringUtils.isEmpty(i)){
                log.error("插入日志表 b_entity_opt_log 失败!");
            }
            //编辑
            marketingCustomer.setId(vo.getId());
            marketingCustomerMapper.updateByPrimaryKeySelective(marketingCustomer);

        }

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public ApiResult<Boolean> apiCodeOnly(String id,String apiCode) {
        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andStatusEqualTo((byte) 1);
        List<MarketingCustomer> select = marketingCustomerMapper.selectByExample(example);
        if (select != null && select.size()>0){
            if(StringUtils.isEmpty(id)){
                return new ApiResult<Boolean>().success(false,"apicode已存在！");
            }
            for(MarketingCustomer single:select){
                if(id.equals(single.getId().toString())){
                    return new ApiResult<Boolean>().success(true);
                }
            }
            return new ApiResult<Boolean>().success(false,"apicode已存在！");
        }else {
            return new ApiResult<Boolean>().success(true);
        }

    }
}
