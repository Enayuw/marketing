package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.CaseShuheUserWithBLOBs;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheTransferDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * 数禾转化实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:25
 */
@Service
@Slf4j
public class PushShuheTransferDataServiceImpl implements IPushShuheTransferDataService {

    @Resource
    private CaseShuheUserMapper caseShuheUserMapper;
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(3, 5);

    @Override
    public ResponseShuheDTO insertShuheTransferData(String apiCode, String jsonData) {
        String msg = "";
        ResponseShuheDTO responseShuheDTO = new ResponseShuheDTO();
        try {
            final ShuheTransferJsonDTO jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
            }.getType());
            if (StringUtils.isEmpty(jsonDTO.getOrderId())) {
                msg += "orderId,释义：批量上传案件编号；";
            }
            if (StringUtils.isEmpty(jsonDTO.getMobile())) {
                msg += "mobile,释义：手机号；";
            }
            if (jsonDTO.getDataItem() == null || jsonDTO.getDataItem().size() < 1) {
                msg += "dataItem,释义：扩展字段；";
            }
            if (!"".equals(msg)) {
                responseShuheDTO.failed("抱歉,缺失必填参数！缺失参数为：".concat(msg));
                log.info("shuhe-1:{}", responseShuheDTO.getDesc());
                return responseShuheDTO;
            }
            String userType = jsonDTO.getBizType();
            if (StringUtils.isEmpty(userType)) {
                /*
                 * 对bizType字段做兜底，对应营销userType,
                 * 当bizType未传时，需要主动去上传接口中查找，
                 * 如果未查到需要返回给客户提示信息，并将数据落库到本地
                 */
                userType = iMarketingSyncUserService.getUserTypeLatestByCustNum(apiCode, jsonDTO.getOrderId());
            }
            IUserType userTypeStrategy = UserTypeStrategyFactory.getUserTypeStrategy(userType);
            CaseShuheUserWithBLOBs caseShuheUser;
            if (userTypeStrategy instanceof UnknownUserType) {
                caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(userTypeStrategy, jsonDTO, apiCode, jsonData);
                responseShuheDTO.failed("抱歉,未知的业务类型\"" + userType + "\"!");
                log.info("shuhe-2:{}", responseShuheDTO.getDesc());
            } else {
                caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(userTypeStrategy, jsonDTO, apiCode, jsonData);
                responseShuheDTO.success();
            }
            CaseShuheUserWithBLOBs finalCaseShuheUser = caseShuheUser;
            System.out.println(finalCaseShuheUser.toString());
            Integer row = null;
            Future<Integer> futureInsert = BR_EXECUTORS.submit(() -> caseShuheUserMapper.insertSelective(finalCaseShuheUser));
            // TODO: 2022/2/11 处理 转化数据为 转化？ 电销？ 黑名单？ 不做处理？ 明文电话需要加密保存到数据库
            try {
                row = futureInsert.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e.getMessage(), e);
            }
            if (row == null || row < 1) {
                msg = "shuhe 推送数据保存失败！";
                log.error(msg);
                responseShuheDTO.failed("抱歉，".concat(msg));
                caseShuheUser = new CaseShuheUserWithBLOBs();
                caseShuheUser.setJsonData(jsonData);
                caseShuheUser.setCustNum(jsonDTO.getOrderId());
                caseShuheUser.setApiCode(apiCode);
                caseShuheUser.setMobile(jsonDTO.getMobile());
                caseShuheUser.setBiztype(jsonDTO.getBizType());
                caseShuheUser.setErrorInfo(msg);
                caseShuheUserMapper.insertSelective(caseShuheUser);
            }
            return responseShuheDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            CaseShuheUserWithBLOBs caseShuheUser = new CaseShuheUserWithBLOBs();
            caseShuheUser.setJsonData(jsonData);
            caseShuheUser.setApiCode(apiCode);
            caseShuheUser.setErrorInfo(e.toString());
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//            caseShuheUserMapper.insertSelective(caseShuheUser);
            return responseShuheDTO.failed();
        }
    }
}
