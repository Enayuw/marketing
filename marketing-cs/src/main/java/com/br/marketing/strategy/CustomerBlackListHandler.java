package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.robotaiapi.input.BlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneParentDTO;
import com.br.marketing.client.robotaiapi.output.ReqBlackPhoneVO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
 * @Description : 客服黑名单接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 18:08
 */
@Slf4j
@Service
public class CustomerBlackListHandler extends AbstractExternalInterfaceHandler<BlackDetailDTO> {

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Override
    public JSONObject call(List<BlackDetailDTO> blackDetailDTOList, ProcessHandlerContext context) {
        /**
         * 客服黑名单接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = blackDetailDTOList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BlackDetailDTO> subList;
            if (i == pageCount) {
                subList = blackDetailDTOList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = blackDetailDTOList.subList((i - 1) * pageSize, pageSize * (i));
            }
            BlackPhoneDTO<BlackDetailDTO> jsondata = new BlackPhoneDTO<>();
            jsondata.setMethod("blackData");
            jsondata.setData(subList);
            ReqBlackPhoneDTO dto = new ReqBlackPhoneDTO();
            dto.setApiCode(context.getApiCode());
            dto.setJsonData(JSON.toJSONString(jsondata));
            ReqBlackPhoneParentDTO parentDTO = new ReqBlackPhoneParentDTO();
            parentDTO.setDto(dto);
            parentDTO.setBlackDetailDTOList(subList);
            parentDTO.setTransferInfoId(context.getTransferInfoId());
            Result<String> callBalckResult = callCustomerBlack(parentDTO,0);
            if (!ResultCode.SUCCESS.getValue().equals(callBalckResult.getCode())) {
                log.error(String.format("推送黑名单报错：%s", callBalckResult.getData()));
            }

        }
        return null;
    }

    /**
     * 调用客服黑名单接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param parentDTO
     * @return
     */
    @RetryMethod
    public Result<String> callCustomerBlack(ReqBlackPhoneParentDTO parentDTO,Integer retry){
        ReqBlackPhoneVO reqBlackPhoneVO = robotaiApiServiceClient.pushBlack(parentDTO);
        if ("00".equals(reqBlackPhoneVO.getCode()) && CollectionUtils.isEmpty(reqBlackPhoneVO.getData())) {
            Set<String> set = parentDTO.getBlackDetailDTOList().stream().map(BlackDetailDTO::getDataId).collect(Collectors.toSet());
            saveBizLog(String.join(",", set), handlerEnum().getCode(), parentDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        if (("00".equals(reqBlackPhoneVO.getCode()) && (!CollectionUtils.isEmpty(reqBlackPhoneVO.getData())))
                || "9999".equals(reqBlackPhoneVO.getCode())) {
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue())
                    .setDate("9999".equals(reqBlackPhoneVO.getCode()) ? "9999" : "部分成功");
        }
        return new Result().setCode(ResultCode.FAIL.getValue()).setDate(reqBlackPhoneVO.getCode());
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.CUSTOMER_BLACK_LIST;
    }
}
