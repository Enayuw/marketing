package com.br.marketing.chain.xiecheng.common;

import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.XieChengDataMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class XieChengReportRepeatHandler extends AbstractXieChengReportHandler {

    @Resource
    private XieChengDataMapper xieChengDataMapper;

    @Override
    public String process(XieChengReportContext context) {
        Boolean isPush;
        Boolean isDelete = false;
        if (context.getPushConfig().getOffRepeatByPeriod()) {
            List<Integer> pushStatusList =
                    xieChengDataMapper.getReportPushStatusInPeriod(context.getSha256Tel(), context.getApiCode());
            if (CollectionUtils.isEmpty(pushStatusList)) {
                isPush = false;
                isDelete = true;
            } else {
                Integer pushCount = pushStatusList.stream().filter(pushStatus -> pushStatus == 2)
                        .collect(Collectors.toList()).size();
                isPush = pushCount < context.getPushConfig().getOffRepeatCount();
            }
        } else {
            List<XieChengData> xieChengRepeatDatalist =
                    xieChengDataMapper.getByCellToday(context.getSha256Tel(),context.getPushConfig().getSoleCellApiCodes());
            isPush = CollectionUtils.isEmpty(xieChengRepeatDatalist);
        }
        if (!isPush) {
            if (context.getPushConfig().getOffRepeatByPeriod()) {
                if (isDelete) {
                    return "数据不在锁定期内，不可推送";
                } else {
                    return "数据在锁定期内已推送过" + context.getPushConfig().getOffRepeatCount() + "次";
                }
            } else {
                return "数据重复未推送";
            }
        }
        return null;
    }

    protected XieChengReportRepeatHandler() {
        super("xieChengReportRepeat", XieChengBizMarkEnum.COMMON.name(), HandlerStageEnum.PRE.name());
    }

}
