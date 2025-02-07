package com.br.marketing.service.carclue.todb;

import java.util.List;

public interface CarCluesDataToDBService {
    void cleanCallDetailsData(List<String> apiCodes, String date);
}
