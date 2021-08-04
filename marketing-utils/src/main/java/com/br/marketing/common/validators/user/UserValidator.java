package com.br.marketing.common.validators.user;

import cn.hutool.core.util.IdcardUtil;
import com.br.common.validator.*;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.regex.Pattern;

/**
 * 参数校验
 *
 * @since 2018/3/12
 */
@Slf4j
public class UserValidator {
    private static final Pattern CUS_NUM_PAT = Pattern.compile(Constants.CUS_NUM_REGEX);

    /**
     * 交付系统配置的校验类型
     * 不需要：0;
     * 需要：1;
     * 不需要(通用强校验)：2;
     * 需要(通用强校验)：3;
     * 不需要(通用弱校验)：4;
     * 需要(通用弱校验)：5;
     */
    private Integer isCheck;

    public UserValidator(Integer isCheck) {
        this.isCheck = isCheck;
    }

    /**
     * 校验身份证号是否合法
     * 根据交付系统的配置区分校验方式
     * 2、3强校验
     * 4、5弱校验
     * 其他情况使用原有校验方式
     *
     * @param id 身份证号
     * @return 校验结果
     */
    public boolean validateId(String id) {
        log.debug("UserValidator isCheck:{}", this.isCheck);
        if (this.isCheck == 2 || this.isCheck == 3) {
            return IdcardUtils.isStrongValidCard(id);
        } else if (this.isCheck == 4 || this.isCheck == 5) {
            return IdcardUtils.isWeakValidCard(id);
        } else if (!IdcardUtil.isValidCard(id)) {
            return false;
        }
        return true;
    }

    /**
     * 校验手机号
     * 根据交付系统的配置采用不同的校验方式
     * 0、1使用原有校验方式
     * 其他情况使用同一校验方式
     *
     * @param phone 手机号
     * @return 校验结果
     */
    public boolean validatePhone(String phone) {
        if (this.isCheck == 0 || this.isCheck == 1) {
            if (!(new PhoneValidator()).valid(phone)) {
                return false;
            }
        } else {
            return CellUtils.isValidateCell(phone);
        }
        return true;
    }


    /**
     * 日期校验
     * 根据交付系统的配置采用不同的校验方式
     * 0、1使用原有校验方式
     * 其他情况使用同一校验方式
     *
     * @param date 日期
     * @return 校验结果
     */
    public boolean validateDate(String date) {
        if (this.isCheck == 0 || this.isCheck == 1) {
            try {
                DateUtils.parseDateByString(date, "yyyy-MM-dd");
            } catch (ParseException e) {
                return false;
            }
            return true;
        } else {
            return DateUtils.isValidateYMD(date);
        }
    }

    /**
     * 邮箱地址校验
     * 使用公司统一校验方式
     *
     * @param email 邮箱地址
     * @return 校验结果
     */
    public boolean validateEmail(String email) {
        if (!EmailUtils.isValidateMail(email)) {
            return false;
        }
        return true;
    }

    /**
     * 姓名校验
     * 根据交付系统的配置采用个不同的校验方式
     * 0、1使用原有的校验方式
     * 其他情况使用公司的统一校验方式
     *
     * @param name 姓名
     * @return 校验结果
     */
    public boolean validateName(String name) {
        if (this.isCheck == 0 || this.isCheck == 1) {
            if (name.length() > 30 || !new NameValidator().valid(name)) {
                return false;
            }
        } else {
            return NameUtils.isValidateName(name);
        }
        return true;
    }

    /**
     * 客户编号校验
     * 使用原有校验方式
     *
     * @param cusNum 客户编号
     * @return 校验结果
     */
    public boolean validateCusNum(String cusNum) {
        if (StringUtils.isBlank(cusNum)) {
            return false;
        }
        if (!CUS_NUM_PAT.matcher(cusNum).matches()) {
            return false;
        }
        return true;
    }
}
