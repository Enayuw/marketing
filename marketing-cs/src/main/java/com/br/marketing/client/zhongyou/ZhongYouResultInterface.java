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
    /**
     * 中邮数据流结果处理
     * @param inputStream 数据流
     * @param fileId 文件id
     */
    Map<String,String> applyStream(InputStream inputStream,Long fileId) ;

    /**
     * 中邮entity 结果处理
     * @param httpEntity 返回实体
     */
    Map<String,String> applyEntity(HttpEntity httpEntity) ;
}
