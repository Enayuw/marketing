package com.br.marketing.service.Impl.sanliuling;

import com.br.common.log.AlertLog;
import com.br.marketing.client.sanliuling.SanLiuLingClient;
import com.br.marketing.client.sanliuling.SanLiuLingTrafficReq;
import com.br.marketing.client.sanliuling.SanLiuLingTrafficResp;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.SanLiuLingPpData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.SanLiuLingPpDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName SanLiuLingApiServiceImpl
 * @Author kongbx
 * @Date 2025/6/20 15:54
 */
@Service
@Slf4j
public class SanLiuLingApiServiceImpl implements SanLiuLingApiService {

    @Resource
    private SanLiuLingPpDataMapper sanLiuLingPpDataMapper;
    @Resource
    private SanLiuLingClient sanLiuLingClient;
    @Autowired
    LocalFileMapper localFileMapper;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    private final static String TITLE = "【360-pp流量业务营销】";

    @Override
    public void pushTrafficData(LocalFile localFile) {
        localFile.setPushStartTime(new Date());
        boolean actionMark = true;
        Long minId = null;
        int total = 0;
        while (actionMark) {
            List<SanLiuLingPpData> dataList = sanLiuLingPpDataMapper.getTrafficData(localFile.getId(), minId);
            if (dataList.isEmpty()) {
                actionMark = false;
                continue;
            }

            List<String> mobileMd5List = dataList.stream()
                    .map(SanLiuLingPpData::getMobileNoMd5)
                    .collect(Collectors.toList());

            SanLiuLingTrafficReq sanLiuLingTrafficReq = new SanLiuLingTrafficReq();
            sanLiuLingTrafficReq.setChannel("brllt");
            sanLiuLingTrafficReq.setMobileMd5(mobileMd5List);

            total += dataList.size();
            minId = dataList.get(dataList.size() - 1).getId();
            Result result = sanLiuLingClient.batchTrafficData(sanLiuLingTrafficReq);

            if (Objects.equals(result.getCode(), ResultCode.SUCCESS.getValue())) {
                Object data = result.getData();
                if (data instanceof SanLiuLingTrafficResp) {
                    SanLiuLingTrafficResp sanLiuLingTrafficResp = (SanLiuLingTrafficResp) data;
                    if ("200".equals(sanLiuLingTrafficResp.getCode())) {
                        List<String> respMobileMd5List = sanLiuLingTrafficResp.getData().getMobile_md5();

                        // 校验返回数量和请求数量一致
                        if (respMobileMd5List.size() != dataList.size()) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SANLIULING_SERVICEERROR.getCode()
                                    , "返回的mobileMd5数量与请求数量不一致"));
                        }

                        for (int i = 0; i < dataList.size(); i++) {
                            SanLiuLingPpData updateData = new SanLiuLingPpData();
                            updateData.setId(dataList.get(i).getId());
                            updateData.setMobileResult(respMobileMd5List.get(i));
                            sanLiuLingPpDataMapper.updateByPrimaryKeySelective(updateData);
                        }
                    }
                }
            }
        }
        localFile.setPushEndTime(new Date());
        localFile.setPushNumber(total);
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
    }

}
