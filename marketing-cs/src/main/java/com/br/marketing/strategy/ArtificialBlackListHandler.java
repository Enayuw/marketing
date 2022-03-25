package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.client.dassservice.output.DassExportAdapterDTO;
import com.br.marketing.context.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
 * @Description : 人工黑名单接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 18:12
 */
@Service
@Slf4j
public class ArtificialBlackListHandler extends AbstractExternalInterfaceHandler<BlackListDTO>{

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;


    @Override
    public JSONObject call(List<BlackListDTO> transferList, ProcessHandlerContext context) {
        /**
         * 黑名单接口 每1000条数据一个批次
         */
        int pageSize = 1000;
        int totalCount = transferList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BlackListDTO> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = transferList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferList.subList((i - 1) * pageSize, pageSize * (i));
            }

            DassExportAdapterDTO dassExportAdapterDTO = new DassExportAdapterDTO(subList);
            dassExportAdapterDTO.setTransferInfoId(context.getTransferInfoId());

            methodRetryHandlerService.callBlackList(dassExportAdapterDTO,0);
        }

        return null;
    }


    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_BLACK_LIST;
    }
}
