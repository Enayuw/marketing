//package com.br.marketing.innerapi.controller.auth;
//
//import com.br.cloud.web.MethodType;
//import com.br.cloud.web.PrometheusTimeMethod;
//import com.br.marketing.common.commondto.ApiResult;
//import com.br.marketing.common.constants.auth.CodeEnum;
//import com.br.marketing.dto.userinfo.UserDetail;
//import com.br.marketing.entity.auth.MarketingRole;
//import com.br.marketing.entity.auth.MarketingUserInfo;
//import com.br.marketing.service.auth.MarketingRoleService;
//import com.br.marketing.service.auth.MarketingUserInfoService;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.annotation.Resource;
//import java.util.List;
//
///**
// * -------------------------------
// *
// * @author guangchao.zhang
// * @Description 用户控制器
// * @Date 2022/3/10 11:36 AM
// * ------------------------------
// */
//@RestController
//@RequestMapping("user")
//public class MarketingUserInfoController {
//
//    @Resource
//    private MarketingUserInfoService marketingUserInfoService;
//
//    @Resource
//    private MarketingRoleService marketingRoleService;
//    /**
//     * 获取所有角色
//     *
//     * @param
//     * @return
//     */
//    @GetMapping("/getAllRole")
//    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
//    public ApiResult<List<MarketingRole>> getAllRole() {
//       List<MarketingRole> marketingRoles =  marketingRoleService.selectRoleList();
//       return new ApiResult<List<MarketingRole>>().success(marketingRoles);
//    }
//
//    ///**
//    // * 查看用户列表
//    // *
//    // * @param key
//    // * @return
//    // */
//    //@GetMapping("/list")
//    //@PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
//    //public String list(String key) {
//    //    return respJson(CodeEnum.SUCC, userService.getList(this.getPage(User.class), key));
//    //}
//    /**
//     * 保存用户
//     *
//     * @param user
//     * @return
//     */
//    @GetMapping("/save")
//    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
//    public ApiResult<Boolean> insert(MarketingUserInfo user) {
//        UserDetail loginUser = getUserDetailFromSession();
//        if (StringUtils.isBlank(user.getUserName())) {
//            return new ApiResult<Boolean>().fail();
//        }
//        if (!checkUserName(user.getUsername())) {
//            return respJson(CodeEnum.REPEAT);
//        }
//        userService.saveUser(loginUser, user);
//        return respJson(CodeEnum.SUCC);
//    }
//}
//
