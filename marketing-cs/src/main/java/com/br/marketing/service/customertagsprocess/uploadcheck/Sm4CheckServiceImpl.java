package com.br.marketing.service.customertagsprocess.uploadcheck;

import com.br.marketing.dto.AesGeneralDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.util.sm4.Sm4Util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * SM4国密对称加密 上传校验策略
 * <p>
 * 页面配置密钥、加密模式、填充模式等参数，与AES策略逻辑一致，
 * 底层调用 Sm4Util (BouncyCastle) 进行解密。
 */
@Service
@Slf4j
public class Sm4CheckServiceImpl implements IUploadCheckService {

    @Override
    public void check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        AesGeneralDTO dto = new AesGeneralDTO();
        dto.setCipherMode(customerTagsVO.getCipherMode());
        dto.setPaddingScheme(customerTagsVO.getPaddingScheme());
        dto.setCharset(customerTagsVO.getCharset());
        dto.setDynamicKeys(customerTagsVO.getDynamicKeys());
        dto.setIv(customerTagsVO.getIv());

        if (StringUtils.isNotBlank(user.getCell())) {
            dto.setText(user.getCell());
            String plainText = Sm4Util.decrypt(dto);
            if (StringUtils.isNotBlank(plainText)) {
                isValid(user, plainText, "cell", isCheck);
            } else {
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_SM4.getType());
            }
        }

        if (StringUtils.isNotBlank(user.getId())) {
            dto.setText(user.getId());
            String plainText = Sm4Util.decrypt(dto);
            isValid(user, plainText, "id", isCheck);
        }

        if (StringUtils.isNotBlank(user.getName())) {
            dto.setText(user.getName());
            String plainText = Sm4Util.decrypt(dto);
            isValid(user, plainText, "name", isCheck);
        }
    }
}
