package com.br.marketing.common.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Constants {
    public static final Pattern MYREGEX = Pattern.compile("\\.");
    public static final String ERRORFILE = "_FileVerification_error_";
    public static final String SFTP_IN_ERROR_PATH = "/UploadFiles/marketing/apiCode/error/";
    public static final String SFTP_IN_INPUT_PATH = "/UploadFiles/marketing/apiCode/input/";
    public static final Pattern FREQUENCY = Pattern.compile("^[0-5]{1}$");
    public static final String MYREGEX1 = "\\p{C}";

    public static Map<String, Integer> riskMap = new HashMap<>();
    public static final String[] DEFAULT_CLOUMN = {"cus_num", "name", "id", "cell", "pass_date", "user_date", "loan_maturity_date", "approve_result",
            "linkman_cell", "time_range", "home_addr", "tel_home", "mail"};
    public static Map<String, String> headMap = new HashMap<>();
    public static final String PUBLIC_APICODE = "4003434";

    public static final String TMP_FILE_PATH = "/opt/data/inloan/download/marketing/tmp/";


    public static final String DELETE_MONITOR_ERROR = "DELETE_MONITOR_ERROR";
    public static final String DELETE_MONITOR_SUCCESS = "DELETE_MONITOR_SUCCESS";

    public static final String HXRESULTERROR_RETRY_KEY = "HXRESULTERROR_RETRY_KEY";
    public static final String FTP_TO_SFTP_CHECK_TIME = "FTP_TO_SFTP_CHECK_TIME";

    public static final String UPLOAD_DATA_NUM = "UPLOAD_DATA_NUM_";
    public static final String UPLOAD_FAILDATA_NUM = "UPLOAD_FAILDATA_NUM_";


    public static final String INSERT_DB_NUMBER = "INSERT_DB_NUMBER_";
    public static final String HX_FLAG_98_NUM = "HX_FLAG_98_NUM_";

    public static final String SFTP_P_SECRET_KEY = "s%^*K%)l*R(a20201105";
    public static final String LOAN_WARNING_FTP = "ftp";
    public static final String LOAN_WARNING_SFTP = "sftp";
    public static final String LOAN_DISK = "localDisk";

    /**
     * 1.按逗号分隔
     * 2.按\001分隔
     */
    public static Map<Integer, String> sepMap = new HashMap<>();
    /**
     * 0：1天 、1：7天 、2：30天、3：15天、4：90天 99:1天
     */
    public static Map<String, Integer> frequencyMap = new HashMap<>();
    /**
     * 画像产品flag转换机制
     * http://c.100credit.cn/pages/viewpage.action?pageId=31365271
     */
    public static Map<String, String> flagMap = new HashMap<>();

    public static Map<String, String> requestCodeMap = new HashMap<>();

    public static Map<String, String> monitorTypeMap = new HashMap<>();
    static {
        monitorTypeMap.put("1","once");
        monitorTypeMap.put("2","once");
        monitorTypeMap.put("3","all");
        monitorTypeMap.put("4","all");
    }
    static {
        requestCodeMap.put("00", "00");
        requestCodeMap.put("1001", "Md5");
        requestCodeMap.put("1003", "SM3");
        requestCodeMap.put("1002", "1002");
        requestCodeMap.put("1006", "AES");
        requestCodeMap.put("1011", "3DES");
    }

    static {
        headMap.put("cus_num", "客户编号");
        headMap.put("name", "姓名");
        headMap.put("id", "身份证号");
        headMap.put("cell", "手机号");
        headMap.put("pass_date", "审批通过日");
        headMap.put("loan_maturity_date", "贷款到期日");
        headMap.put("approve_result", "贷前审批结果");
        headMap.put("linkman_cell", "联系人手机号");
        headMap.put("time_range", "时间范围");
        headMap.put("home_addr", "家庭地址");
        headMap.put("tel_home", "家庭座机号");
        headMap.put("mail", "邮箱");
        headMap.put("user_date", "观察日期");
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
        frequencyMap.put("99", 1);
    }

    static {
        riskMap.put("A", 2);
        riskMap.put("B", 3);
        riskMap.put("C", 4);
        riskMap.put("D", 5);
        riskMap.put("无结果", 1);
        riskMap.put("Exception", 0);
    }

    public static final String REDIS_STMT_RULE_PREFIX = "redisMonitor_";

    public static final String CELL_REGEX = "^1[2-9][0-9]\\d{8}$";
    public static final String TEL_HOME_REGEX = "^[0-9]{2,4}-[0-9]{7,8}$";
    public static final String CUS_NUM_REGEX = "^([a-zA-Z0-9]{1,64})$";

    /**
     * 正则 HH:mm
     */
    public static final String TIME_MINUTE_REGEX = "^([0-1]?[0-9]|2[0-3]):([0-5][0-9])$";

    /**
     * 正则 HH:mm:ss
     */
    public static final String TIME_SECOND_REGEX = "^([0-1]?[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";

    public static final String DELETE_MONIZTOR_REMARK = "^[0-9a-zA-Z_.]{1,100}$";
    public static final String REDIS_RADAR_PREFIX = "cnt_loan";
    public static final String REDIS_RADAR_TEST_PREFIX = "cnt_loan_test";
    public static final String REDIS_RADAR_TOTALCOUNT = "totalCount";


    public static final String LOAN_BUSINESSTYPECODE = "A202";

    public static final Integer DATA_ISDELETE_NO = 0;

    public static final Integer DATA_ISDELETE_YES = 1;

    public static final Integer DATA_VALID = 1;

    public static final Integer DATA_DELING = 2;

    public static final Integer DATA_DEL = 9;
    public static final Integer STATUS_VOID = 0;
    public static final Integer STATUS_START = 1;
    public static final Integer STATUS_DELETE = 2;

    //禁用
    public static final Integer ENABLED_FORB = 0;

    //启用
    public static final Integer ENABLED_ACT = 1;
    /**
     * 加解密key
     */
    public static final String TAG_KEY = "id,name,cell";
    public static final String JSON_DATA_KEYARR = "dataItems";

    //否
    public static final Integer NO = 0;

    //是
    public static final Integer YES = 1;


}
