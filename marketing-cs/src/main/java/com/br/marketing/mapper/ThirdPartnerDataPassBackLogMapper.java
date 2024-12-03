package com.br.marketing.mapper;

import com.br.marketing.entity.ThirdPartnerDataPassBackLog;
import java.util.List;

public interface ThirdPartnerDataPassBackLogMapper extends ThirdPartnerDataPassBackLogMapperBase{

    int saveBatch(List<ThirdPartnerDataPassBackLog> ThirdPartnerDataPassBackLogList);
}
