package com.br.marketing.common.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * Created by jack on 2016/12/4.
 *
 */
public class StringUtils {

    private final static String VALIAFSWIFTNUM = "^[a-z0-9]{16}_[a-z0-9]{16}_[a-z0-9]{11}$";
    private final static String CARD_CDE18 =
            "^((1[1-5])|(2[1-3])|(3[1-7])|(4[1-6])|(5[0-4])|(6[1-5])|71|(8[12])|91)\\d{4}((19\\d{2}(0[13-9]|1[012])(0[1-9]|[12]\\d|30))|(19\\d{2}(0[13578]|1[02])31)|(19\\d{2}02(0[1-9]|1\\d|2[0-8]))|(19([13579][26]|[2468][048]|0[48])0229))\\d{3}(\\d|X|x)?$";
    private final static String CARD_CDE15 =
            "^[1-9][0-9]{5}[0-9]{2}((01|03|05|07|08|10|12)(0[1-9]|[1-2][0-9]|3[0-1])|(04|06|09|11)(0[1-9]|[1-2][0-9]|30)|02(0[1-9]|[1-2][0-9]))[0-9]{3}";
    private final static String USER_NAME="^[a-zA-Z]\\w{5,17}$";
    private final static String NAME="^[\\u4E00-\\u9FA5]{2,10}(?:·[\\u4E00-\\u9FA5]{2,10})*$";
    private final static String VALIMOBILE = "^((\\+86)|(86)|(086))?1[3456789][0-9]\\d{8}$";

    public static boolean isEmpty(Object obj) {
        return (obj == null || obj.toString().length() == 0);
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static boolean isNull(Object obj) {
        return (obj == null);
    }

    public static boolean isNotNull(Object obj) {
        return !isNull(obj);
    }

    public static long time() {
        return System.currentTimeMillis();
    }

    /**
     * 对象转JSON
     */
    public static String parseJSON(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * 获取错误信息
     */
    public static String getErrorMessage(String errJson) {
        try {
            JSONObject json = JSON.parseObject(errJson);
            return json.getString("message");
        } catch (Exception e) {
            return errJson;
        }
    }

    /**
     * 给定字符判断是否为数字{0...9}。
     */
    public final static boolean isDigitString(String str) {
        if (str == null) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    public final static boolean isUsername(String str) {
        Pattern p = Pattern.compile(USER_NAME);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /***
     *
     * 验证中文名字
     *
     * @param name
     *
     * @return
     */

    public static boolean validateNameStr(String name) {
        if(isEmpty(name)){
            return false;
        }
        Pattern pattern = Pattern.compile(NAME);
        Matcher matcher = pattern.matcher(name);
        if(isNotEmpty(name)){
            if(name.length()>= 2 && name.length() <= 30){
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean vaildateAfSwiftNum(String num) {
        Pattern p = Pattern.compile(VALIAFSWIFTNUM, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(num);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }

/*
    *//**
     * 校验手机号（支持MD5解析校验）
     * @param content
     * @return
     *//*
    public static boolean validateMobile(String content) {
        if (content != null && content.length() == 32) {
            String cell = EsMd5Utils.query("cell", content);
            if (StringUtils.isEmpty(cell)) {
                return false;
            }else{
                Pattern p = Pattern.compile(VALIMOBILE);
                Matcher m = p.matcher(cell);
                return m.matches();
            }
        } else if(content == null){
            return false;
        } else {
            Pattern p = Pattern.compile(VALIMOBILE);
            Matcher m = p.matcher(content);
            return m.matches();
        }
    }*/

    public static boolean validateCard(String content) {
        Pattern p = null;
        if (content != null) {
            if (content.length() == 15) {
                p = Pattern.compile(CARD_CDE15);
            } else if (content.length() == 18) {
                p = Pattern.compile(CARD_CDE18);
            } else {
                return false;
            }
            Matcher m = p.matcher(content);
            return m.matches();
        } else {
            return false;
        }

    }

    public static boolean validateDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.parse(date);
            return true;
        } catch (Exception e) {
            try {
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
                dateFormat2.parse(date);
                return true;
            } catch (Exception ex) {
                // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
                return false;
            }
        }
    }

    /**
     * md5
     * @param value
     * @return
     */
    public static String md5(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        String md5 = DigestUtils.md5Hex(value);
        return md5;
    }
    public static boolean isNotBlank(String str) {
        boolean blank = !isBlank(str);
        if (blank && equalsIgnoreCase(str, "null")) {
            blank = false;
        }

        return blank;
    }
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }

        // handle negatives, which means last n characters
        if (start < 0) {
            start = str.length() + start;
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return "";
        }

        return str.substring(start);
    }

    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end;
        }
        if (start < 0) {
            start = str.length() + start;
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }
    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.indexOf(searchStr) >= 0;
    }
    public static String[] split(String str, String separatorChars) {
        return splitWorker(str, separatorChars, -1, false);
    }
    private static String[] splitWorker(String str, String separatorChars, int max, boolean preserveAllTokens) {
        // Performance tuned for 2.0 (JDK1.4)
        // Direct code is quicker than StringTokenizer.
        // Also, StringTokenizer uses isSpace() not isWhitespace()

        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        List list = new ArrayList();
        int sizePlus1 = 1;
        int i = 0, start = 0;
        boolean match = false;
        boolean lastMatch = false;
        if (separatorChars == null) {
            // Null separator means use whitespace
            while (i < len) {
                if (Character.isWhitespace(str.charAt(i))) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else if (separatorChars.length() == 1) {
            // Optimise 1 character case
            char sep = separatorChars.charAt(0);
            while (i < len) {
                if (str.charAt(i) == sep) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        } else {
            // standard case
            while (i < len) {
                if (separatorChars.indexOf(str.charAt(i)) >= 0) {
                    if (match || preserveAllTokens) {
                        lastMatch = true;
                        if (sizePlus1++ == max) {
                            i = len;
                            lastMatch = false;
                        }
                        list.add(str.substring(start, i));
                        match = false;
                    }
                    start = ++i;
                    continue;
                }
                lastMatch = false;
                match = true;
                i++;
            }
        }
        if (match || (preserveAllTokens && lastMatch)) {
            list.add(str.substring(start, i));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }
}
