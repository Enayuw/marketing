package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.datarelayservice.service.TcSearchService;
import com.br.marketing.mapper.TcSearchMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TcSearchServiceImpl implements TcSearchService {

    @Resource
    private TcSearchMapper tcSearchMapper;
    @Override
    public Object search() {
        List<String> custList = new ArrayList<>();
        custList.add("fyp2023040401");
        custList.add("fyp2023040402");
        return tcSearchMapper.search(custList);
    }
}
