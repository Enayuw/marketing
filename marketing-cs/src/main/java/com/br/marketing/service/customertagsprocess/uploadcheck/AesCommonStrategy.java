package com.br.marketing.service.customertagsprocess.uploadcheck;

import org.apache.commons.lang3.StringUtils;
import com.br.marketing.dto.AesGeneralDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;
import com.br.marketing.util.aes.AesUtil;
import org.springframework.stereotype.Service;

@Service
public class AesCommonStrategy implements IUploadCheckService{

    @Override
    public void check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        AesGeneralDTO aesGeneralDTO = new AesGeneralDTO();
        aesGeneralDTO.setCipherMode(customerTagsVO.getCipherMode());
        aesGeneralDTO.setPaddingScheme(customerTagsVO.getPaddingScheme());
        aesGeneralDTO.setCharset(customerTagsVO.getCharset());
        aesGeneralDTO.setDynamicKeys(customerTagsVO.getDynamicKeys());
        if(StringUtils.isNotBlank(user.getCell())){
            aesGeneralDTO.setText(user.getCell());
            String plainText = AesUtil.decrypt(aesGeneralDTO);
            isValid(user, plainText, "cell", isCheck);
        }

        if(StringUtils.isNotBlank(user.getId())){
            aesGeneralDTO.setText(user.getId());
            String plainText = AesUtil.decrypt(aesGeneralDTO);
            isValid(user, plainText, "id", isCheck);
        }

        if(StringUtils.isNotBlank(user.getName())){
            aesGeneralDTO.setText(user.getName());  
            String plainText = AesUtil.decrypt(aesGeneralDTO);
            isValid(user, plainText, "name", isCheck);
        }
    }
}
