package com.br.marketing.common.validators.user;


import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.validators.Valid;

import java.util.regex.Pattern;

/**
 * 姓名验证器
 *
 * @author Wang Weiwei
 * @since 2018/3/12
 */
public class NameValidator implements Valid<String> {
    private static final Pattern NAME_PATTERN = Pattern.compile("[\\u4E00-\\u9FA5]{2,27}(?:·[\\u4E00-\\u9FA5]{2,27})*");
    private static final Pattern NAME_PATTERN1 = Pattern.compile("[\\u4E00-\\u9FA5]{2,30}");

    NameValidator() {
    }

    @Override
    public boolean valid(String param) {
        if (StringUtils.isNotBlank(param) && (param.length() >= 2 && param.length() <= 30)) {
            if (param.contains("·")) {
                return NAME_PATTERN.matcher(param).matches();
            } else {
                return NAME_PATTERN1.matcher(param).matches();
            }
        }
        return false;
    }
}
