package com.br.marketing.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.TableCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.CommonConstants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.EntityOptLogMapper;
import com.br.marketing.mapper.MarketingCustomerAssignedGroupMapper;
import com.br.marketing.mapper.MarketingCustomerConfigMapper;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.ICustomerConfigService;
import com.br.marketing.service.IMarketingCustomerAssignedGroupService;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.vo.CustomerListVo;
import com.br.marketing.vo.CustomerSelectVO;
import com.br.marketing.vo.MarketingCustomerListVO;
import com.br.marketing.vo.MarketingCustomerVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingCustomerConfigMapper marketingCustomerConfigMapper;

    @Resource
    CustomerTagsProcessServiceImpl customerTagsProcessService;

    @Autowired
    ICustomerConfigService iCustomerConfigService;

    @Resource
    private IMarketingCustomerAssignedGroupService marketingCustomerAssignedGroupService;


    @Override
    public List<CustomerSelectVO> getCidOrApiCodeList(String cid) {
        List<CustomerSelectVO> cidOrApiCodeList = marketingCustomerMapper.getCidOrApiCodeList(cid);
        if (StringUtils.isEmpty(cid)) {
            return cidOrApiCodeList.stream().distinct().collect(Collectors.toList());
        }
        return cidOrApiCodeList;
    }

    @Override
    public PageResultReturn getCustomerList(int page, int pageSize, String name, String apiCode, String accountType, String accountStatus) {
        PageHelper.startPage(page, pageSize);
        try {
            MarketingCustomerExample marketingCustomerExample = new MarketingCustomerExample();
            MarketingCustomerExample.Criteria criteria = marketingCustomerExample.createCriteria();
            if (apiCode != null) {
                criteria.andApiCodeEqualTo(apiCode);
            }
            if (name != null) {
                criteria.andNameLike("%" + name + "%");
            }
            if (accountType != null) {
                criteria.andAccountTypeEqualTo(Byte.valueOf(accountType));
            }
            if (accountStatus != null) {
                criteria.andAccountStatusEqualTo(Byte.valueOf(accountStatus));
            }
            marketingCustomerExample.setOrderByClause("create_time desc, update_time desc");
            List<MarketingCustomer> marketingCustomersList = marketingCustomerMapper.selectByExample(marketingCustomerExample);

            List<String> apiCodes = marketingCustomersList.stream().map(MarketingCustomer::getApiCode).collect(Collectors.toList());
            HashMap<String, MarketingCustomerConfig> configs = new HashMap();
            if (!CollectionUtils.isEmpty(apiCodes)) {
                MarketingCustomerConfigExample configExample = new MarketingCustomerConfigExample();
                configExample.createCriteria().andApiCodeIn(apiCodes)
                        .andIsDelEqualTo(Constants.DATA_VALID);
                List<MarketingCustomerConfig> marketingCustomerConfigs = marketingCustomerConfigMapper.selectByExample(configExample);
                for (MarketingCustomerConfig marketingCustomerConfig : marketingCustomerConfigs) {
                    configs.put(marketingCustomerConfig.getApiCode(), marketingCustomerConfig);
                }
            }

            List<String> cidList = marketingCustomersList.stream().map(MarketingCustomer::getCid).collect(Collectors.toList());
            Map<String, String> cidGroupMap = Maps.newHashMap();
            if(!CollectionUtils.isEmpty(cidList)) {
                MarketingCustomerAssignedGroupExample assignedGroupExample = new MarketingCustomerAssignedGroupExample();
                assignedGroupExample.createCriteria().andCidIn(cidList);
                List<MarketingCustomerAssignedGroup> marketingCustomerAssignedGroups =
                        marketingCustomerAssignedGroupService.selectByExample(assignedGroupExample);
                cidGroupMap = marketingCustomerAssignedGroups.stream()
                        .collect(Collectors.toMap(MarketingCustomerAssignedGroup::getCid,
                                MarketingCustomerAssignedGroup::getAssignedGroup, (x1, x2) -> x1));
            }

            ArrayList<CustomerListVo> customerListVos = new ArrayList<>();
            for (MarketingCustomer marketingCustomer : marketingCustomersList) {
                CustomerListVo customerListVo = new CustomerListVo();
                BeanUtils.copyProperties(marketingCustomer, customerListVo);
                MarketingCustomerConfig marketingCustomerConfig = configs.get(marketingCustomer.getApiCode());
//                customerListVo.setCheckType(marketingCustomerConfig == null
//                        ? CustomerTagsValue.CheckTypeEnum.CHECKCELL.getValue()
//                        : marketingCustomerConfig.getCheckType());
                customerListVo.setScoreSeparator(marketingCustomerConfig == null ? CommonConstants.COMMA :
                        marketingCustomerConfig.getScoreSeparator());
                customerListVo.setThreeKEncryptType(marketingCustomerConfig == null ? null:
                        marketingCustomerConfig.getThreeKEncryptType());
                customerListVo.setCipherMode(marketingCustomerConfig == null ? null: marketingCustomerConfig.getCipherMode());
                customerListVo.setPaddingScheme(marketingCustomerConfig == null ? null: marketingCustomerConfig.getPaddingScheme());
                customerListVo.setCharset(marketingCustomerConfig == null ? null: marketingCustomerConfig.getCharset());
                customerListVo.setIv(marketingCustomerConfig == null ? null: marketingCustomerConfig.getIv());
                customerListVo.setDynamicKeys(marketingCustomerConfig == null ? null: marketingCustomerConfig.getDynamicKeys());
                customerListVo.setAssignedGroup(cidGroupMap.get(marketingCustomer.getCid()));
                customerListVos.add(customerListVo);
            }
            PageInfo<MarketingCustomer> marketingCustomerPageInfo = new PageInfo<>(marketingCustomersList);
            return PageResultReturn.setPageResult(customerListVos, page, pageSize,marketingCustomerPageInfo.getTotal());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> saveOrUpdateCustomer(MarketingCustomerListVO vo, MarketingUserDetail user) {

        Date date = new Date();

        //新增、变更，还需要记录变更日志，加个日志表
        MarketingCustomer marketingCustomer = new MarketingCustomer();
        marketingCustomer.setMessage(vo.getMessage() != null ? vo.getMessage() : "");
        marketingCustomer.setThreadNum(vo.getThreadNum());
        marketingCustomer.setSort(vo.getSort());
        marketingCustomer.setStatus(vo.getStatus());
        marketingCustomer.setAccountStatus(vo.getAccountStatus());
        marketingCustomer.setExtendConfigInfo(vo.getExtendConfigInfo());
        if (StringUtils.hasText(vo.getExpireDay())) {
            marketingCustomer.setExpireDay(vo.getExpireDay().trim());
        }
        marketingCustomer.setType("all,once");
        //push_type如果为1,push_url、push_thread_num必须不为空
        marketingCustomer.setPushType(vo.getPushType() != null ? vo.getPushType() : 0);
        marketingCustomer.setPushThreadNum(vo.getPushThreadNum() != null ? vo.getPushThreadNum() : 0);
        marketingCustomer.setPushUrl(vo.getPushUrl() != null ? vo.getPushUrl() : "");
        marketingCustomer.setName(vo.getName());
        marketingCustomer.setShortName(vo.getShortName());
        marketingCustomer.setUpdateTime(date);
        marketingCustomer.setSmsCategory(vo.getSmsCategory());
        marketingCustomer.setFirstDepartment(vo.getFirstDepartment());
        marketingCustomer.setSecondDepartment(vo.getSecondDepartment());
        marketingCustomer.setApiType(vo.getApiType());
        if (StringUtils.isEmpty(vo.getId())) {
            //新增
            marketingCustomer.setCid(vo.getCid());
            marketingCustomer.setApiCode(vo.getApiCode());
            marketingCustomer.setCreateTime(date);
            marketingCustomerMapper.insertSelective(marketingCustomer);

            MarketingCustomerConfig marketingCustomerConfig = new MarketingCustomerConfig();
            marketingCustomerConfig.setApiCode(vo.getApiCode());
            marketingCustomerConfig.setCreateTime(date);
            marketingCustomerConfig.setUpdateTime(date);
            marketingCustomerConfig.setCheckType(vo.getCheckType());
            marketingCustomerConfig.setScoreSeparator(vo.getScoreSeparator());
            marketingCustomerConfig.setThreeKEncryptType(vo.getThreeKEncryptType());
            marketingCustomerConfig.setCipherMode(vo.getCipherMode());
            marketingCustomerConfig.setPaddingScheme(vo.getPaddingScheme());
            marketingCustomerConfig.setCharset(vo.getCharset());
            marketingCustomerConfig.setIv(vo.getIv());
            marketingCustomerConfig.setDynamicKeys(vo.getDynamicKeys());
            marketingCustomerConfigMapper.insertSelective(marketingCustomerConfig);

        } else {
            //更新记录到日志表
            EntityOptLog entityOptLog = new EntityOptLog();
            entityOptLog.setSourceObj(TableCodeEnum.MARKETING_CUSTOMER.getTableName());
            entityOptLog.setSourceEntity(TableCodeEnum.MARKETING_CUSTOMER.getTableEntity());
            entityOptLog.setSourceId(vo.getId().toString());

            StringBuilder content = new StringBuilder();
            MarketingCustomer customerOld = marketingCustomerMapper.selectByPrimaryKey(vo.getId());

            MarketingCustomerConfigExample configExample = new MarketingCustomerConfigExample();
            configExample.createCriteria()
                    .andIsDelEqualTo(Constants.DATA_VALID)
                    .andApiCodeEqualTo(customerOld.getApiCode());
            List<MarketingCustomerConfig> marketingCustomerConfigs = marketingCustomerConfigMapper.selectByExample(configExample);
            if (marketingCustomerConfigs.size() <= 0) {
                MarketingCustomerConfig marketingCustomerConfig = new MarketingCustomerConfig();
                marketingCustomerConfig.setApiCode(vo.getApiCode());
                marketingCustomerConfig.setCreateTime(date);
                marketingCustomerConfig.setUpdateTime(date);
                marketingCustomerConfig.setCheckType(vo.getCheckType());
                marketingCustomerConfig.setScoreSeparator(vo.getScoreSeparator());
                marketingCustomerConfig.setThreeKEncryptType(vo.getThreeKEncryptType());
                marketingCustomerConfig.setCipherMode(vo.getCipherMode());
                marketingCustomerConfig.setPaddingScheme(vo.getPaddingScheme());
                marketingCustomerConfig.setCharset(vo.getCharset());
                marketingCustomerConfig.setIv(vo.getIv());
                marketingCustomerConfig.setDynamicKeys(vo.getDynamicKeys());
                marketingCustomerConfigMapper.insertSelective(marketingCustomerConfig);
            } else {
                MarketingCustomerConfig marketingCustomerConfig = marketingCustomerConfigs.get(0);
                MarketingCustomerConfig updateEntity = new MarketingCustomerConfig();
                updateEntity.setId(marketingCustomerConfig.getId());
                updateEntity.setCheckType(vo.getCheckType());
                updateEntity.setScoreSeparator(vo.getScoreSeparator());
                updateEntity.setThreeKEncryptType(vo.getThreeKEncryptType());
                updateEntity.setCipherMode(vo.getCipherMode());
                updateEntity.setPaddingScheme(vo.getPaddingScheme());
                updateEntity.setCharset(vo.getCharset());
                updateEntity.setIv(vo.getIv());
                updateEntity.setDynamicKeys(vo.getDynamicKeys());
                marketingCustomerConfigMapper.updateByPrimaryKeySelective(updateEntity);
                //更新3k加密类型
                if (!Objects.isNull(vo.getThreeKEncryptType())) {
                    iCustomerConfigService.updateEncryptyType(vo.getApiCode(), vo.getThreeKEncryptType());
                }
                content.append("【checkType】=【" + marketingCustomerConfig.getCheckType() + "】" + "->【" + vo.getCheckType() + "】,");
            }

            content.append("【message】=【" + customerOld.getMessage() + "】" + "->【" + marketingCustomer.getMessage() + "】,");
            content.append("【threadNum】=【" + customerOld.getThreadNum() + "】" + "->【" + marketingCustomer.getThreadNum() + "】,");
            content.append("【sort】=【" + customerOld.getSort() + "】" + "->【" + marketingCustomer.getSort() + "】,");
            content.append("【status】=【" + customerOld.getStatus() + "】" + "->【" + marketingCustomer.getStatus() + "】,");
            content.append("【extendConfigInfo】=【" + customerOld.getExtendConfigInfo() + "】" + "->【" + marketingCustomer.getExtendConfigInfo() + "】,");
            content.append("【pushType】=【" + customerOld.getPushType() + "】" + "->【" + marketingCustomer.getPushType() + "】,");
            content.append("【pushThreadNum】=【" + customerOld.getPushThreadNum() + "】" + "->【" + marketingCustomer.getPushThreadNum() + "】,");
            content.append("【pushUrl】=【" + customerOld.getPushUrl() + "】" + "->【" + marketingCustomer.getPushUrl() + "】,");
            content.append("【name】=【" + customerOld.getName() + "】" + "->【" + marketingCustomer.getName() + "】,");
            content.append("【shortName】=【" + customerOld.getShortName() + "】" + "->【" + marketingCustomer.getShortName() + "】,");

            entityOptLog.setContent(content.toString());
            entityOptLog.setOptUserId(String.valueOf(user.getId()));
            entityOptLog.setOptUserName(user.getUserName());
            /*entityOptLog.setOptUserId("xxx");
            entityOptLog.setOptUserName("xxxx");*/
            entityOptLog.setCreateTime(date);
            int i = entityOptLogMapper.insertSelective(entityOptLog);
            if (StringUtils.isEmpty(i)) {
                log.error("插入日志表 b_entity_opt_log 失败!");
            }
            //编辑
            marketingCustomer.setId(vo.getId());
            marketingCustomerMapper.updateByPrimaryKeySelective(marketingCustomer);
        }
        marketingCustomerAssignedGroupService.assignGroup(vo.getCid(), vo.getAssignedGroup(), vo.getApiCode());
        customerTagsProcessService.delTagsOfRedis(vo.getApiCode());
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public ApiResult<Boolean> apiCodeOnly(String id, String apiCode) {
        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andApiCodeEqualTo(apiCode);
        List<MarketingCustomer> select = marketingCustomerMapper.selectByExample(example);
        if (select != null && select.size() > 0) {
            if (StringUtils.isEmpty(id)) {
                return new ApiResult<Boolean>().success(false, "apicode已存在！");
            }
            for (MarketingCustomer single : select) {
                if (id.equals(single.getId().toString())) {
                    return new ApiResult<Boolean>().success(true);
                }
            }
            return new ApiResult<Boolean>().success(false, "apicode已存在！");
        } else {
            return new ApiResult<Boolean>().success(true);
        }

    }

    @Override
    public List<MarketingCustomerVO> getApiCodeList(String apiCode) {
        MarketingCustomerExample example = new MarketingCustomerExample();
        if (apiCode != null && !"".equals(apiCode)) {
            example.createCriteria().andStatusEqualTo((byte) 1).andApiCodeLike("%" + apiCode + "%");
        } else {
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
    public List<MarketingCustomerVO> getApiCodeList(List<String> apiCodeList) {
        if (CollUtil.isEmpty(apiCodeList)) {
            return new ArrayList<>();
        }
        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andStatusEqualTo((byte) 1).andApiCodeIn(apiCodeList);
        List<MarketingCustomer> list = marketingCustomerMapper.selectByExample(example);

        List<MarketingCustomerVO> vos = list.stream().map(marketingCustomer -> {
            MarketingCustomerVO vo = new MarketingCustomerVO();
            BeanUtils.copyProperties(marketingCustomer, vo);
            vo.setId(marketingCustomer.getId().toString());
            return vo;
        }).collect(Collectors.toList());

        return vos.stream().distinct().collect(Collectors.toList());
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

    @Override
    public String getThreeKEncryptType() {
        return CustomerTagsValue.convertPushJc3keyEnumToJson();
    }


}
