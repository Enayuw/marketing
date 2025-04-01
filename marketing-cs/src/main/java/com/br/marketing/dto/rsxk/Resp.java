package com.br.marketing.dto.rsxk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * <p> </p>
 *
 * @author handong@brgroup.com
 * @date 2021/3/10
 */
@Builder
@AllArgsConstructor
@Data
public class Resp<T>{

    private int code;
    private String msg;
    private T data;

    public static <T> Resp create(int code,String msg,T data)  {
        return new Resp(code,msg,data);
    }

    public static <T>Resp success()  {
        return new Resp(Code.SUCCESS.getCode(), Code.SUCCESS.getMsg(),null);
    }

    public static <T>Resp success(T data)  {
        return new Resp(Code.SUCCESS.getCode(), Code.SUCCESS.getMsg(),data);
    }

    public static <T>Resp fail(T data)  {
        return new Resp(Code.FAIL.getCode(), Code.FAIL.getMsg(),data);
    }

    public static <T>Resp fail(String msg,T data)  {
        return new Resp(Code.FAIL.getCode(),msg,data);
    }

    public static <T>Resp createByCode(Code code,T data)  {
        return new Resp(code.getCode(),code.getMsg(),data);
    }

    public static Resp empty()  {
        return new Resp(Code.SUCCESS.getCode(), Code.SUCCESS.getMsg(),null);
    }


    public static <T>Resp failByMsg(String msg)  {
        return new Resp(Code.FAIL.getCode(),msg ,null);
    }
    public static <T>Resp fail()  {
        return new Resp(Code.FAIL.getCode(), Code.FAIL.getMsg(),null);
    }

    @AllArgsConstructor
    @Getter
    public enum Code {
        SUCCESS(0,"操作成功"),
        FAIL(-1,"操作失败"),

        INVALID_PARAMS(10001,"操作失败，无效参数"),
        ASSOCIATED_DATA(10002,"操作失败，存在使用中的关联数据"),
        INVALID_PLAN(10003,"操作失败，无效计划"),


        INVALID_DELAYTIME(21000,"操作失败，目标条件延迟时间应小于触达配置延迟时间"),
        DUPLICATE_DATA(21002,"操作失败，数据已存在"),

        ILLEGAL_ARG(1001,"无效参数");

        private int code;
        private String msg;
    }

}
