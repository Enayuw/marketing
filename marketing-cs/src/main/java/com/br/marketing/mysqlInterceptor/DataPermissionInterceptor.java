package com.br.marketing.mysqlInterceptor;

import com.br.common.util.StringUtils;
import com.br.marketing.context.ThreadApicodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

/**
 * 数据权限过滤拦截器
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.mysqlInterceptor
 * @Description: 数据权限过滤拦截器
 * @CreateTime: 2022-07-08 14 :39
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 * <p>
 * Executor：拦截执行器的方法。
 * ParameterHandler：拦截参数的处理。
 * ResultHandler：拦截结果集的处理。
 * StatementHandler：拦截Sql语法构建的处理。
 */


/**
 * Executor：拦截执行器的方法。
 * ParameterHandler：拦截参数的处理。
 * ResultHandler：拦截结果集的处理。
 * StatementHandler：拦截Sql语法构建的处理。
 */

/**
 * mybatis 自定义拦截器
 * 三步骤：
 *  1 实现 {@link Interceptor} 接口
 *  2 添加拦截注解 {@link Intercepts}
 *  3 配置文件中添加拦截器
 *
 * 1 实现 {@link Interceptor} 接口
 *      具体作用可以看下面代码每个方法的注释
 * 2 添加拦截注解 {@link Intercepts}
 *      mybatis 拦截器默认可拦截的类型只有四种，即四种接口类型 Executor、StatementHandler、ParameterHandler 和 ResultSetHandler
 *      对于我们的自定义拦截器必须使用 mybatis 提供的注解来指明我们要拦截的是四类中的哪一个类接口
 *      具体规则如下：
 *          a：Intercepts 标识我的类是一个拦截器
 *          b：Signature 则是指明我们的拦截器需要拦截哪一个接口的哪一个方法
 *              type    对应四类接口中的某一个，比如是 Executor
 *              method  对应接口中的哪类方法，比如 Executor 的 update 方法
 *              args    对应接口中的哪一个方法，比如 Executor 中 query 因为重载原因，方法有多个，args 就是指明参数类型，从而确定是哪一个方法
 * 3 配置文件中添加拦截器
 *      拦截器其实就是一个 plugin，在 mybatis 核心配置文件中我们需要配置我们的 plugin ：
 *          <plugin interceptor="liu.york.mybatis.study.plugin.MyInterceptor">
 *              <property name="username" value="LiuYork"/>
 *              <property name="password" value="123456"/>
 *          </plugin>
 *
 * 拦截器顺序
 * 1 不同拦截器顺序：
 *      Executor -> ParameterHandler -> StatementHandler -> ResultSetHandler
 *
 * 2 对于同一个类型的拦截器的不同对象拦截顺序：
 *      在 mybatis 核心配置文件根据配置的位置，拦截顺序是 从上往下
 */

@Intercepts({@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
), @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
)})

@Slf4j
public class DataPermissionInterceptor implements Interceptor {
    private static final Integer MAPPED_STATEMENT_INDEX = 0;
    private static final Integer PARAM_OBJ_INDEX = 1;
    private static final Integer ROW_BOUNDS_INDEX = 2;
    private static final Integer RESULT_HANDLER_INDEX = 3;
    private static final Integer CACHE_KEY_INDEX = 4;
    private static final Integer BOUND_SQL_INDEX = 5;

    private static final String COUNT_PRE = "_COUNT";


