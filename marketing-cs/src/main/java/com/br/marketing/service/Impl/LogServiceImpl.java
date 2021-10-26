package com.br.marketing.service.Impl;

import com.sun.javadoc.Doclet;
import com.sun.javadoc.RootDoc;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;

@Service
public class LogServiceImpl {
    public void writeLog(){
        Field[] fields = Object.class.getClass().getFields();
        for (Field field : fields) {
            new RootDoc().
        }
    }
}
