package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.TableCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.EntityOptLog;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.EntityOptLogMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import com.br.marketing.vo.MarketingCustomerVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    @Resource
    private RedisChgService redisChgService;

    @Override
    public List<CustomerSelectVO> getCidOrApiCodeList(String cid) {
        List<CustomerSelectVO> cidOrApiCodeList = marketingCustomerMapper.getCidOrApiCodeList(cid);
        if (StringUtils.isEmpty(cid)) {
            return cidOrApiCodeList.stream().distinct().collect(Collectors.toList());
        }
        return cidOrApiCodeList;
    }

    @Override
    public PageResultReturn getCustomerList(int page, int pageSize, String name, String apiCode,String accountType,String accountStatus) {
        PageHelper.startPage(page, pageSize);
        try {
            MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
            MarketingCustomerExample.Criteria criteria = marketingCustomerExample.createCriteria();
            if(apiCode!=null){
                criteria.andApiCodeEqualTo(apiCode);
            }
            if(name!=null){
                criteria.andNameLike("%"+name+"%");
            }
            if(accountType!=null){
                criteria.andAccountTypeEqualTo(Byte.valueOf(accountType));
            }
            if(accountStatus!=null){
                criteria.andAccountStatusEqualTo(Byte.valueOf(accountStatus));
            }
            marketingCustomerExample.setOrderByClause("create_time desc, update_time desc");
            List<MarketingCustomer> marketingCustomersList = marketingCustomerMapper.selectByExample(marketingCustomerExample);
            return PageResultReturn.setPageResult(marketingCustomersList, page, pageSize);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> saveOrUpdateCustomer(MarketingCustomerListVO vo, MarketingUserDetail user) {
        //新增、变更，还需要记录变更日志，加个日志表
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        marketingCustomer.setMessage(vo.getMessage()!=null?vo.getMessage():"");
        marketingCustomer.setThreadNum(vo.getThreadNum());
        marketingCustomer.setSort(vo.getSort());
        marketingCustomer.setStatus(vo.getStatus());
        marketingCustomer.setAccountStatus(vo.getAccountStatus());
        marketingCustomer.setExtendConfigInfo(vo.getExtendConfigInfo());
        marketingCustomer.setType("all,once");
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
            entityOptLog.setOptUserId(String.valueOf(user.getId()));
            entityOptLog.setOptUserName(user.getUserName());
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
        example.createCriteria().andApiCodeEqualTo(apiCode);
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

    @Override
    public List<MarketingCustomerVO> getApiCodeList(String apiCode) {
        MarketingCustomerExample example = new MarketingCustomerExample();
        if(apiCode != null && !"".equals(apiCode)){
            example.createCriteria().andStatusEqualTo((byte) 1).andApiCodeLike("%"+apiCode+"%");
        }else{
            example.createCriteria().andStatusEqualTo((byte) 1);
        }
        List<MarketingCustomer> list = marketingCustomerMapper.selectByExample(example);

        List<MarketingCustomerVO> vos = list.stream().map(marketingCustomer -> {
            MarketingCustomerVO vo = new MarketingCustomerVO();
            BeanUtils.copyProperties(marketingCustomer, vo);
            vo.setId(marketingCustomer.getId().toString());
            return vo;
        }).collect(Collectors.toList());

        if (StringUtils.isEmpty(vos)) {
            return vos.stream().distinct().collect(Collectors.toList());
        }
        return vos;
    }

    @Override
    public List<MarketingCustomerVO> getCidOrName(String search) {
        List<MarketingCustomer> list = marketingCustomerMapper.getCidOrName(search);

        List<MarketingCustomerVO> vos = list.stream().map(marketingCustomer -> {
            MarketingCustomerVO vo = new MarketingCustomerVO();
            BeanUtils.copyProperties(marketingCustomer, vo);
            vo.setId(marketingCustomer.getId().toString());
            return vo;
        }).collect(Collectors.toList());

        return vos;
    }

    @Override
    public MarketingCustomer getCacheCustomerByApiCode(String apiCode) {
        String redisKey = RedisKeyConstant.CUSTOMER_INFO.concat(apiCode);
        try {
            Map<String, Object> hgetall = redisChgService.hgetall(redisKey);
            if (CollectionUtils.isEmpty(hgetall)) {
                List<MarketingCustomer> customers = marketingCustomerMapper.getNameByApiCodeList(apiCode);
                if (CollectionUtils.isEmpty(customers)) {
                    return null;
                }
                MarketingCustomer customer = customers.get(0);
                redisChgService.hmset(redisKey, JSONObject.parseObject(JSON.toJSONString(customer)
                        , new TypeReference<Map<String, String>>() {
                        }));
                redisChgService.expire(redisKey, RandomUtils.nextInt(3600 * 24 * 3, 3600 * 24 * 7));
                return customer;
            }
            return JSONObject.parseObject(JSON.toJSONString(hgetall), new TypeReference<MarketingCustomer>() {
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            List<MarketingCustomer> customers = marketingCustomerMapper.getNameByApiCodeList(apiCode);
            return CollectionUtils.isEmpty(customers) ? null : customers.get(0);
        }
    }

    @Override
    public List<String> getApiCodeByProd(List<String> apiCodePrefix) {
        List<String> apiCodeByZs = marketingCustomerMapper.getApiCodeByZs(apiCodePrefix);
        return apiCodeByZs;
    }

}
