package com.br.marketing.api.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 规则集风险分级 实体类
 */
@TableName(value = "risk_rank")
@Slf4j
public class RiskRank extends Model<RiskRank> {
    private static final long serialVersionUID = 1L;
    @TableField(value = "id")
    private Integer id;
    @TableField(value = "api_code")
    private String apiCode;
    @TableField(value = "logic")
    private String logic;
    private static transient String defaultLogic;
    static {
        try (InputStream in = new ClassPathResource("default_risk_rank.json").getInputStream();
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)
        ){
            char[] bytes = new char[in.available()];
            reader.read(bytes);
            defaultLogic = new String(bytes);
        } catch (FileNotFoundException e) {
            log.error("未找到文件default_risk_rank.json",e);
        } catch (IOException e) {
            log.error("读取文件异常default_risk_rank.json",e);
        }
    }

    /**
     * 获取分数区间
     * @param score 分数
     * @return String
     */
    public String getRiskRank(Integer score){
        logic= StringUtils.isEmpty(logic)?JSONArray.parseArray(defaultLogic).toJSONString():logic;
        try{
            JSONArray array = JSONArray.parseArray(logic);
            for(int i = 0;i < array.size();i++){
                JSONObject riskRank = array.getJSONObject(i);
                if (score <= riskRank.getInteger("max")&&riskRank.getInteger("min")<=score){
                    return riskRank.getString("name");
                }
            }
        }catch (Exception e){
            log.error("规则集风险分级json串解析出错:{}",score,e);
        }
        return null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public void setLogic(String logic) {
        if(StringUtils.isEmpty(logic)){
            this.logic = JSONArray.parseArray(defaultLogic).toJSONString();
        }else {
            this.logic = JSONArray.parseArray(logic).toJSONString();
        }
    }


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "RiskRank{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", logic='" + logic + '\'' +
                '}';
    }
}
