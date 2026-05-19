package com.br.marketing.service.customertagsprocess.uploadcheck;

import com.br.common.encryption.Md5Utils;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * SM3国密哈希 上传校验策略
 * <p>
 * 客户已明确配置为SM3加密方式，所有数据统一按SM3处理，
 * 通过gRPC调用SM3反查服务获取明文。
 */
@Service
@Slf4j
class Sm3CheckServiceImpl implements IUploadCheckService {

    @Override
    public void check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        encodeMapping(user, "cell", isCheck);
        encodeMapping(user, "id", isCheck);
        encodeMapping(user, "name", isCheck);
    }

    private void encodeMapping(MarketingPreUserDetailDTO user, String type, Integer isCheck) {
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

        // 已确定为SM3，直接走SM3 gRPC反查
        String plainText = RpcClientProxy.decode(content, type, "sm3", "");
        if (StringUtils.isBlank(plainText)) {
            // cell解密失败则报错
            if ("cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_SM3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            // id、name解密失败认为是明文
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

        UserValidator userValidator = new UserValidator(isCheck);
        if ("cell".equals(type)) {
            if (!userValidator.validatePhone(plainText)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            } else {
                user.setCellMd5(Md5Utils.cell32(plainText));
                user.setCellSha256(Sha256Util.getSHA256Encrypt(plainText));
            }
            user.setCell(BrCipherMaker.getInstance().encode(plainText));
        }
        if ("id".equals(type)) {
            userValidator.validateId(plainText);
            user.setId(BrCipherMaker.getInstance().encode(plainText));
        }
        if ("name".equals(type)) {
            userValidator.validateName(plainText);
            user.setName(BrCipherMaker.getInstance().encode(plainText));
        }
    }
}
