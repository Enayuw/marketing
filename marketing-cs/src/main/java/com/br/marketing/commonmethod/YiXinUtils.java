package com.br.marketing.commonmethod;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.userdata.DassBatchImportDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class YiXinUtils {
    public static String getDxType(String type){
        switch (type){
            case "13":
                return "21";
            case "12":
                return "30";
            case "9":
                return "17";
            case "7":
                return "15";
            case "17":
                return "26";
            case "18":
                return "27";
            case "6":
                return "14";
            case "8":
                return "29";
            case "4":
                return "12";
            case "15":
                return "23";
            case "0":
                return "0";
            case "25":
                return "25";
            default:
                return null;
        }
    }

    public static String getLevel(String grade){
        if(StringUtils.isEmpty(grade)){
            return grade;
        }
        switch (grade.toUpperCase()){
            case "A":
                return "A级(有明确意向)";
            case "B":
                return "B级(可能有意向)";
            case "C":
                return "C级(明确拒绝)";
            case "D":
                return "D级(用户忙)";
            case "E":
                return "E级(拨打失败)";
            case "F":
                return "F级(无效客户)";
            default:
                return null;
        }
    }

    public static String getPrioritySymbol(String type){
        switch (type){
            case "13":
            case "9":
            case "7":
            case "6":
            case "8":
            case "25":
            case "0":
                return "1";
            case "12":
            case "17":
            case "15":
                return "2";
            case "18":
            case "4":
                return "3";
            default:
                return null;
        }
    }

    public static String getGender(String gender){
        if ("0".equals(gender)){
            return  "女";
        }else if ("1".equals(gender)){
            return "男";
        }
        return gender;
    }

    public static String getActivity(String rate){
        if ("1".equals(rate)){
            return "2";
        } else if ("2".equals(rate)){
            return "4";
        }
        return rate;
    }
}
