package com.br.marketing.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Constants {
    public static final Pattern MYREGEX = Pattern.compile("\\.");
    public static final String ERRORFILE="_FileVerification_error_";
    public static final String SFTP_IN_ERROR_PATH="/UploadFiles/marketing/apiCode/error/";
    public static final String SFTP_IN_INPUT_PATH="/UploadFiles/marketing/apiCode/input/";
    public static final  Pattern FREQUENCY= Pattern.compile("^[0-5]{1}$");

    public static Map<String,Integer> riskMap = new HashMap<>();
                                                 //cus_num,id,cell,name,loan_maturity_date,approve_result,linkman_cell,time_range,home_addr,tel_home,mail
    public static final String[] DEFAULT_CLOUMN={"cus_num","name","id","cell","pass_date","user_date","loan_maturity_date","approve_result","linkman_cell","time_range","home_addr","tel_home","mail"};
    public static final String[] DEFAULT_CLOUMN_DEL={"name","id","cell"};
    public static final Integer PPDFREQUENCY=5;
    public static final Integer FREQUENCY360=7;
    public static final Integer FREQUENCYHNNX=7;
    public static final String LOAN_WARNING_CHF_KEY="POINT_LOAN_WARNING_CHG_";
    public static final String LOAN_WARNING_CHF_CNT_KEY="LOAN_WARNING_CHG_CNT_";
    public static Map<String,String> headMap = new HashMap<>();
    public static final String PUBLIC_APICODE="4002511";

    public static final String  APICODE_SN_OPERATION_DEPARTMENT = "3005913";
    public static final String  APICODE_SN_OPERATION_DEPARTMENT_QA = "7410104";
    public static final String  APICODE_PPD = "3004761";
    public static final String  APICODE_PPD_QA = "1";
    public static final String  APICODE_360= "3005390";
    public static final String  APICODE_360_QA= "7410101";
    public static final String  APICODE_360_MARKET= "3007130";
    public static final String  APICODE_360_MARKET_QA= "7410106,7410998";
    public static final String  APICODE_SN_RISK_DEPARTMENT = "3005538";
    public static final String  APICODE_SN_RISK_DEPARTMENT_QA = "7410103";
    public static final String  APICODE_HNNX = "3006722";
    public static final String  APICODE_APICODE_HNNX_QA = "7410105";
    public static final String  APICODE_DAAS= "4002758,4002759";
    public static final String  APICODE_DAAS_QA= "7410102,7410356";
    public static final String  APICODE_SHAZI="7410480,7410481";
    public static final String  TMP_FILE_PATH="/opt/data/inloan/download/warning/tmp/";







    public static final String  DELETE_MONITOR_ERROR= "DELETE_MONITOR_ERROR";
    public static final String  DELETE_MONITOR_SUCCESS= "DELETE_MONITOR_SUCCESS";

    public static final String  HXRESULTERROR_RETRY_KEY= "HXRESULTERROR_RETRY_KEY";
    public static final String  FTP_TO_SFTP_CHECK_TIME= "FTP_TO_SFTP_CHECK_TIME";

    public static final String UPLOAD_DATA_NUM="UPLOAD_DATA_NUM_";
    public static final String UPLOAD_FAILDATA_NUM="UPLOAD_FAILDATA_NUM_";

    public static final String FILE_DATA_RESULT="result.txt";

    public static final String STRATEGY_ID_360="DTB0000001";
    public static final String CLOSE_DATE_360="2021-06-15";

    public static final String STRATEGY_ID_PPD="DTB0000001";
    public static final String STRATEGY_ID_PPD_SEC="DTB0000003:002";
    public static final String CLOSE_DATE_PPD="2021-10-31";

    public static final String INSERT_DB_NUMBER="INSERT_DB_NUMBER_";
    public static final String HX_FLAG_98_NUM="HX_FLAG_98_NUM_";

    public static final String SFTP_P_SECRET_KEY ="s%^*K%)l*R(a20201105";
    public static final String SYNC_FILENUM="SYNC_FILENUM_";
    public static final String LOAN_WARNING_FTP="ftp";
    public static final String LOAN_WARNING_SFTP="sftp";
    public static final String MQ_EXCHANGE="loan.warning.exchange";
    public static final String MQ_QUEUE="loan.warning.queue";
    public static final String MQ_ROUTINGKEY="loan.warning.routingkey";
    /**
     * 报警发送码
     */
    public static Map<String,String>  sendCodeMap = new HashMap<>();
    /**
     * 1.按逗号分隔
     * 2.按\001分隔
     */
    public static Map<Integer,String> sepMap = new HashMap<>();
    /**
     * 0：1天 、1：7天 、2：30天、3：15天、4：90天 99:1天
     */
    public static Map<String,Integer> frequencyMap = new HashMap<>();
    /**
     * 画像产品flag转换机制
     * http://c.100credit.cn/pages/viewpage.action?pageId=31365271
     */
    public static Map<String,String> flagMap = new HashMap<>();

    public static Map<String,String> requestCodeMap = new HashMap<>();
    static {
        requestCodeMap.put("00","00");
        requestCodeMap.put("1001","Md5");
        requestCodeMap.put("1003","SM3");
        requestCodeMap.put("1002","1002");
        requestCodeMap.put("1006","AES");
        requestCodeMap.put("1011","3DES");
    }
    static {
        headMap.put("cus_num","客户编号");
        headMap.put("name","姓名");
        headMap.put("id","身份证号");
        headMap.put("cell","手机号");
        headMap.put("pass_date","审批通过日");
        headMap.put("loan_maturity_date","贷款到期日");
        headMap.put("approve_result","贷前审批结果");
        headMap.put("linkman_cell","联系人手机号");
        headMap.put("time_range","时间范围");
        headMap.put("home_addr","家庭地址");
        headMap.put("tel_home","家庭座机号");
        headMap.put("mail","邮箱");
        headMap.put("user_date","观察日期");
    }
    static {
        sepMap.put(1, ",");
        sepMap.put(2, "\001");
    }
    static {
        flagMap.put("speciallist_c", "specialList_c");
        flagMap.put("speciallist", "specialList");
        flagMap.put("payconsumption", "payConsumption");
        flagMap.put("accountchangemonth", "accountChangeMonth");
        flagMap.put("accountchange", "accountChange");
        flagMap.put("telecomcheck", "telecomCheck");
        flagMap.put("applyloan", "applyLoan");
        flagMap.put("airtravel", "airTravel");
        flagMap.put("basicinformation", "basicInformation");
        flagMap.put("eccatethree", "ecCateThree");
        flagMap.put("applyfeature", "ApplyFeature");
        flagMap.put("consumptionfeature", "ConsumptionFeature");
    }
    static {
        frequencyMap.put("0", 1);
        frequencyMap.put("1", 7);
        frequencyMap.put("2", 30);
        frequencyMap.put("3", 15);
        frequencyMap.put("4", 90);
        frequencyMap.put("5", 3);
        frequencyMap.put("99",1);
    }
    static {
        riskMap.put("A", 2);
        riskMap.put("B", 3);
        riskMap.put("C", 4);
        riskMap.put("D", 5);
        riskMap.put("无结果", 1);
        riskMap.put("Exception", 0);
    }
    static {
        sendCodeMap.put("sysError","60000");
        sendCodeMap.put("resultVolume01","50005");
        sendCodeMap.put("ftpToSftp","50004");
        sendCodeMap.put("dataFileUploadFail","50001");
        sendCodeMap.put("dataFileVolumn","50002");
        sendCodeMap.put("uploadSuccess","50000");
        sendCodeMap.put("fileUploadFtp","50003");
    }
    public static final String REDIS_STMT_PREFIX="redisProduct_loan_";
    public static final String REDIS_STMT_RULE_PREFIX="redisMonitor_";

    public static final String ID_CARD_REGEX = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$|^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X)$";
    public static final String CELL_REGEX = "^1[2-9][0-9]\\d{8}$";
    public static final String NAME_REGEX = "^[\\u4E00-\\u9FA5]{2,10}(?:·[\\u4E00-\\u9FA5]{2,10})*$";
    public static final String BANK_ID_REGEX = "\\d{16,21}";
    public static final String MD5_REGEX = "^([a-fA-F0-9]{32})$";
    public static final String TEL_HOME_REGEX = "^[0-9]{2,4}-[0-9]{7,8}$";
    public static final String MAIL_REGEX ="^\\w+@[a-z0-9]+\\.[a-z]{2,4}$";
    public static final String CUS_NUM_REGEX = "^([a-zA-Z0-9]{1,64})$";

    public static final String DELETE_MONIZTOR_REMARK="^[0-9a-zA-Z_.]{1,100}$";
    public static final String DELETE_MONIZTOR_SERIALNUMBER="^[0-9]{1,10}$";
    public static final String REDIS_RADAR_PREFIX = "cnt_loan";
    public static final String REDIS_RADAR_TEST_PREFIX = "cnt_loan_test";
    public static final String REDIS_RADAR_TOTALCOUNT = "totalCount";




    public static final String LOAN_BUSINESSTYPECODE="A202";
    public static final Integer DATA_VALID = 1;
}
