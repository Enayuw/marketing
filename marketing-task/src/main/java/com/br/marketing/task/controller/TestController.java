package com.br.marketing.task.controller;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.Customer;
import com.br.marketing.mapper.CustomerMapper;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.Impl.CheckServicePackageImpl;
import com.br.marketing.service.Impl.ProductResultByConfigSimpleServiceImpl;
import com.br.marketing.task.service.Impl.LoanWarningServiceImpl;
import com.br.marketing.task.utils.HxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/test")
@RestController
public class TestController {

    @Value("${myenv}")
    private String myenv;

    @Autowired
    CheckServicePackageImpl checkServicePackage;

    @GetMapping("/index")
    public String index(){
        String s = checkServicePackage.checkCsPackage();
        return "hello word".concat(myenv).concat("=====").concat(s);
    }

    @Autowired
    LoanWarningServiceImpl loanWarningService;

    @Resource
    CustomerMapper customerMapper;

    @Autowired
    IProductResultSimpleService productResultSimpleService;

    @GetMapping("/task")
    public String task(){
        Customer customerByApiCode = customerMapper.getCustomerByApiCode("7410571");
        loanWarningService.process(customerByApiCode,null);
        return "";
    }

    @GetMapping("/flat")
    public String hxflat(){
        String s="{\"swift_number\":\"4003434_20220124155040_30484E11A\",\"code\":\"00\",\"Flag\":{\"score\":\"1\",\"scoredata\":\"1\"},\"Score\":{\"scorescashonhrcd\":\"74\",\"scorescashonhrxy\":\"7\"},\"ScoreData\":{\"scorescashonhrcd\":{\"pd_cell_province\":\"辽宁\",\"pd_id_gender\":\"1\",\"pd_cell_type\":\"移动\"},\"scorescashonhrxy\":{\"pd_cell_province\":\"辽宁\",\"pd_id_gender\":\"1\",\"pd_cell_type\":\"移动\"}}}";
        String s1 = HxUtil.hauXiangFlat(s);
        System.out.println(s1);
        return "";
    }

    @GetMapping("/clearInnerCache")
    public String clearInnerCache(@RequestParam("type") Integer type){
        if(Integer.valueOf(1).equals(type)){
            ProductResultByConfigSimpleServiceImpl.flagScoreByinnerList.clear();
        }
        return "success";
    }

    @GetMapping("/getInnerCache")
    public String getInnerCache(@RequestParam("type") Integer type){
        if(Integer.valueOf(1).equals(type)){
            Result<List<String>> flagProduct = productResultSimpleService.getFlagProduct();
            return JSON.toJSONString(flagProduct);
        }
        return "....";
    }
}
