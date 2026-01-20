package com.br.marketing.service.tccpa;


import java.util.List;
import java.util.Map;

public interface TcCpaCustCellMappingService {

    String selectCell(String userKey);

    List<Map<String, Object>> selectCellInfo(List<String> userKeyList);

}
