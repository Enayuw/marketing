package com.br.marketing.thread;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * halo清洗数据
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.check.thread
 * @Description: halo清洗数据
 * @CreateTime: 2022-07-04 10 :09
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Slf4j
public class HaloCleanHistoryThread implements Callable<String> {


    private final MarketingSyncInfoMapper marketingSyncInfoMapper;

    private final MarketingSyncUser marketingSyncUser;


    private final MarketingSyncUser cellFromCurrentUser;


    public HaloCleanHistoryThread(MarketingSyncUser marketingSyncUser, MarketingSyncInfoMapper marketingSyncInfoMapper,MarketingSyncUser cellFromCurrentUser) {
        this.marketingSyncInfoMapper = marketingSyncInfoMapper;
        this.marketingSyncUser = marketingSyncUser;
        this.cellFromCurrentUser = cellFromCurrentUser;
    }

    @Override
    public String call() throws Exception {
        String apiCode = marketingSyncUser.getApiCode();

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
            marketingSyncInfoMapper.updateBySyncHaLuoRemark(updateHisUser, apiCode, marketingSyncUser.getId());
        }
        return "";
    }
}
