package com.br.marketing.service.customertagsprocess.uploadcheck;

import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.util.aes.AesSllUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @ClassName AesSllCollectionStrategy
 * @Description 360催收数据清洗
 * @Author kongbx
 * @Date 2025/9/13 17:13
 */
@Service
public class AesSllCollectionStrategy implements IUploadCheckService {

    @Override
    public void check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        if (StringUtils.isNotBlank(user.getCell())) {
            String plainText = AesSllUtil.decrypt(user.getCell(), customerTagsVO.getDynamicKeys());
            if (StringUtils.isNotBlank(plainText)) {
                isValid(user, plainText, "cell", isCheck);
            } else {
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_5.getType());
            }
        }
        if (StringUtils.isNotBlank(user.getName())) {
            String plainText = AesSllUtil.decrypt(user.getName(), customerTagsVO.getDynamicKeys());
            isValid(user, plainText, "name", isCheck);
        }
        //String reserveField1 = user.getReserveField1();
        //if(StringUtils.isNotBlank(reserveField1)){
        //    JSONObject jsonObject = JSONObject.parseObject(reserveField1);
        //    String plainText = AesSllUtil.decrypt(jsonObject.getString("template_no"), customerTagsVO.getDynamicKeys());
        //    if (StringUtils.isNotBlank(plainText)) {
        //        isValid(user, plainText, "cell", isCheck);
        //    }
        //}

    }

}
