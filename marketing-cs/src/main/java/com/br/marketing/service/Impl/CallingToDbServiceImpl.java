package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.service.CallingToDbService;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author guangchao.zhang
 * @Classname CallingToDbServiceImpl
 * @Description 首次拨打入库实现
 * @Date 2022/2/14 6:22 PM
 */
@Service
public class CallingToDbServiceImpl implements CallingToDbService {

    @Resource
    CustomerCallingDialogMapper customerCallingDialogMapper;

    @Override
    public Result execute(TxtToDbDTO dto) {
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        List<String> dataRows = Splitter.on(",").splitToList(row);
        Result result = new Result();
        result.setCode(1);
        try {
            customerCallingDialogMapper.insert(getCustomerCallingDialog(dto, address, dataRows));
        } catch (Exception e) {
            result.setCode(500);
            throw new RuntimeException(e);
        }
        return result;
    }

    private CustomerCallingDialog getCustomerCallingDialog(TxtToDbDTO dto, HashMap<Integer, String> address, List<String> dataRows) throws Exception {
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setApiCode(dto.getApiCode());
        customerCallingDialog.setLocalId(dto.getLocalId());
        //是否发送数据到客户端(0:未发送/1: 已发送)
        customerCallingDialog.setSendStatus(0);
        customerCallingDialog.setStatus((byte) 1);
        customerCallingDialog.setCreateTime(new Date());
        for (int i = 0; i < dataRows.size(); i++) {
            String headAddress = address.get(i);
            //"custNum", "callStartTime", "groupType", "taskId"
            if ("custNum".equals(headAddress)) {
                headAddress = "caseNum";
            }
            if ("groupType".equals(headAddress)) {
                customerCallingDialog.setUserType(dataRows.get(i));
            }
            Field field = customerCallingDialog.getClass().getDeclaredField(headAddress);
            field.setAccessible(true);
            Class<?> type = field.getType();
            if (type == Integer.class) {
                field.set(customerCallingDialog, Integer.parseInt(dataRows.get(i)));
            } else {
                field.set(customerCallingDialog, dataRows.get(i));
            }
        }
        return customerCallingDialog;
    }
}
