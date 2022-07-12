package com.br.marketing.mysqlInterceptor;

import com.github.pagehelper.PageInterceptor;
import com.github.pagehelper.autoconfigure.PageHelperAutoConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 自定义拦截器注册
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.mysqlInterceptor
 * @Description: 自定义拦截器注册
 * @CreateTime: 2022-07-08 17 :52
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */

@Configuration
@AutoConfigureAfter(PageHelperAutoConfiguration.class)
public class MyDataPermissionAutoConfiguration {
    @Autowired
    private List<SqlSessionFactory> sqlSessionFactoryList;

    @PostConstruct
    public void addMyInterceptor() {
        DataPermissionInterceptor e = new DataPermissionInterceptor();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            sqlSessionFactory.getConfiguration().addInterceptor(e);
        }
    }

}
