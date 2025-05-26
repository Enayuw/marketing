package com.br.marketing.check.service.Impl.clean.transfer;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.service.IFileToMarketingRuleTransferService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ZhaoLianFileToMarketingRuleTransferServiceImpl
 * @Description 招联转化数据清洗
 * @Author kongbx
 * @Date 2025/5/26 20:04
 */
@Service
public class ZhaoLianFileToMarketingRuleTransferServiceImpl implements IFileToMarketingRuleTransferService {

    @Override
    public Result isVaild(List<FileToMarketingDataFieldVO> vos, Map<String, FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleTransferService.super.isVaild(vos, voMaps);
    }

    @Override
    public TransferDataDTO make(List<FileToMarketingDataFieldVO> vos) {
        return IFileToMarketingRuleTransferService.super.make(vos);
    }

    @Override
    public String getTaskId(String apiCode, String filename) {
        // 文件示例：transform_20250526_E1Z13250526004BR_01.txt
        String[] parts = filename.split("_");
        String taskId = "";
        if (parts.length >= 3) {
            taskId = parts[2];
        }
        return taskId;
    }

}
