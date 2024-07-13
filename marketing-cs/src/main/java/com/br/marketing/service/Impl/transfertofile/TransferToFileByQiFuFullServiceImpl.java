package com.br.marketing.service.Impl.transfertofile;


import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.DynamicParameterServiceImpl;
import com.br.marketing.service.Impl.RuleRedisServiceImpl;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author 贺东硕
 * @Date 2024/07/13 10:46
 * @Description:奇富360转化数据全量提取
 * 2024/07/09 https://c.100credit.cn/pages/viewpage.action?pageId=166648062
 */
@Slf4j
@Service
public class TransferToFileByQiFuFullServiceImpl extends AbstractTransferToFileByQiFuService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    private TableCreateServiceImpl tableCreateService;
    @Autowired
    DynamicParameterServiceImpl dynamicParameterService;

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    String getExtractTime(String apiCode) {
        return marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getString("extTime");
    }

    @Override
    String getSuffix(String apiCode) {
        return marketingCommonConfig.getQiFuExtDataConfig().get(apiCode).getString("suffix");
    }

    @Override
    void writeQifuTransferToFile(Writer fw, String apiCode, TransferFileTask transferFileTask, String requestDate) {
        Long start = System.currentTimeMillis();
        AtomicInteger totalSize = new AtomicInteger(0);
        long timeout = 5L;
        String tcId = tableCreateService.getTcId(apiCode);
        MarketingTransferSyncUser syncUser = new MarketingTransferSyncUser();
        syncUser.settCid(tcId);
        syncUser.setApiCode(apiCode);
        Integer pageSize = dynamicParameterService.getPageSize(null);
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(100, 100, 1);
        boolean isCustom = marketingCommonConfig.getValidityPeriodApiCodeList().contains(apiCode);
        for (; ; ) {

        }
    }

}
