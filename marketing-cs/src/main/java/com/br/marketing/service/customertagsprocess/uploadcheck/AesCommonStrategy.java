package com.br.marketing.service.customertagsprocess.uploadcheck;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.dto.AesGeneralDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.util.aes.AesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 通用 AES 上传校验。
 * <p>
 * 与 {@link Sm4CheckServiceImpl} 一致：cell 解密失败则判失败；id、name 解密失败则按明文入库（系统内再加密）。
 */
@Service
public class AesCommonStrategy implements IUploadCheckService {

    @Override
    public void check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        AesGeneralDTO dto = buildDto(customerTagsVO);
        encodeMapping(user, "cell", isCheck, dto);
        encodeMapping(user, "id", isCheck, dto);
        encodeMapping(user, "name", isCheck, dto);
    }

    private static AesGeneralDTO buildDto(CustomerTagsVO customerTagsVO) {
        AesGeneralDTO dto = new AesGeneralDTO();
        dto.setCipherMode(customerTagsVO.getCipherMode());
        dto.setPaddingScheme(customerTagsVO.getPaddingScheme());
        dto.setCharset(customerTagsVO.getCharset());
        dto.setDynamicKeys(customerTagsVO.getDynamicKeys());
        dto.setIv(customerTagsVO.getIv());
        return dto;
    }

    private void encodeMapping(MarketingPreUserDetailDTO user, String type, Integer isCheck, AesGeneralDTO dto) {
        String content = "";
        switch (type) {
            case "cell":
                content = StringUtils.isEmpty(user.getCell()) ? "" : user.getCell();
                break;
            case "id":
                content = StringUtils.isEmpty(user.getId()) ? "" : user.getId();
                break;
            case "name":
                content = StringUtils.isEmpty(user.getName()) ? "" : user.getName();
                break;
            default:
                return;
        }

        if (StringUtils.isBlank(content)) {
            return;
        }

        dto.setText(content);
        String plainText = AesUtil.decrypt(dto);
        if (StringUtils.isBlank(plainText)) {
            if ("cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_4.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            if ("id".equals(type)) {
                user.setIdOriginal(BrCipherMaker.getInstance().encode(content));
                user.setId(BrCipherMaker.getInstance().encode(content));
            }
            if ("name".equals(type)) {
                user.setNameOriginal(BrCipherMaker.getInstance().encode(content));
                user.setName(BrCipherMaker.getInstance().encode(content));
            }
            return;
        }

        isValid(user, plainText, type, isCheck);
    }
}
