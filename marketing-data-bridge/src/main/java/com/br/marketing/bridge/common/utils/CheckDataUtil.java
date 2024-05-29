package com.br.marketing.bridge.common.utils;

import com.br.common.encryption.BrCipherMaker;
import com.br.common.validator.DateUtils;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.entity.MerchantParam;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bairong on 2020/5/16.
 */

@Slf4j
public class CheckDataUtil {
    /**
     * 交付系统配置为不校验必填项时，3k必须至少有一个
     * @param rows 数据行
     * @param idIndex id的位置
     * @param nameIndex name的位置
     * @param cellIndex cell的位置
     * @return 是否校验通过
     */
    private static boolean checkNotRequired(String[] rows, int idIndex, int nameIndex, int cellIndex) {
        boolean flag = false;
        if (idIndex == -1 && nameIndex == -1 && cellIndex == -1) {
            return flag;
        } else {
            String id = "";
            String name = "";
            String cell = "";
            if (idIndex != -1) {
                try {
                    id = rows[idIndex];
                } catch (ArrayIndexOutOfBoundsException e) {

                }
            }
            if (nameIndex != -1) {
                try {
                    name = rows[nameIndex];
                } catch (ArrayIndexOutOfBoundsException e) {

                }
            }
            if (cellIndex != -1) {
                try {
                    cell = rows[cellIndex];
                } catch (ArrayIndexOutOfBoundsException e) {

                }
            }
            if (StringUtils.isEmpty(id) && StringUtils.isEmpty(name) && StringUtils.isEmpty(cell)) {
                return flag;
            } else {
                flag = true;
            }
        }
        return flag;
    }

    /**
     * 校验当前行的数据是否合法
     * @param head 表头
     * @param row 数据行
     * @param apiCdoe apiCdoe
     * @param errorfw 错误结果输出
     * @param sb 解密校验处理完的数据行
     * @param decodeClient 解密客户端
     * @return 校验是否通过
     * @throws IOException
     */
    public static boolean checkData(String head, String row, String apiCdoe,
                                    Writer errorfw, StringBuilder sb,  DecodeGrpcClient decodeClient) throws IOException {
        boolean flag = true;
        String[] rows = row.split(",");
        String[] columns = head.split(",");

        MerchantParam merchantParam = RpcClientProxy.getMerchantParam(apiCdoe);
            int cusNum = findIndex(columns, "cus_num");
            if (cusNum == -1) {
                StringBuilder errorSb=new StringBuilder();
                errorSb.append(Constants.headMap.get("cus_num")).append("未填写,").append(row).append("\n");
                errorfw.append(errorSb);
                return false;
            }else {
                String row1="";
                try {
                    row1= rows[cusNum];
                }catch (ArrayIndexOutOfBoundsException e){
                    log.warn("ArrayIndexOutOfBoundsException {}",row);
                }
                if(StringUtils.isEmpty(row1)){
                    StringBuilder errorSb=new StringBuilder();
                    errorSb.append(Constants.headMap.get("cus_num")).append("未填写,").append(row).append("\n");
                    errorfw.append(errorSb);
                    return false;
                }
            }

        int id = findIndex(columns, "id");
        int name = findIndex(columns, "name");
        int cell = findIndex(columns, "cell");
        //若是不校验必填参数，则必须保证有客户编号和三要素中的一项
        if (merchantParam.getIsCheck() == 0||merchantParam.getIsCheck()==2||merchantParam.getIsCheck()==4) {
            if (!checkNotRequired(rows, id, name, cell)) {
                StringBuilder errorSb=new StringBuilder();
                errorSb.append("缺失必填参数,").append(row).append("\n");
                errorfw.append(errorSb);
                return false;
            }
        } else if (merchantParam.getIsCheck() == 1||merchantParam.getIsCheck()==3||merchantParam.getIsCheck()==5) {
            if (rows.length < 4) {
                StringBuilder errorSb=new StringBuilder();
                errorSb.append("缺失必填参数,").append(row).append("\n");
                errorfw.append(errorSb);
                return false;
            }

            if ((id == -1 || StringUtils.isEmpty(rows[id]))
                    || (name == -1 || StringUtils.isEmpty(rows[name]))
                    || (cell == -1 || StringUtils.isEmpty(rows[cell]))) {
                StringBuilder errorSb=new StringBuilder();
                errorSb.append("缺失必填参数,").append(row).append("\n");
                errorfw.append(errorSb);
                return false;
            }
        }
        String[] defaultCloumn=Constants.DEFAULT_CLOUMN;

        boolean pass=true;
        for (int i = 0; i < defaultCloumn.length; i++) {
            String columnName = defaultCloumn[i];
            int index = findIndex(columns, columnName);
            if (index == -1) {
                sb.append(",");
                continue;
            }

            String cloumnData = "";
            try {
                cloumnData = rows[index];
            } catch (ArrayIndexOutOfBoundsException e) {
                log.debug("columnName:{},head:{},row:{}",columnName,head,row);
                sb.append(",");
                continue;
            }
            Map<String, String> resultMap;
            if (!StringUtils.isEmpty(cloumnData)) {
                resultMap = checkColumn(cloumnData, columnName, merchantParam, decodeClient);
                String result = resultMap.get("result");
                String decode = resultMap.get("decode");
                if("decodeFail".equals(decode)){
                    pass=false;
                }
                if (StringUtils.isEmpty(result)) {
                    StringBuilder errorSb = new StringBuilder();
                    String s1 = Constants.headMap.get(columnName);
                    errorSb.append(s1 + "错误,").append(row + "\n");
                    errorfw.append(errorSb);
                    flag = false;
                    break;
                } else {
                    sb.append(result).append(",");
                }
            } else {
                sb.append(",");
            }
        }
        if (!pass) {
            String requestCode = merchantParam.getRequestCode();
            String decodeFailType = "";
            if ("1001".equals(requestCode)) {
                decodeFailType = "Md5";
            } else if ("1003".equals(requestCode)) {
                decodeFailType = "SM3";
            } else if ("1002".equals(requestCode)) {
                decodeFailType = "Sha256";
            }
            sb.append(decodeFailType).append(",");
        }else{
            sb.append(",");
        }
        return flag;
    }


