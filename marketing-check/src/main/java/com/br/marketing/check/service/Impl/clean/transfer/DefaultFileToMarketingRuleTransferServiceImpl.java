package com.br.marketing.check.service.Impl.clean.transfer;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.SimpleResult;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.service.IFileToMarketingRuleTransferService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName DefaultFileToMarketingRuleTransferServiceImpl
 * @Description 默认转化清洗逻辑
 * @Author kongbx
 * @Date 2025/5/26 19:16
 */
@Service
public class DefaultFileToMarketingRuleTransferServiceImpl implements IFileToMarketingRuleTransferService {

    @Override
    public SimpleResult isVaild(List<FileToMarketingDataFieldVO> vos, Map<String,FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleTransferService.super.isVaild(vos,voMaps);
    }

    @Override
    public TransferDataItemDTO make(List<FileToMarketingDataFieldVO> vos) {
        return IFileToMarketingRuleTransferService.super.make(vos);
    }

    @Override
    public String getTaskId(String apiCode,String fileNm) {
        return IFileToMarketingRuleTransferService.super.getTaskId(apiCode,fileNm);
    }

}