    /**
     * 这个方法很好理解
     * 作用只有一个：我们不是拦截方法吗，拦截之后我们要做什么事情呢？
     *      这个方法里面就是我们要做的事情
     *
     * 解释这个方法前，我们一定要理解方法参数 {@link Invocation} 是个什么鬼？
     * 1 我们知道，mybatis拦截器默认只能拦截四种类型 Executor、StatementHandler、ParameterHandler 和 ResultSetHandler
     * 2 不管是哪种代理，代理的目标对象就是我们要拦截对象，举例说明：
     *      比如我们要拦截 {@link Executor#update(MappedStatement ms, Object parameter)} 方法，
     *      那么 Invocation 就是这个对象，Invocation 里面有三个参数 target method args
     *          target 就是 Executor
     *          method 就是 update
     *          args   就是 MappedStatement ms, Object parameter
     *
     *   如果还是不能理解，我再举一个需求案例：看下面方法代码里面的需求
     *
     *  该方法在运行时调用
     */

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        /*
         * 需求：我们需要对当前登录的用户匹配的apiCode作为过滤条件生成新的sql语句
         * 那我就可以让我们的自定义拦截器 DataPermissionInterceptor 拦截 Executor 的 query 方法，
         * 比如我们拦截点是 Executor 的 query 方法 ：  int update(MappedStatement ms, Object parameter)
         *
         * 我们是不是还需要调用这个query方法呢，如何如调用呢？
         * 所以就出现了 Invocation 对象，它这个时候其实就是一个 Executor，而且 method 对应的就是 query 方法，我们
         * 想要调用这个方法，只需要执行 invocation.proceed()
         */

        /* 因为我拦截的就是Executor，所以我可以强转为 Executor，默认情况下，这个Executor 是个 SimpleExecutor */


        /*
         * Executor 的 query 方法里面有一个参数 MappedStatement，它是包含了 sql 语句的，所以我获取这个对象
         * 以下是伪代码，思路：
         * 1 通过反射从 Executor 对象中获取 MappedStatement 对象
         * 2 从 MappedStatement 对象中获取 SqlSource 对象
         * 3 然后从 SqlSource 对象中获取获取 BoundSql 对象
         * 4 最后通过 BoundSql#getSql 方法获取 sql
         */

        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = (MappedStatement) args[MAPPED_STATEMENT_INDEX];
        Object parameter = args[PARAM_OBJ_INDEX];
        RowBounds rowBounds = (RowBounds) args[ROW_BOUNDS_INDEX];
        ResultHandler resultHandler = (ResultHandler) args[RESULT_HANDLER_INDEX];
        Executor executor = (Executor) invocation.getTarget();
        CacheKey cacheKey;
        BoundSql boundSql;

