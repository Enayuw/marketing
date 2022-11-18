package com.br.marketing.api.controller;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import org.apache.ibatis.ognl.OgnlException;
import org.apache.ibatis.ognl.OgnlRuntime;
/**
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @BelongsPackage: com.br.marketing.api.controller
 * @Description:
 * @CreateTime: 2022-07-28 18 :34
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
public class TestMybatis {
}
class Caller extends Thread {

    @Override
    public void run() {

        try {
            Method m = OgnlRuntime.getGetMethod(null, People.class, "name");
            if (m != null) {
                System.out.println(m.getName() + " had got!!");
            } else {
                System.out.println("null had got!!");
            }
        } catch (IntrospectionException e) {
            System.out.println("IntrospectionException occurs:" + e.getMessage());
            e.printStackTrace();
        } catch (OgnlException e) {
            System.out.println("OgnlException occurs:" + e.getMessage());
            e.printStackTrace();
        }
    }
}

class People {

    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}