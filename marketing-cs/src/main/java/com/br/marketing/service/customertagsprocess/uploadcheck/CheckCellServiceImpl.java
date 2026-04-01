package com.br.marketing.service.customertagsprocess.uploadcheck;

import com.br.common.encryption.Md5Utils;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MonitorTypeEnum;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import com.br.marketing.service.ICustomerConfigService;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.IUploadCheckService;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.service.customertagsprocess.vo.CustomerTagsVO;

import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@Service
@Slf4j
public class CheckCellServiceImpl implements IUploadCheckService {

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private ICustomerConfigService customerConfigService;

    @Resource
    private CustomerTagsProcessServiceImpl customerTagsProcessService;

    @Override
    public void  check3key(MarketingPreUserDetailDTO user, Integer isCheck, CustomerTagsVO customerTagsVO) {
        encodeMapping(user, "cell", isCheck, customerTagsVO);
        encodeMapping(user, "id", isCheck, customerTagsVO);
        encodeMapping(user, "name", isCheck, customerTagsVO);
    }

    private void encodeMapping(MarketingPreUserDetailDTO user, String type, Integer isCheck, CustomerTagsVO customerTagsVO) {
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
            isMw = Boolean.FALSE;
            content = RpcClientProxy.decode(content, type, "md5", "");
            if (StringUtils.isBlank(content) && "cell".equals(type)) {
                user.setFailType(MonitorTypeEnum.FAIL_TYPE_1.getType());
                user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
            }
        } else if (content.length() == 64) {
            isMw = Boolean.FALSE;
            String originalHash = content;
            content = RpcClientProxy.decode(originalHash, type, "sha", "");
            if (StringUtils.isBlank(content)) {
                content = RpcClientProxy.decode(originalHash, type, "sm3", "");
                if (StringUtils.isNotBlank(content)) {
                    tryUpgradeToSm3(customerTagsVO);
                } else if ("cell".equals(type)) {
                    user.setFailType(MonitorTypeEnum.FAIL_TYPE_2.getType());
                    user.setStatus(MonitorTypeEnum.STATUS_2.getTypeCode());
                }
            }
        }
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
            }
            if(isMw){
                user.setNameOriginal(BrCipherMaker.getInstance().encode(content));
            }
            user.setName(BrCipherMaker.getInstance().encode(content));
        }
    }

    /**
     * SM3反查成功后加锁更新加密类型，后续数据直接走SM3策略
     */
    private void tryUpgradeToSm3(CustomerTagsVO customerTagsVO) {
        String apiCode = customerTagsVO != null ? customerTagsVO.getApiCode() : null;
        if (StringUtils.isBlank(apiCode)) {
            return;
        }
        String lockKey = RedisKeyConstant.ENCRYPT_UPGRADE_SM3_LOCK + apiCode;
        String requestId = UUID.randomUUID().toString();
        try {
            boolean locked = redisChgService.lock(lockKey, requestId, 5000L);
            if (locked) {
                try {
                    customerConfigService.updateEncryptyType(apiCode,
                            CustomerTagsValue.PushJc3keyTypeEnum.SM3.getValue());
                    customerTagsProcessService.delTagsOfRedis(apiCode);
                    log.warn("apiCode={} 检测到SM3数据，加密类型自动升级为SM3", apiCode);
                } finally {
                    redisChgService.unlock(lockKey, requestId);
                }
            }
        } catch (Exception e) {
            log.error("SM3加密类型自动升级失败, apiCode={}", apiCode, e);
        }
    }
}
