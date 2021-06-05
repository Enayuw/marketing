package com.br.marketing.task.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.PropertiesUtil;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

/**
 * Created by Bairong on 2020/5/8.
 */
@Slf4j
public class ProductResultUtil {

    public static void dealProResult(JSONObject hxJson, Set<String> products,StringBuilder sb,
                                     Map<String, String> proFieldMap,String sep,String apiCode){
        if(!hxJson.isEmpty()){
            if(Constants.APICODE_DAAS.contains(apiCode)||Constants.APICODE_DAAS_QA.contains(apiCode)){
                if(products.contains("applyloanstr")){
                    String fields = PropertiesUtil.getProperty("ApplyLoanStr_DAAS");
                    String[] split = fields.split(",");
                    for (int i=0;i<split.length;i++){
                        sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                        sb.append(sep);
                    }
                }
                if(products.contains("applyloan_d")){
                    String fields = PropertiesUtil.getProperty("ApplyLoan_d_DAAS");
                    String[] split = fields.split(",");
                    for (int i=0;i<split.length;i++){
                        sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                        sb.append(sep);
                    }
                }
                if(products.contains("keyattribution")){
                    String fields = PropertiesUtil.getProperty("KeyAttribution_DAAS");
                    String[] split = fields.split(",");
                    for (int i=0;i<split.length;i++){
                        sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                        sb.append(sep);
                    }
                }
                if(products.contains("scorecashon")){
                    String fields = PropertiesUtil.getProperty("scorecashon_DAAS");
                    String[] split = fields.split(",");
                    for (int i=0;i<split.length;i++){
                        sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                        sb.append(sep);
                    }
                }
                return;
            }
            if(Constants.APICODE_SHAZI.contains(apiCode)){
//                BrCipherMaker instance = BrCipherMaker.getInstance();
//                String cell=instance.decode(blu.getCell());
//                if(StringUtils.isNotEmpty(cell)) {
//                    MerchantParam merchantParam = IceClient.getMerchantParam(apiCode);
//                    UserValidator userValidator = new UserValidator(merchantParam.getIsCheck());
//                    if(userValidator.validatePhone(cell)){
//                        sb.append(encode(merchantParam.getRequestCode(),cell));
//                    }else {
//                        sb.append(cell);
//                    }
//                }
//                sb.append(sep);

                if(products.contains("scorencashonszyxxy")){
                    Integer age =hxJson.getInteger("sd_scorencashonszyxxy_pd_id_apply_age");
                    if(age ==null || age<20||age>=50){
                        return;
                    }
                    String[] deleteArea="新疆,西藏,广西,贵州".split(",");
                    String cellProvince=hxJson.getString("sd_scorencashonszyxxy_pd_cell_province");
                    String idWhere=hxJson.getString("sd_scorencashonszyxxy_pd_id_where");
                    if(StringUtils.isEmpty(cellProvince) ||StringUtils.isEmpty(idWhere)){
                        return;
                    }
                    for (String area : deleteArea) {
                        if(cellProvince.contains(area)||idWhere.contains(area)){
                            return;
                        }
                    }
                    if(StringUtils.isEmpty(hxJson.get("scorencashonszyxxy"))){
                        return;
                    }
                    String fields = PropertiesUtil.getProperty("scorencashonszyxxy");
                    String[] split = fields.split(",");
                    for (int i=0;i<split.length;i++){
                        sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                        sb.append(sep);
                    }
                }
                if(products.contains("scoremcashonxhqbdzcd")){
                    if(StringUtils.isEmpty(hxJson.get("scoremcashonxhqbdzcd"))){
                        return;
                    }
                    String fields = PropertiesUtil.getProperty("scoremcashonxhqbdzcd");
                    String[] split = fields.split(",");
                    for (int i=0;i<split.length;i++){
                        sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                        sb.append(sep);
                    }
                }
                return;
            }
            if(products.contains("speciallist_c")){
                String speciallistCFields = proFieldMap.get("SpecialList_c");
                String[] split = speciallistCFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            if(products.contains("inforelation")){
                String inforelationFields = proFieldMap.get("InfoRelation");
                String[] split = inforelationFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            if(products.contains("applyloanstr")){
                String applyloanstrFields = proFieldMap.get("ApplyLoanStr");
                String[] split = applyloanstrFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            if(products.contains("applyloanusury")){
                String applyloanusuryFields = proFieldMap.get("ApplyLoanUsury");
                String[] split = applyloanusuryFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            if(products.contains("executionlimited")){
                String executionlimitedFields = proFieldMap.get("ExecutionLimited");
                String[] split = executionlimitedFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            if(products.contains("consumptionfeature")){
                String consumptionfeatureFields = proFieldMap.get("ConsumptionFeature");
                String[] split = consumptionfeatureFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            if(products.contains("netshopping")){
                String netshoppingFields = proFieldMap.get("NetShopping");
                String[] split = netshoppingFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }
            if(products.contains("scorecust")){
                String scorecust = hxJson.getString("scorecust");
                if(StringUtils.isEmpty(scorecust)){
                    sb.append("0").append(sep).append(sep);
                }else{
                    sb.append("1").append(sep).append(scorecust).append(sep);
                }
            }
            if((Constants.APICODE_SN_OPERATION_DEPARTMENT.equals(apiCode)||Constants.APICODE_SN_OPERATION_DEPARTMENT_QA.equals(apiCode))
                &&products.contains("scoremconsonsncfclxmodel")){
                    String scorecust = hxJson.getString("sccm_score");
                    if(StringUtils.isEmpty(scorecust)){
                        sb.append("0").append(sep).append(sep);
                    }else{
                        sb.append("1").append(sep).append(scorecust).append(sep);
                    }
            }
            if(products.contains("scoredata")){
                String scoredataFields = proFieldMap.get("ScoreData");
                String[] split = scoredataFields.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
            }

            if((Constants.APICODE_SN_RISK_DEPARTMENT.equals(apiCode)||Constants.APICODE_SN_RISK_DEPARTMENT_QA.equals(apiCode))
                    &&products.contains("scoremixuals")&&products.contains("scorebcashonsndzysbl")){
                String flagScorebcashonsndzysbl = hxJson.getString("flag_scorebcashonsndzysbl");
                String flagScoremixuals = hxJson.getString("flag_scoremixuals");
                if("1".equals(flagScoremixuals)||"1".equals(flagScorebcashonsndzysbl)){
                    sb.append("1").append(sep);
                }else{
                    sb.append("0").append(sep);
                }
                String scoremixuals = proFieldMap.get("scoremixuals");
                String[] split = scoremixuals.split(",");
                for (int i=0;i<split.length;i++){
                    sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                    sb.append(sep);
                }
                String scorebcashonsndzysbl = proFieldMap.get("scorebcashonsndzysbl");
                String[] split1 = scorebcashonsndzysbl.split(",");
                for (int i=0;i<split1.length;i++){
                    sb.append(hxJson.get(split1[i])==null?"":hxJson.get(split1[i]));
                    sb.append(sep);
                }
                sb.append("1").append(sep).append("666").append(sep);
            }
            if(products.contains("scorecust1")){
                String scorecust = hxJson.getString("scorecust1");
                if(StringUtils.isEmpty(scorecust)){
                    sb.append("0").append(sep).append(sep);
                }else{
                    sb.append("1").append(sep).append(scorecust).append(sep);
                }
            }
            deal(hxJson, products, sb, proFieldMap, sep, apiCode);
        }
    }

    private static void deal(JSONObject hxJson, Set<String> products, StringBuilder sb, Map<String, String> proFieldMap, String sep, String apiCode) {
        if(products.contains("stability_c")){
            String stability_cFields = proFieldMap.get("Stability_c");
            String[] split = stability_cFields.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("applyfeature")){
            String applyfeatureFields = proFieldMap.get("ApplyFeature");
            String[] split = applyfeatureFields.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("totalloan")){
            String totalloanFields = proFieldMap.get("TotalLoan");
            String[] split = totalloanFields.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("scoreafrevoloan")){
            String scoreafrevoloan = hxJson.getString("scoreafrevoloan");
            if(StringUtils.isEmpty(scoreafrevoloan)){
                sb.append("0").append(sep).append(sep);
            }else{
                sb.append("1").append(sep).append(scoreafrevoloan).append(sep);
            }
        }
        if(products.contains("graylistexpand")){
            String graylistexpandFields = proFieldMap.get("GrayListExpand");
            String[] split = graylistexpandFields.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("populationderivation")){
            String populationderivationFields = proFieldMap.get("PopulationDerivation");
            String[] split = populationderivationFields.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("fraudrelation_g")){
            String fraudrelationGFields = proFieldMap.get("FraudRelation_g");
            String[] split = fraudrelationGFields.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("scorecust2")){
            String scorecust = hxJson.getString("scorecust2");
            if(StringUtils.isEmpty(scorecust)){
                sb.append("0").append(sep).append(sep);
            }else{
                sb.append("1").append(sep).append(scorecust).append(sep);
            }
        }
        if((Constants.APICODE_SN_RISK_DEPARTMENT.equals(apiCode)||Constants.APICODE_SN_RISK_DEPARTMENT_QA.equals(apiCode))
                &&products.contains("scoremixuals")&&products.contains("scorebcashonsndzysbl")){
            sb.append("1").append(sep).append("666").append(sep);
        }

        if(products.contains("scoredzminsu")){
            sb.append(hxJson.getString("flag_scoremdzinsu")==null?"":hxJson.getString("flag_scoremdzinsu"))
                    .append(sep).append(hxJson.getString("smi_score")==null?"":hxJson.getString("smi_score")).append(sep);
        }

        if(products.contains("mobcheag")){
            sb.append(hxJson.getString("flag_mobcheag")==null?"":hxJson.getString("flag_mobcheag"))
                    .append(sep).append(hxJson.getString("mca_age")==null?"":hxJson.getString("mca_age")).append(sep);
        }
        if(products.contains("specialgdwph")){
            String specialgdwph = proFieldMap.get("SpecialGdWph");
            String[] split = specialgdwph.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("applyloanstrgdwph")){
            String specialgdwph = proFieldMap.get("ApplyloanstrGdWph");
            String[] split = specialgdwph.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("fraudrelationgdwph")){
            String specialgdwph = proFieldMap.get("FraudrelationGdWph");
            String[] split = specialgdwph.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
        if(products.contains("scorecashongdwph")){
            String specialgdwph = proFieldMap.get("ScoreCashonGdWph");
            String[] split = specialgdwph.split(",");
            for (int i=0;i<split.length;i++){
                sb.append(hxJson.get(split[i])==null?"":hxJson.get(split[i]));
                sb.append(sep);
            }
        }
    }
}
