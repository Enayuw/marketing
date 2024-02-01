package com.br.marketing.thread;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.Callable;

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




    public HaloCleanHistoryThread(MarketingSyncUser marketingSyncUser, MarketingSyncInfoMapper marketingSyncInfoMapper) {
        this.marketingSyncInfoMapper = marketingSyncInfoMapper;
        this.marketingSyncUser = marketingSyncUser;
    }

    @Override
    public String call() {
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
                //修改数据的id,修改数据的上传时间,修改数据的status,修改数据的failType,custNum,
            } else {
                reserveFieldObj.put("message", "未找到离当前时间最近的cell 数据");
                updateHisUser.setReserveField2(reserveFieldObj.toJSONString());
                marketingSyncInfoMapper.updateBySyncHaLuoRemark(updateHisUser, apiCode, marketingSyncUser.getId());
            }
        }catch (Exception e){
            log.error("清洗更新报错，{}",e);
        }

        return "";
    }
}
