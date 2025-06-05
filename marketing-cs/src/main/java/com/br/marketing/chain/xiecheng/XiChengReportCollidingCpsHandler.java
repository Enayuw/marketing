package com.br.marketing.chain.xiecheng;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVt;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVtExample;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.XieChengSmsCollidingDataLogVtMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class XiChengReportCollidingCpsHandler extends AbstractXieChengReportHandler {

    @Resource
    private XieChengSmsCollidingDataLogVtMapper xieChengSmsCollidingDataLogVtMapper;

    @Override
    void process(XieChengReportContext context) {
        Integer day = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        XieChengSmsCollidingDataLogVtExample vtExample = new XieChengSmsCollidingDataLogVtExample();
        vtExample.createCriteria()
                .andSha256CodeListEqualTo(context.getSha256Tel())
                .andStatusEqualTo(2)
                .andSendDateEqualTo(day);
        List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVts = xieChengSmsCollidingDataLogVtMapper.selectByExample(vtExample);
        if (xieChengSmsCollidingDataLogVts.size() == 0) {
            context.setError("没有获取到当日撞库结果");
            return;
        }
        XieChengSmsCollidingDataLogVt dataLogVt = xieChengSmsCollidingDataLogVts.get(0);
        if(!dataLogVt.getResult()){
            context.setError("命中当日撞库结果为false");
            return;
        }
        if (StringUtils.isBlank(dataLogVt.getOrgChannel())) {
            context.setError("命中当日OrgChannel为空,id="+dataLogVt.getSmsCollidingDataVtId());
            return;
        }
        context.getAdReqDTO().setMktChannel(dataLogVt.getOrgChannel());
    }

    protected XiChengReportCollidingCpsHandler() {
        super(4, XieChengBizMarkEnum.CPS.name());
    }
}
