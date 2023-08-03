package com.br.marketing.client.zhongyou;

import org.apache.http.HttpEntity;

import java.io.InputStream;
import java.util.Map;

/**
 * 描述：： 中邮结果处理回调函数类
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouResultInterface
 * @author: it-yml
 * @create: 2023-08-02 14:45
 * @Version 1.0
 * --------------------------------------
 **/
public interface ZhongYouResultInterface {
    //
    Map<String,String> applyStream(InputStream inputStream) ;
    Map<String,String> applyEntity(HttpEntity httpEntity) ;
}
