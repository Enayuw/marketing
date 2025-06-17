package com.br.marketing.chain.xiecheng.cps;

import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVt;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVtExample;
import com.br.marketing.entity.XieChengSmsCollidingDataVt;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.XieChengSmsCollidingDataLogVtMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataVtMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class XieChengReportCollidingCpsHandler extends AbstractXieChengReportHandler {

    @Resource
    private XieChengSmsCollidingDataLogVtMapper xieChengSmsCollidingDataLogVtMapper;

    @Resource
    private XieChengSmsCollidingDataVtMapper xieChengSmsCollidingDataVtMapper;

    @Override
    public String process(XieChengReportContext context) {
        //1.当日撞库返回为true
        Integer day = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        XieChengSmsCollidingDataLogVtExample logVtExample = new XieChengSmsCollidingDataLogVtExample();
        logVtExample.createCriteria()
                .andSha256CodeListEqualTo(context.getSha256Tel())
                .andStatusEqualTo(2)
                .andSendDateEqualTo(day);
        List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVts = xieChengSmsCollidingDataLogVtMapper.selectByExample(logVtExample);
        if (xieChengSmsCollidingDataLogVts.size() == 0) return "没有获取到当日撞库结果";
        XieChengSmsCollidingDataLogVt dataLogVt = xieChengSmsCollidingDataLogVts.get(0);
        if (!dataLogVt.getResult()) return "命中当日撞库结果为false";
        if (StringUtils.isBlank(dataLogVt.getOrgChannel())) {
            return "命中当日OrgChannel为空,id=" + dataLogVt.getSmsCollidingDataVtId();
        }
        context.getAdReqDTO().setMktChannel(dataLogVt.getOrgChannel());
        //2.上报时间<releasetime
        Boolean isPush = xieChengSmsCollidingDataVtMapper.selectMaxNextPushTimetikv_(context.getSha256Tel());
        if(!isPush) return "撞库释放时间小于当前时间";
        return null;
    }

    protected XieChengReportCollidingCpsHandler() {
        super("xieChengReportCollidingCps", XieChengBizMarkEnum.CPS.name(), HandlerStageEnum.THREAD.name());
    }
}
