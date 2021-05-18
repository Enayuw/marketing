package com.br.marketing.common.validators.user;


import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.validators.Valid;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Wang Weiwei
 * @since 2018/3/12
 */
public class PhoneValidator implements Valid<String> {
private static final Pattern SIMPLE_PHONE = Pattern.compile("^[1][3-9][0-9]{9}$");
private static final Pattern COMPLEX_PHONE = Pattern.compile("^((\\+86)|(86)|(086))?[1][3456789][0-9]{9}$");

        PhoneValidator() {
        }

@Override
public boolean valid(String param) {
        if(StringUtils.isEmpty(param)){
            return false;
        }
        Matcher m = SIMPLE_PHONE.matcher(param);
        boolean b = m.matches();
        if (!b) {
            m = COMPLEX_PHONE.matcher(param);
            if (m.matches()) {
                return true;
            }
        }

        return b;
    }
}
