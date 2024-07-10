package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.client.zhijia.ZhiJiaClient;
import com.br.marketing.client.zhijia.input.ReqAddZhiJiaClueDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.ZhiJiaClueBackDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ClassName ZhiJiaClueFeedBackServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2024/7/10 15:44
 */
@Service
@Slf4j
public class ZhiJiaClueFeedBackServiceImpl implements ZhiJiaClueFeedBackService{

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private ZhiJiaClueBackDataMapper zhiJiaClueBackDataMapper;

    @Resource
    private ZhiJiaClient zhiJiaClient;

    @Value("${api.zhijia.zhiJiaClientId:00}")
    private String zhiJiaClientId;

    @Value("${api.zhijia.zhiJiaClientSecret:00}")
    private String zhiJiaClientSecret;

    @Value("${api.zhijia.zhiJiaClientAppid:00}")
    private Integer zhiJiaClientAppid;

    @Override
    public void process() {

        // 创建撞库线程池
        ThreadPoolExecutor zhiJiaCollidingThread =
                BrExecutors.getThreadPool(marketingCommonConfig.getZhiJiaCollidingThread(), marketingCommonConfig.getZhiJiaCollidingThread());

        while (true) {
            // 查询未推送数据
            String now = LocalDate.now().toString();
            ZhiJiaClueBackDataExample example = new ZhiJiaClueBackDataExample();
            example.createCriteria().andStatusEqualTo(1).andPushStatusEqualTo(0)
                    .andCreateDateEqualTo(now);
            example.setOrderByClause("id asc limit 2000");
            List<ZhiJiaClueBackData> zhiJiaClueBackDataList = zhiJiaClueBackDataMapper.selectByExample(example);
            if (zhiJiaClueBackDataList.isEmpty()) {
                break;
            }
            // 执行前将状态改为推送中
            List<Long> ids = zhiJiaClueBackDataList.stream().map(ZhiJiaClueBackData::getId).collect(Collectors.toList());
            zhiJiaClueBackDataMapper.updateBatchById(ids,1);
            // 开始推送
            zhiJiaCollidingThread.execute(() -> pushZhiJiaCollidingSync(zhiJiaClueBackDataList));
        }
        zhiJiaCollidingThread.shutdown();
        try {
            while (!zhiJiaCollidingThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("之家创建线索接口线程池关闭");
            }
        } catch (InterruptedException ex) {
            zhiJiaCollidingThread.shutdownNow();
            log.error("之家创建线索接口线程池关闭！异常", ex);
            Thread.currentThread().interrupt();
        }

    }

    public void pushZhiJiaCollidingSync(List<ZhiJiaClueBackData> zhiJiaClueBackDataList){

        for (ZhiJiaClueBackData zhiJiaClueBackData : zhiJiaClueBackDataList) {
            Long id = zhiJiaClueBackData.getId();
            // 调用省市区接口
            // 调用车辆信息接口
            try {
                // 调用高质线索创建接口
                Result result = zhiJiaClient.addZhiJiaClue(buildAddZhiJiaClue());
                // 更新结果
                if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    //更新成功
                    updatePushStatus(id, 2, result.getMessage());
                } else {
                    //更新失败
                    updatePushStatus(id, 3, result.getMessage());
                }
            }catch (Exception e){
                log.error("调用高质线索创建接口异常！", e.getMessage());
            }
        }
    }

    private void updatePushStatus(Long id, int status, String message) {
        ZhiJiaClueBackDataExample example = new ZhiJiaClueBackDataExample();
        example.createCriteria().andIdEqualTo(id);
        ZhiJiaClueBackData record = new ZhiJiaClueBackData();
        record.setPushStatus(status);
        record.setErrorMsg(message);
        zhiJiaClueBackDataMapper.updateByExampleSelective(record,example);
    }

    private ReqAddZhiJiaClueDTO buildAddZhiJiaClue() {
        // 获取access_token
        // 组装入参
        ReqAddZhiJiaClueDTO dto = new ReqAddZhiJiaClueDTO();
        dto.setAccess_token("");
        dto.setMobile("");
        dto.setMobilecode("");
        dto.setCid(1);
        dto.setCountyid(1);
        dto.setBrandid("");
        dto.setSeriesid("");
        dto.setSpecid("");
        dto.setFirstregtime("");
        dto.setPlatenum("");
        dto.setMileage("");
        dto.setAppid(zhiJiaClientAppid);
        return dto;
    }

}
