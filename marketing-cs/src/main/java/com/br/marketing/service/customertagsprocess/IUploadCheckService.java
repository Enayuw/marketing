package com.br.marketing.service.customertagsprocess;

import org.apache.commons.lang3.StringUtils;
import com.br.common.util.BrCipherMaker;
import com.br.common.encryption.Md5Utils;
import com.br.common.encryption.Sha256Util;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;

import java.util.Map;

public interface IUploadCheckService {

    default void process3keyCheck(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO tagsVO) {
        // 保存原始值到Original字段
        user.setCellOriginal(user.getCell());
        user.setIdOriginal(user.getId());
        user.setNameOriginal(user.getName());

        // 调用具体的3key校验实现
        check3key(user, isCheck, tagsVO);
    }

    /**
     * 3K值 规则校验
     *
     * @param user
     * @param content
     * @param type
     * @param isCheck
     */
    default void isValid(MarketingPreUserDetailDTO user, String content, String type, Integer isCheck) {
        UserValidator userValidator = new UserValidator(isCheck);
        if (StringUtils.isNotBlank(content) && "cell".equals(type)) {
            if (!userValidator.validatePhone(content)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            } else {
                user.setCellMd5(Md5Utils.cell32(content));
                user.setCellSha256(Sha256Util.getSHA256Encrypt(content));
                user.setCell(BrCipherMaker.getInstance().encode(content));
            }
        } else if (StringUtils.isNotBlank(content) && "id".equals(type)) {
            if (userValidator.validateId(content)) {
                user.setId(BrCipherMaker.getInstance().encode(content));
            }
        } else if (StringUtils.isNotBlank(content) && "name".equals(type)) {
            if (userValidator.validateName(content)) {
                user.setName(BrCipherMaker.getInstance().encode(content));
            }
        }
    }

    void check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO tagsVO);

}
