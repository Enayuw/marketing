package com.br.marketing.service.Impl;

import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.ITransferSyncUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 客户转化数据记录业务接口 实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/15 10:00
 */
@Service
@Slf4j
public class TransferSyncUserServiceImpl implements ITransferSyncUserService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Override
    public int insertSelective(MarketingTransferSyncUser marketingTransferSyncUser) {
        return marketingTransferSyncUserMapper.insertSelective(marketingTransferSyncUser);
    }

    @Override
    public int updateByPrimaryKeySelective(MarketingTransferSyncUser marketingTransferSyncUser) {
        return marketingTransferSyncUserMapper.updateByPrimaryKeySelective(marketingTransferSyncUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertInfoAndSync(MarketingTransferSyncUser marketingTransferSyncUser
            , MarketingTransferInfo transferInfo, CaseShuheUser caseShuheUser) {
        try {
            int rowInfo = marketingTransferInfoMapper.insertSelective(transferInfo);
            if (rowInfo < 1) {
                caseShuheUser.setErrorInfo("#2saveTransferInfo:保存到标准转化信息失败");
                caseShuheUser.setSaveStatus(2);
                throw new BusinessException();
            }
            int rowSync = marketingTransferSyncUserMapper.insertSelective(marketingTransferSyncUser);
            if (rowSync < 1) {
                caseShuheUser.setSaveStatus(3);
                caseShuheUser.setErrorInfo("#3saveTransferInfo:保存到标准转化详情失败");
                throw new BusinessException();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            caseShuheUser.setSaveStatus(2);
            caseShuheUser.setErrorInfo("保存到标准转化信息异常:" + e.getMessage());
            return null;
        }
        return transferInfo.getId();
    }
}
