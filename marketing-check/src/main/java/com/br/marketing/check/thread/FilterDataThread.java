package com.br.marketing.check.thread;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.mapper.MarketingUserMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

@Slf4j
public class FilterDataThread implements Callable<String> {

    private List<String> dataList;
    private JSONArray tcArray;
    private String head;
    private MarketingUserMapper marketingUserMapper;
    List<MarketingUser> dirtyUsers=new ArrayList<>();
    Map<String,Object> param=new HashMap<>();
    public FilterDataThread(List<String> dataList, JSONArray tcArray, String head, Integer currentNum, String apiCode, String batchNumber, MarketingUserMapper marketingUserMapper){
        this.dataList=dataList;
        this.tcArray=tcArray;
        this.head=head;
        this.marketingUserMapper = marketingUserMapper;
        param.put("api_code",apiCode);
        param.put("batch_number",batchNumber);
    }
    @Override
    public String call() throws Exception {
        try {
            log.info("任务开始tc_array--{},head--{}",tcArray,head);
            here:
            for (String row:dataList){
               // log.info("row--{}",row);
                String[] rowSplit = row.split(",");
                if(rowSplit.length<5){
                    continue ;
                }
                /**
                 * 遍历每个规则集配置的剔除规则，每个规则集的剔除规则之间是或的关系，只要一个规则集的剔除逻辑命中，则剔除当前数据
                 */
                for(int tc=0;tc<tcArray.size();tc++){
                    String filterJson=tcArray.getString(tc);
                    DocumentContext parse = JsonPath.parse(filterJson);
                    String level= parse.read("$.level").toString();
                    String relation= parse.read("$.relation").toString();
                    String ruleCodes = parse.read("$.ruleCode").toString();

                    //风险等级不为空，则先判断风险等级，如果风险等级满足剔除条件，则结束该条数据的判断
                    if(StringUtils.isNotEmpty(level)){
                        String[] split = level.split(",");
                        for (int i=0;i<split.length;i++){
                            //只要满足配置的任一风险等级，则跳出到外层循环，并将标志改成true；
                            if(rowSplit[5].equals(split[i])){
                                log.info("命中风险等级--{}",row);
                                param.put("cus_num",rowSplit[2]);
                                MarketingUser marketingUser = marketingUserMapper.queryUserByCusNum(param);
                               // log.info("BLoanUser--{]",bLoanUser.toString());
                                dirtyUsers.add(marketingUser);
                                continue here;
                            }
                        }

                        //若没有满足风险等级则继续判断规则是否满足
                        boolean b = dealRuleType(rowSplit, relation, ruleCodes);
                        if(b){
                            //如果规则命中剔除逻辑，则到最外层循环，进行下一条数据处理
                            continue here;
                        }
                        //若风险等级没有配置，直接判断规则
                    }else{
                        boolean b = dealRuleType(rowSplit, relation, ruleCodes);
                        if(b){
                            //如果规则命中剔除逻辑，则到最外层循环，进行下一条数据处理
                            continue here;
                        }
                    }

                }
            }

            if(dirtyUsers.size()>0){
                log.info("insert into dirty table --{}",dirtyUsers.size());
                marketingUserMapper.insertDirtyuser(dirtyUsers);
            }
        }catch (Exception e){
            log.error("FilterDataThread 出错了---{}",e);
        }
        return null;
    }

    /**
     * 遍历配置的规则，先从表头中找到当前规则编号所在的下标位置，然后从数据中拿出当前下标的值，如果该值不为空且规则关系是或，则满足条件直接返回。
     * 若该值不为空，但规则之间的关系是且，
     * 则将该值添加到数组中，最后比较规则编号数组与规则对应值的数组的长度，如果长度相等则证明满足条件。
     *
     * 如果命中剔除逻辑，返回true，否则返回false
     * @param rowSplit
     * @param relation
     * @param ruleCodes
     * @throws IOException
     */
    private boolean dealRuleType(String[] rowSplit, String relation, String ruleCodes) throws IOException {
        try {
            JSONArray ruleCodeArray=JSONArray.parseArray(ruleCodes);
            List<String> ruleCodeResult=new ArrayList<>();
            String[] headSplit = head.split(",");
            for(int i=0;i<ruleCodeArray.size();i++){
                String code = ruleCodeArray.getString(i);
                int index = findIndex(headSplit, code);
                if(index==-1){
                    continue;
                }
                String s = rowSplit[index];
                if(StringUtils.isNotEmpty(s)){
                    if("0".equals(relation)){
                        log.info("命中剔除规则--{}",rowSplit[2]);
                        param.put("cus_num",rowSplit[2]);
                        MarketingUser marketingUser = marketingUserMapper.queryUserByCusNum(param);
                        dirtyUsers.add(marketingUser);
                        return true;
                    }else if(s.length()>1){
                        ruleCodeResult.add(s);
                    }
                }
            }

            if("1".equals(relation)){
                if(ruleCodeResult.size()!=0&&ruleCodeArray.size()!=0&&ruleCodeResult.size()==ruleCodeArray.size()){
                    log.info("命中剔除规则--{}",rowSplit[2]);
                    param.put("cus_num",rowSplit[2]);
                    MarketingUser marketingUser = marketingUserMapper.queryUserByCusNum(param);
                    dirtyUsers.add(marketingUser);
                    return true;
                }
            }
        }catch (Exception e){
            log.error("dealRuleType 出错---{}",e);
        }
        return false;
    }

    public static int findIndex(String[] array,String value){
        for(int i = 0;i<array.length;i++){
            if(array[i].equals(value)){
                return i;
            }
        }

        //当if条件不成立时，默认返回一个负数值-1
        return -1;
    }

}
