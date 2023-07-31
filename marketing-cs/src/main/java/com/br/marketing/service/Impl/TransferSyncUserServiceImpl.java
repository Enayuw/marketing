package com.br.marketing.service.Impl;

import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.entity.CaseShuheUploadData;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.CaseShuheUploadDataMapper;
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
    CaseShuheUploadDataMapper caseShuheUploadDataMapper;

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

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public void saveShUploadData(CaseShuheUploadData shuheUploadData) {
//        try {
//            int i = caseShuheUploadDataMapper.insertSelective(shuheUploadData);
//            if (i != 1) {
//                String mgs = "数禾上传数据前置表入库失败";
//                BusinessException exception = new BusinessException(mgs);
//                exception.setExceptionMessage(mgs);
//            }
//            response2ShuheDTO.setMsgId(requestId);
//            shuheUploadData.setRequestId(response2ShuheDTO.getMsgId());
//        } catch (Exception e) {
//            log.error(e.getMessage()
//                    + "\nrequestId:" + shuheUploadData.getRequestId()
//                    + "\napiCode:" + apiCode
//                    + "\njsonData:" + jsonData, e);
//            response2ShuheDTO.setMsgId(shuheUploadData.getRequestId());
//            return response2ShuheDTO.failed();
//        }
//        saveSyncInfo(adapterMarketingPreUserDTO(uploadDataDTO, listInfo, shuheUploadData), shuheUploadData);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertInfoAndSync(MarketingTransferSyncUser marketingTransferSyncUser
            , MarketingTransferInfo transferInfo, CaseShuheUser caseShuheUser) throws Exception {
        int rowInfo = marketingTransferInfoMapper.insertSelective(transferInfo);
        if (rowInfo < 1) {
            caseShuheUser.setSaveStatus(2);
            throw new Exception("#2保存到'b_marketing_transfer_info'失败");
        }
        int rowSync = marketingTransferSyncUserMapper.insertSelective(marketingTransferSyncUser);
        if (rowSync < 1) {
            caseShuheUser.setSaveStatus(3);
            throw new Exception("#3保存到'b_marketing_transfer_sync_" + marketingTransferSyncUser.gettCid() + "'失败");
        }
    }
}
