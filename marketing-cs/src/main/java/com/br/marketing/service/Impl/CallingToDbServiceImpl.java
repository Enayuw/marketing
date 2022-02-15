package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.entity.CustomerCallingDialog;
import com.br.marketing.mapper.CustomerCallingDialogMapper;
import com.br.marketing.service.CallingToDbService;
import org.apache.curator.shaded.com.google.common.base.Splitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    public Result callingToDb(TxtToDbDTO dto) {
        String row = dto.getContent();
        HashMap<Integer, String> address = dto.getAddress();
        Integer line = dto.getLine();
        List<String> datas = Splitter.on(",").splitToList(row);
        CustomerCallingDialog customerCallingDialog = new CustomerCallingDialog();
        customerCallingDialog.setApiCode(dto.getApiCode());
        //是否发送数据到客户端(0:未发送/1: 已发送)
        customerCallingDialog.setSendStatus(0);
        customerCallingDialog.setStatus((byte)1);
        customerCallingDialog.setCreateTime(new Date());
        for (int i = 0; i < datas.size(); i++) {
            String sureaddress = address.get(i);
            //"custNum", "callStartTime", "groupType", "taskId"
            switch (sureaddress) {
                case "custNum":
                    customerCallingDialog.setCaseNum(datas.get(i));
                    break;
                case "callStartTime":
                    customerCallingDialog.setCallStartTime(datas.get(i));
                    break;
                case "groupType":
                    customerCallingDialog.setGroupType(Integer.parseInt(datas.get(i)));
                    customerCallingDialog.setUserType(Integer.parseInt(datas.get(i)));
                    break;
                case "taskId":
                    customerCallingDialog.setTaskId(datas.get(i));
                    break;
            }
        }
        int insert = customerCallingDialogMapper.insert(customerCallingDialog);
        System.out.println(insert);
        return null;
    }
}
