package com.br.marketing.monkeydata.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.monkeydata.service.Impl.ZhongAnHandleImpl;

public class TestMonkey {
    public static void main(String[] args) {
        IMonkeyDataHandle handle = new ZhongAnHandleImpl();
//        Result action = handle.action();
        System.out.println("123");
    }
}