        //由于逻辑关系，只会进入一次
        if (args.length == 4) {
            //4 个参数时
            boundSql = mappedStatement.getBoundSql(parameter);
            cacheKey = executor.createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
        } else {
            //6 个参数时
            cacheKey = (CacheKey) args[CACHE_KEY_INDEX];
            boundSql = (BoundSql) args[BOUND_SQL_INDEX];
        }
        // 获取SQL
        String sql = boundSql.getSql();
        Configuration configuration = mappedStatement.getConfiguration();
        AddDataAuth permissionByDelegate = getPermissionByDelegate(mappedStatement);
        String apicode = ThreadApicodeInfo.getData();
        String finalSql = "";
        //符合过滤条件：接口和Mapper双注解
        if (permissionByDelegate != null && StringUtils.isNotBlank(apicode)) {
            finalSql = "select * from (" + sql + ")  as u where u.api_code in(" + apicode + ")";

            Field field = boundSql.getClass().getDeclaredField("sql");
            field.setAccessible(true);
            field.set(boundSql, finalSql);
            //执行修改后的sql语句
            return executor.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
        } else {
            return invocation.proceed();
        }
    }

    /**
     * 这个方法也很好理解
     * 作用就只有一个：那就是Mybatis在创建拦截器代理时候会判断一次，当前这个类 MyInterceptor 到底需不需要生成一个代理进行拦截，
     * 如果需要拦截，就生成一个代理对象，这个代理就是一个 {@link Plugin}，它实现了jdk的动态代理接口 {@link }，
     * 如果不需要代理，则直接返回目标对象本身
     *
     * Mybatis为什么会判断一次是否需要代理呢？
     * 默认情况下，Mybatis只能拦截四种类型的接口：Executor、StatementHandler、ParameterHandler 和 ResultSetHandler
     * 通过 {@link Intercepts} 和 {@link Signature} 两个注解共同完成
     * 试想一下，如果我们开发人员在自定义拦截器上没有指明类型，或者随便写一个拦截点，比如Object，那Mybatis疯了，难道所有对象都去拦截
     * 所以Mybatis会做一次判断，拦截点看看是不是这四个接口里面的方法，不是则不拦截，直接返回目标对象，如果是则需要生成一个代理
     *
     *  该方法在 mybatis 加载核心配置文件时被调用
     */

    @Override
    public Object plugin(Object target) {
        /*
         * 看了这个方法注释，就应该理解，这里的逻辑只有一个，就是让mybatis判断，要不要进行拦截，然后做出决定是否生成一个代理
         *
         * 下面代码什么鬼，就这一句就搞定了？
         * Mybatis判断依据是利用反射，获取这个拦截器 MyInterceptor 的注解 Intercepts和Signature，然后解析里面的值，
         * 1 先是判断要拦截的对象是四个类型中 Executor、StatementHandler、ParameterHandler、 ResultSetHandler 的哪一个
         * 2 然后根据方法名称和参数(因为有重载)判断对哪一个方法进行拦截  Note：mybatis可以拦截这四个接口里面的任一一个方法
         * 3 做出决定，是返回一个对象呢还是返回目标对象本身(目标对象本身就是四个接口的实现类，我们拦截的就是这四个类型)
         *
         * 好了，理解逻辑我们写代码吧~~~  What !!! 要使用反射，然后解析注解，然后根据参数类型，最后还要生成一个代理对象
         * 我一个小白我怎么会这么高大上的代码嘛，怎么办？
         *
         * 那就是使用下面这句代码吧  哈哈
         * mybatis 早就考虑了这里的复杂度，所以提供这个静态方法来实现上面的逻辑
         */
        return Plugin.wrap(target, this);
    }

    /**
     * 这个方法最好理解，如果我们拦截器需要用到一些变量参数，而且这个参数是支持可配置的，
     *  类似Spring中的@Value("${}")从application.properties文件获取
     * 这个时候我们就可以使用这个方法
     *
     * 如何使用？
     * 只需要在 mybatis 配置文件中加入类似如下配置，然后 {@link Interceptor#setProperties(Properties)} 就可以获取参数
     *      <plugin interceptor="liu.york.mybatis.study.plugin.MyInterceptor">
     *           <property name="username" value="LiuYork"/>
     *           <property name="password" value="123456"/>
     *      </plugin>
     *      方法中获取参数：properties.getProperty("username");
     *
     * 问题：为什么要存在这个方法呢，比如直接使用 @Value("${}") 获取不就得了？
     * 原因是 mybatis 框架本身就是一个可以独立使用的框架，没有像 Spring 这种做了很多依赖注入的功能
     *
     *  该方法在 mybatis 加载核心配置文件时被调用
     */
    @Override
    public void setProperties(Properties properties) {
        System.out.println(333);
    }

    /**
     * 获取数据权限注解信息
     *
     * @param mappedStatement
     * @return
     */
    private AddDataAuth getPermissionByDelegate(MappedStatement mappedStatement) {
        AddDataAuth dataAuth = null;
        try {
            String id = mappedStatement.getId();
            //统计SQL取得注解也是实际查询id上得注解，所以需要去掉_COUNT
            if (id.contains(COUNT_PRE)) {
                id = id.replace(COUNT_PRE, "");
            }
            String className = id.substring(0, id.lastIndexOf("."));
            String methodName = id.substring(id.lastIndexOf(".") + 1, id.length());
            final Class<?> cls = Class.forName(className);
            final Method[] method = cls.getMethods();
            for (Method me : method) {
                if (me.getName().equals(methodName) && me.isAnnotationPresent(AddDataAuth.class)) {
                    dataAuth = me.getAnnotation(AddDataAuth.class);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataAuth;
    }

}
