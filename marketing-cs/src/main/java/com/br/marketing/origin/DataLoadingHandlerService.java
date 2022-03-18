package com.br.marketing.origin;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 特殊数据加载处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/18 10:36
 */

@Service
@Slf4j
public class DataLoadingHandlerService {

    private static final String cidKey = "marketing:innerapi:transfer:cid:";

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    RedisChgService redisChgService;

    public String getTcIdFromRedis(String apiCode){
        // 1 获取分表后缀
        String key = cidKey.concat(apiCode);
        String tcId;
        try {
            tcId = redisChgService.get(key);
            if (StringUtils.isEmpty(tcId)) {
                tcId = tableCreateService.getTcId(apiCode);
                // 缓存一周
                redisChgService.setex(key, tcId, 7 * 24 * 3600);
            }
        } catch (Exception e) {
            tcId = tableCreateService.getTcId(apiCode);
            log.error("根据客户apiCode -- {} 查询tcId失败 --",apiCode, e);
        }
        return tcId;
    }
}
