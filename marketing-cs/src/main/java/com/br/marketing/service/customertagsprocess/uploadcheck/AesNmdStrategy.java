package com.br.marketing.service.customertagsprocess.uploadcheck;

import com.br.marketing.entity.MonitorTypeEnum;
import org.apache.commons.lang3.StringUtils;

import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.util.aes.AesHexUtil;
import org.springframework.stereotype.Service;

@Service
public class AesNmdStrategy implements IUploadCheckService {

    @Override
    public void check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        if (StringUtils.isNotBlank(user.getCell())) {
            String plainText = AesHexUtil.decrypt(user.getCell(), customerTagsVO.getDynamicKeys());
            if (StringUtils.isNotBlank(plainText)) {
                isValid(user, plainText, "cell", isCheck);
            } else {
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_5.getType());
            }
        }
        if (StringUtils.isNotBlank(user.getId())) {
            String plainText = AesHexUtil.decrypt(user.getId(), customerTagsVO.getDynamicKeys());
            isValid(user, plainText, "id", isCheck);
        }
        if (StringUtils.isNotBlank(user.getName())) {
            String plainText = AesHexUtil.decrypt(user.getName(), customerTagsVO.getDynamicKeys());
            isValid(user, plainText, "name", isCheck);
        }
    }

}