    /**
     * 校验单个字段是否合法
     * @param data 字段值
     * @param column 字段
     * @param merchantParam 用户信息
     * @param decodeClient 解密客户端
     * @return 校验结果
     */
    public static Map<String, String> checkColumn(String data, String column, MerchantParam merchantParam, DecodeGrpcClient decodeClient) {
        //log.info("column:{},data:{}",column,data);
        Map<String, String> map = new HashMap<>();
        String result = "";
        String decode = "";
        UserValidator userValidator = new UserValidator(merchantParam.getIsCheck());
        BrCipherMaker instance = BrCipherMaker.getInstance();
        switch (column) {
            case "cus_num":
                if (StringUtils.isEmpty(data)) {
                    break;
                } else if (!userValidator.validateCusNum(data)) {
                    break;
                }
                result = data;
                break;
            case "name":
                if (StringUtils.isEmpty(data)) {
                    break;
                } else {
                    data = data.replace("•", "·");
                    String name = decodeClient.decode("name", data, merchantParam.getRequestCode(), merchantParam.getDecryptKey(), merchantParam.getIsCheck());
                    if (StringUtils.isEmpty(name)) {
                        if (data.length() > 128) {
                            break;
                        }
                        result = data;
                        decode = "decodeFail";
                        break;
                    } else {
                        if (!userValidator.validateName(name)) {
                            break;
                        }
                        result =instance.encode(name);
                        break;
                    }
                }
            case "id":
                if (StringUtils.isEmpty(data)) {
                    break;
                } else {
                    String id = decodeClient.decode("id", data, merchantParam.getRequestCode(), merchantParam.getDecryptKey(), merchantParam.getIsCheck());
                    int len = 0;
                    if ("1002".equals(merchantParam.getRequestCode()) || "1003".equals(merchantParam.getRequestCode())) {
                        len = 64;
                    } else {
                        len = 32;
                    }
                    if (StringUtils.isEmpty(id)) {
                        if (data.length() != len) {
                            break;
                        }
                        result = data;
                        decode = "decodeFail";
                        break;
                    }
                    if (!userValidator.validateId(id)) {
                        break;
                    }
                    result = instance.encode(id);
                    break;
                }
            case "cell":
                if (StringUtils.isEmpty(data)) {
                    break;
                } else {
                    String cell = decodeClient.decode("cell", data, merchantParam.getRequestCode(), merchantParam.getDecryptKey(), merchantParam.getIsCheck());
                    int len = 0;
                    if ("1002".equals(merchantParam.getRequestCode()) || "1003".equals(merchantParam.getRequestCode())) {
                        len = 64;
                    } else {
                        len = 32;
                    }
                    if (StringUtils.isEmpty(cell)) {
                        if (data.length() != len) {
                            break;
                        }
                        result = data;
                        decode = "decodeFail";
                        break;
                    }
                    if (!userValidator.validatePhone(cell)) {
                        break;
                    }
                    result = instance.encode(cell);
                    break;
                }
            default:
                break;
        }
        map.put("decode", decode);
        checkOptionalColumn(data,column,map,result,userValidator);
        return map;
    }

