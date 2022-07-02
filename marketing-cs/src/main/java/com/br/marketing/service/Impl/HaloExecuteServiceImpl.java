package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.HaloExecuteService;
import com.br.marketing.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 多线程处理类
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.service.Impl
 * @Description: 多线程处理类
 * @CreateTime: 2022-07-01 14 :00
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Service
@Slf4j
public class HaloExecuteServiceImpl implements HaloExecuteService {

    @Autowired
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public Result execute(MarketingSyncUser marketingSyncUser) {
        Result result = new Result();
        result.setCode(1);
        try {
            String apiCode = marketingSyncUser.getApiCode();
            // 查询离当前时间最近的有cell的数据 正常的数据
            MarketingSyncUser cellFromCurrentUser = marketingSyncInfoMapper.getCellFromCurrent(apiCode, marketingSyncUser.getCustNum());
            JSONObject reserveFieldObj = new JSONObject();
            MarketingSyncUser updateHisUser = new MarketingSyncUser();
            if (cellFromCurrentUser != null) {
                //更新reserve_field2
                reserveFieldObj.put("message", "根据custNum为key值将距离当前时间最近的cell,status,fail_type清洗入库");
                //更新时间、上传数据cell、status、fail_type
                reserveFieldObj.put("update_update_time", TimeUtils.parseDateToStr(marketingSyncUser.getUpdateTime()));
                reserveFieldObj.put("update_cell", marketingSyncUser.getCell());
                reserveFieldObj.put("update_status", marketingSyncUser.getStatus());
                reserveFieldObj.put("update_fail_type", marketingSyncUser.getFailType());
                updateHisUser.setReserveField2(reserveFieldObj.toJSONString());
                //洗入cell、status、fail_type
                updateHisUser.setCell(cellFromCurrentUser.getCell());
                updateHisUser.setStatus(cellFromCurrentUser.getStatus());
                updateHisUser.setFailType(cellFromCurrentUser.getFailType());
                updateHisUser.setUpdateTime(new Date());
                int update = marketingSyncInfoMapper.updateBySyncHaLuo(updateHisUser, apiCode, marketingSyncUser.getId());
                log.warn("更新操作 update:{} id:{}", update, marketingSyncUser.getId());
                //修改数据的id,修改数据的上传时间,修改数据的status,修改数据的failType,custNum,
            } else {
                reserveFieldObj.put("message", "未找到离当前时间最近的cell 数据");
                updateHisUser.setReserveField2(reserveFieldObj.toJSONString());
                int update = marketingSyncInfoMapper.updateBySyncHaLuoRemark(updateHisUser, apiCode, marketingSyncUser.getId());
            }
        } catch (Exception e) {
            result.setCode(500);
            throw new RuntimeException(e);
        }
        return result;
    }
}
