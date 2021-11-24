package com.br.marketing.entity;

import lombok.Data;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2021/5/6 15:45
 * @Description:
 **/
@Data
public class Customer {
    private Long id;
    private String cid;
    private String apiCode;
    private String message;
    private String type;
    private Integer threadNum;
    private Integer taskTime;
    private Integer finishDate;
    private Integer pushCustomer;
    private Integer sort;
    private Integer status;
    private Integer checkBlackList;
    private Integer checkRedisNumber;
    private Integer saveLog;
    private String extendConfigInfo;
    private Integer pushThreadNum;
    private Integer pushType;
    private String pushUrl;
    private String shortName;
}
