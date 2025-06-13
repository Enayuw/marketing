package com.br.marketing.chain.xiecheng.common;

import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.XiechengSmsQuitDataMapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class XieChengReportSmsQuitHandler extends AbstractXieChengReportHandler {

    @Resource
    private XiechengSmsQuitDataMapper xiechengSmsQuitDataMapper;

    @Override
    public String process(XieChengReportContext context) {
        Integer xiechengSmsQuitDataSize = xiechengSmsQuitDataMapper.getCountSmsQuitDataByMobile(context.getSha256Tel());
        if (xiechengSmsQuitDataSize > 0) return "命中投诉退订数据";
        return null;
    }

    public XieChengReportSmsQuitHandler() {
        super("xieChengReportSmsQuit", XieChengBizMarkEnum.COMMON.name(), HandlerStageEnum.THREAD.name());
    }

}
