package com.br.marketing.check.service.Impl.clean.upload;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.service.IFileToMarketingRuleService;
import com.br.marketing.vo.FileToMarketingDataFieldVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @ClassName ZhaoLianFileToMarketingRuleServiceImpl
 * @Description 招联转化数据清洗实现
 * @Author kongbx
 * @Date 2025/5/26 19:29
 */
@Service
public class ZhaoLianFileToMarketingRuleServiceImpl implements IFileToMarketingRuleService {

    @Override
    public Result isVaild(List<FileToMarketingDataFieldVO> vos, Map<String, FileToMarketingDataFieldVO> voMaps) {
        return IFileToMarketingRuleService.super.isVaild(vos, voMaps);
    }

    @Override
    public MarketingPreUserDetailDTO make(List<FileToMarketingDataFieldVO> vos) {
        return IFileToMarketingRuleService.super.make(vos);
    }

    @Override
    public String getTaskId(String apiCode, String filename) {
        // 文件示例：original_yyyymmdd_E0012250428069BR_01.txt
        String[] parts = filename.split("_");
        String taskId = "";
        if (parts.length >= 3) {
            taskId = parts[2];
        }
        return taskId;
    }

}