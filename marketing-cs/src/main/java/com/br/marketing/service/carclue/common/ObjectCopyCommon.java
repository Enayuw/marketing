package com.br.marketing.service.carclue.common;

import com.br.marketing.entity.CarClueInfo;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;

public class ObjectCopyCommon {

    /**
     * 对象实现深拷贝
     *
     * @param from   复制对象
     * @param target 目标对象
     * @return T
     */
    public static <T> T deepCopyBean(T from, Class<T> target) throws Exception {
        T result = target.getDeclaredConstructor().newInstance();
        BeanUtils.copyProperties(from, result);
        return result;
    }


/*    public static void main(String args[]) throws Exception {


        CarClueInfo carClueInfo = new CarClueInfo();
        carClueInfo.setClueDataStatus(0);
        ObjectCopyCommon.deepCopyBean(carClueInfo, CarClueInfo.class);

    }*/
}




