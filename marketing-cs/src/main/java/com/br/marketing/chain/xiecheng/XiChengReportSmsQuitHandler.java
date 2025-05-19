package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.mapper.XiechengSmsQuitDataMapper;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
public class XiChengReportSmsQuitHandler extends AbstractXieChengReportHandler{

    @Resource
    private XiechengSmsQuitDataMapper xiechengSmsQuitDataMapper;

    @Override
    void process(XieChengReportContext context) {
        Integer xiechengSmsQuitDataSize = xiechengSmsQuitDataMapper.getCountSmsQuitDataByMobile(context.getSha256Tel());
        if (xiechengSmsQuitDataSize > 0) {
            context.setError("命中投诉退订数据");
        }
    }

    public XiChengReportSmsQuitHandler() {
        super(2);
    }

}
