package com.br.marketing.api.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entity.ProInSys;
import com.br.marketing.api.entity.ProInfo;
import com.br.marketing.common.constants.strategy.ProType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;

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
 *
 * @Description : 商户/产品配置信息 数据库操作类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/5/4 14:21
 */

@Repository
@Slf4j
public class MerchantRepository {
    @Resource
    private RedisService redisService;
    @Resource
    private IceClient iceClient;

    /**
     * Gets all product code.
     *
     * @param apiCode the api code
     * @param proType the pro type
     * @return the all product code
     */
    public String getAllProductCode(String apiCode, String proType) {
        //从数据库中查询未删除的同一类别的所有商品编号

        //从redis中拿到该类别的所有产品code
        List<ProInfo> list = allProduct(apiCode, proType);
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(list)) {
            for (ProInfo proInfo : list) {
                sb.append(proInfo.getProCode()).append(",");
            }
        }
        return StringUtils.hasText(sb)?sb.substring(0,sb.length()-1):null;
    }


    /**
     * All product list.
     *
     * @param apiCode  the api code
     * @param proClass the pro class
     * @return the list
     */
    public List<ProInfo> allProduct(String apiCode, String proClass){
        Map<String, String> proMap = iceClient.getProInfo(apiCode,proClass);
        if (StringUtils.isEmpty(proMap)){
            return new ArrayList<>();
        }
        List<ProInfo> list = change2ProInfo(JSONArray.parseArray(redisService.get("Loan_ProInfo"),ProInSys.class));
        List<ProInfo> products = new ArrayList<>();
        Iterator<Map.Entry<String,String>> entryIterator=proMap.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,String> entry=entryIterator.next();
            String proCode=entry.getKey();
            String version=entry.getValue();
            for (ProInfo proInfo:list) {
                if (StringUtils.isEmpty(proClass)){
                    if (proCode.equals(proInfo.getProCode().trim()) && version.toLowerCase().equals(proInfo.getVersion().trim().toLowerCase())){
                        products.add(proInfo);
                    }
                }else if (proCode.equals(proInfo.getProCode().trim()) && version.toLowerCase().equals(proInfo.getVersion().trim().toLowerCase())
                        && proClass.equals(proInfo.getProClass())){
                    products.add(proInfo);
                }
            }
        }

        return products;
    }

    /**
     * Change 2 pro info list.
     *
     * @param list the list
     * @return the list
     */
    public List<ProInfo> change2ProInfo(List<ProInSys> list){
        List<ProInfo> proInfos = new ArrayList<>();
        for (ProInSys pro:list) {
            proInfos.add(getProInfo(pro));
        }
        return proInfos;
    }
    private ProInfo getProInfo(ProInSys pro) {
        ProInfo proInfo = new ProInfo();
        /*
        "B2"开头的 ---规则产品
        "B101"     ---基础数据产品
        "B303"     ---行为模型产品
        "reviewStrA"---贷前重审产品
        "phone_relation","phoneStatusCheck" ---号码状态核查产品
        */
        String typeCode = pro.getProductionTypeCode();
        String proCode = pro.getProductionName();
        if (typeCode.startsWith("B2")){
            proInfo.setProClass(ProType.RULE_TYPE.getCode());
        }else if ("B101".equals(typeCode)){
            proInfo.setProClass(ProType.BASE_DATA.getCode());
        }else if ("B303".equals(typeCode)){
            proInfo.setProClass(ProType.BEHAVIOR_SCORE.getCode());
        }else if ("phone_relation".equals(proCode)||"phoneStatusCheck".equals(proCode)){
            proInfo.setProClass(ProType.PHONE_CHECK.getCode());
        }else if ("reviewStrA".equals(proCode)){
            proInfo.setProClass(ProType.STRATEGY_RETRY.getCode());
        }else{
            log.info("the product can not find their categories , the product {}"
                    , JSONObject.toJSONString(pro));
        }
        proInfo.setProCode(pro.getProductionName());
        proInfo.setProName(pro.getProductionChineseName());
        proInfo.setVersion(pro.getVersion());
        proInfo.setRemark(pro.getDescription());
        return proInfo;
    }
}




