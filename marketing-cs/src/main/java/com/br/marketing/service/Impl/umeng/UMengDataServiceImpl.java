package com.br.marketing.service.Impl.umeng;

import com.br.marketing.entity.UMengData;
import com.br.marketing.mapper.UMengDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class UMengDataServiceImpl implements IUMengDataService {

    @Resource
    private UMengDataMapper uMengDataMapper;

    @Override
    public List<UMengData> selectDeviceAddList(Long localId, String apiCode, Long lastSearchId, Integer searchSize) {
        return uMengDataMapper.selectDeviceAddList(localId,apiCode,lastSearchId,searchSize);
    }

    @Override
    public Integer updateDeviceAddStatus(List<Long> idList, Integer deviceAddStatus) {
        return uMengDataMapper.updateDeviceAddStatus(idList,deviceAddStatus);
    }

    @Override
    public List<UMengData> selectDevicePushList(Long localId,String apiCode, Long lastSearchId, Integer searchSize) {
        return uMengDataMapper.selectDevicePushList(localId,apiCode,lastSearchId,searchSize);
    }

    @Override
    public List<UMengData> selectDeviceByCell(Long localId, String cell) {
        return uMengDataMapper.selectDeviceByCell(localId,cell);
    }

    @Override
    public Integer updatePushStausByIds(List<Long> idList, Integer pushStatus) {
        return uMengDataMapper.updatePushStausByIds(idList,pushStatus);
    }

    @Override
    public List<UMengData> selectEventPushList(Long localId, String apiCode, Long lastSearchId, Integer searchSize) {
        return uMengDataMapper.selectEventPushList(localId,apiCode,lastSearchId,searchSize);
    }
}