    /**
     * 校验选填项字段是否合法
     * @param data 字段值
     * @param column 字段
     * @param map 结果
     * @param result  结果
     * @param userValidator 校验类
     */
    private static void  checkOptionalColumn(String data, String column, Map<String, String> map,String result, UserValidator userValidator){
        Date now = new Date();
        switch (column) {
        case "pass_date":
            if (StringUtils.isEmpty(data)) {
                break;
            } else {
                Date passDate;
                try {
                    if(!userValidator.validateDate(data)){
                        break;
                    }
                    passDate= DateUtils.parseDateByString(data,"yyyy-MM-dd");
                    if (passDate.compareTo(now) > 0) {
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            result = data;
            break;
        case "user_date":
            if (StringUtils.isEmpty(data)) {
                break;
            } else {
                Date userDate;
                try {
                    if(!userValidator.validateDate(data)){
                        break;
                    }
                    userDate =DateUtils.parseDateByString(data,"yyyy-MM-dd");
                    if (userDate.compareTo(now) > 0) {
                        break;
                    }
                } catch (Exception e) {
                    break;
                }
            }
            result = data;
            break;
        case "loan_maturity_date":
            if (StringUtils.isEmpty(data)) {
                break;
            } else {
                if(!userValidator.validateDate(data)){
                    break;
                }
            }
            result = data;
            break;
        case "approve_result":
            if (StringUtils.isEmpty(data)) {
                break;
            }
            if (!"通过".equals(data) && !"拒绝".equals(data) && !"复议".equals(data) && !"无结果".equals(data) && !"无贷前审批".equals(data)) {
                break;
            }
            result = data;
            break;
        case "linkman_cell":
            if (StringUtils.isEmpty(data)) {
                break;
            } else if (!data.matches(Constants.CELL_REGEX)) {
                break;
            }
            result = data;
            break;
        case "time_range":
            if (StringUtils.isEmpty(data)) {
                break;
            } else if (!data.matches("^([0-5])$")) {
                break;
            }
            result = data;
            break;
        case "home_addr":
            if (StringUtils.isEmpty(data)) {
                break;
            } else if (data.length() > 60) {
                break;
            }
            result = data;
            break;
        case "tel_home":
            if (StringUtils.isEmpty(data)) {
                break;
            } else if (!data.matches(Constants.TEL_HOME_REGEX)) {
                break;
            }
            result = data;
            break;
        case "mail":
            if (StringUtils.isEmpty(data)) {
                break;
            } else if (!userValidator.validateEmail(data)) {
                break;
            }
            result = data;
            break;
        default:
            break;
       }
        map.put("result", result);
    }
    public static int findIndex(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(value)) {
                return i;
            }
        }
        //当if条件不成立时，默认返回一个负数值-1
        return -1;
    }
}