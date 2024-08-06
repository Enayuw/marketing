package com.br.marketing.service.Impl.wuba;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.wuba.WuBaChangeSubmitDataDto;
import com.br.marketing.entity.WubaSubmitConversionData;
import com.br.marketing.entity.WubaSubmitConversionDataExample;
import com.br.marketing.mapper.WubaSubmitConversionDataMapper;
import com.br.marketing.monkeydata.entity.commonobj.Page2Condition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description 58新客提交营销名单修改上报数据
 * @Author lixiang
 * @Date 2024-08-05
 */
@Service
@Slf4j
public class WuBaSubmitConversionChangeDataService {

    private static final String TITLE = "【58新客提交营销名单修改上报数据】";
    private Integer PARTITION_SIZE = 50;
    ThreadPoolExecutor dbActionPool = BrExecutors.getThreadPool(10, 10);

    @Resource
    private WubaSubmitConversionDataMapper dataMapper;


    public Result action(Page2Condition<WuBaChangeSubmitDataDto> condition) {
        return scanData(condition);
    }

    public Result scanData(Page2Condition<WuBaChangeSubmitDataDto> condition) {
        Result result = new Result<>().failure();
        try {
            WuBaChangeSubmitDataDto param = condition.getParam();
            String apiCode = param.getApiCode();
            String marketingTimeStart = param.getMarketingTimeStart();
            String marketingTimeEnd = param.getMarketingTimeEnd();

            WubaSubmitConversionData data = new WubaSubmitConversionData();
            data.setStatus(1);
            data.setPushStatus(0);

            WubaSubmitConversionDataExample dataExample = new WubaSubmitConversionDataExample();
            dataExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andMarketingTimeGreaterThanOrEqualTo(marketingTimeStart)
                    .andMarketingTimeLessThan(marketingTimeEnd)
                    .andCellIsNotNull()
                    .andIsDeletedEqualTo(0);
            dataExample.setOrderByClause("id asc limit 2000");
            int updateSize;
            int totalSize = 0;
            updateSize = dataMapper.updateByExampleSelective(data, dataExample);
            totalSize+=updateSize;
            log.warn(TITLE + "修改成功, 总条数{}", totalSize);
        }catch (Exception e){
            log.warn(TITLE + "修改异常");
        }
        return result.success();
    }
}
