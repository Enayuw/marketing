package com.br.marketing.chain.xiecheng.cps;

import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.XieChengCpsCollidingDataLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Slf4j
@Component
public class XieChengReportCollidingCpsHandler extends AbstractXieChengReportHandler {

    @Resource
    private XieChengCpsCollidingDataLogMapper xieChengCpsCollidingDataLogMapper;

    @Override
    public String process(XieChengReportContext context) {
        //1.当日撞库返回为true
        XieChengCpsCollidingDataLog dataLogVt = xieChengCpsCollidingDataLogMapper.selectLatestCpsLog(context.getSha256Tel());
        if(null == dataLogVt) return "撞库释放时间小于当前时间或无返回true的撞库日志";
        if(StringUtils.isBlank(dataLogVt.getOrgChannel())) return "撞库日志获取orgChannel为空";
        context.getAdReqDTO().setMktChannel(dataLogVt.getOrgChannel());
        return null;
    }

    protected XieChengReportCollidingCpsHandler() {
        super("xieChengReportCollidingCps", XieChengBizMarkEnum.CPS.name(), HandlerStageEnum.THREAD.name());
    }
}
