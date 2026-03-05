package com.br.marketing.service.customertagsprocess.uploadcheck;

import com.br.common.encryption.Md5Utils;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class CheckCellServiceImpl implements IUploadCheckService {
    @Override
    public void  check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        encodeMapping(user, "cell", isCheck);
        encodeMapping(user, "id", isCheck);
        encodeMapping(user, "name", isCheck);
    }

    private void encodeMapping(MarketingPreUserDetailDTO user, String type, Integer isCheck) {
        String content = "";
        Boolean isMw = Boolean.TRUE;
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
        if (DecodeGrpcClient.isMd5(content)) {
            //cell md5
            isMw = Boolean.FALSE;
            content = RpcClientProxy.decode(content, type, "md5", "");
            if (StringUtils.isBlank(content) && "cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_1.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        } else if (content.length() == 64) {
            //cell sha256
            isMw = Boolean.FALSE;
            content = RpcClientProxy.decode(content, type, "sha", "");
            if (StringUtils.isBlank(content) && "cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_2.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        }
        //明文规则校验 md5和sha256解密失败content为空
        UserValidator userValidator = new UserValidator(isCheck);
        if (StringUtils.isNotEmpty(content) && "cell".equals(type)) {
            if (!userValidator.validatePhone(content)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_3.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            } else {
                user.setCellMd5(Md5Utils.cell32(content));
                user.setCellSha256(Sha256Util.getSHA256Encrypt(content));
            }
            if(isMw){
                user.setCellOriginal(BrCipherMaker.getInstance().encode(content));
            }
            user.setCell(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotEmpty(content) && "id".equals(type)) {
            if (!userValidator.validateId(content)) {
                user.setId(content);
            }
            if(isMw){
                user.setIdOriginal(BrCipherMaker.getInstance().encode(content));
            }
            user.setId(BrCipherMaker.getInstance().encode(content));
        }
        if (StringUtils.isNotEmpty(content) && "name".equals(type)) {
            if (!userValidator.validateName(content)) {
                user.setName(content);
                /** 2022/8/11 17:14 业务需求变更，name字段是否成功解密不影响数据状态 */
//                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
            if(isMw){
                user.setNameOriginal(BrCipherMaker.getInstance().encode(content));
            }
            user.setName(BrCipherMaker.getInstance().encode(content));
        }
    }
}
